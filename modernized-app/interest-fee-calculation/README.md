# interest-fee-calculation

`InterestandFeeCalculation` ドメイン（[requirements-spec](../../requirements-spec/ja/InterestandFeeCalculation/)）を実装したモダナイズ後のJavaアプリケーション（Spring Boot / Spring Batch）です。

対象は CardDemo の `demo/cbl/CBACT04C.cbl`（バッチプログラム。JCL `INTCALC.jcl` から起動）。取引カテゴリ別残高を口座ごとに集計し、割引グループから利率を引いて月次利息を計算、利息取引を記帳、口座残高を更新します。

## 実行方法

```bash
cd modernized-app/interest-fee-calculation
mvn test          # 単体テスト＋結合テストの実行（H2インメモリDB使用）
mvn spring-boot:run -Dspring-boot.run.arguments=--interestcalc.business-date=2022071800
```

実運用に載せる場合、`application.yml` のH2設定をAurora PostgreSQL等の接続情報に差し替えてください（本PoCではデータストアの実装のみH2に簡略化しています。エンティティ定義自体はRDB非依存です）。

## COBOL → Java マッピング

| COBOL（CBACT04C.cbl） | Java |
|---|---|
| `ACCOUNT-FILE` / `CVACT01Y` (ACCOUNT-RECORD) | `domain.Account` + `repository.AccountRepository` |
| `XREF-FILE` / `CVACT03Y` (CARD-XREF-RECORD) | `domain.CardXref` + `repository.CardXrefRepository` |
| `DISCGRP-FILE` / `CVTRA02Y` (DIS-GROUP-RECORD) | `domain.DiscountGroup` (+`DiscountGroupId`複合キー) + `repository.DiscountGroupRepository` |
| `TCATBAL-FILE` / `CVTRA01Y` (TRAN-CAT-BAL-RECORD) | `domain.TransactionCategoryBalance` (+`TransactionCategoryBalanceId`複合キー) + `repository.TransactionCategoryBalanceRepository` |
| `TRANSACT-FILE` / `CVTRA05Y` (TRAN-RECORD) | `domain.Transaction` + `repository.TransactionRepository` |
| `PROCEDURE DIVISION` 全体（メインループ、口座切り替え検知） | `batch.InterestCalcProcessor#run` |
| `1100-GET-ACCT-DATA` | `InterestCalcProcessor#getAccountData` |
| `1110-GET-XREF-DATA` | `InterestCalcProcessor#getXrefData` |
| `1200-GET-INTEREST-RATE` + `1200-A-GET-DEFAULT-INT-RATE` | `InterestCalcProcessor#lookupInterestRate` |
| `1300-COMPUTE-INTEREST` | `InterestCalcProcessor#computeMonthlyInterest` |
| `1300-B-WRITE-TX` | `InterestCalcProcessor#writeInterestTransaction` |
| `1050-UPDATE-ACCOUNT` | `InterestCalcProcessor#updateAccount` |
| `1400-COMPUTE-FEES`（原本で "To be implemented" の空処理） | `InterestCalcProcessor#computeFees`（同じく空実装） |
| `Z-GET-DB2-FORMAT-TIMESTAMP` | `util.Db2TimestampFormatter` |
| `9999-ABEND-PROGRAM` | `batch.InterestCalcAbendException`（Spring Batchのステップ失敗として表現） |
| `INTCALC.jcl`（ジョブ・ステップ定義） | `batch.InterestCalcJobConfig` |

各メソッドのJavadocに、対応する `REQ-F-XXX`（[requirements.md](../../requirements-spec/ja/InterestandFeeCalculation/requirements.md)）も記載し、要件→コードのトレーサビリティを保っています。

## 原本から発見した既知の不具合（意図的に再現）

原本のCOBOLを精読したところ、要件仕様書（AWS Transformが自動生成したもの）のREQ-F-024には無い問題が見つかりました。

```cobol
PERFORM UNTIL END-OF-FILE = 'Y'
    IF  END-OF-FILE = 'N'
        PERFORM 1000-TCATBALF-GET-NEXT
        IF  END-OF-FILE = 'N'
            ... (口座切り替え検知・利息計算) ...
        END-IF
    ELSE
         PERFORM 1050-UPDATE-ACCOUNT      ← ここが到達不能
    END-IF
END-PERFORM.
```

外側の `PERFORM UNTIL END-OF-FILE = 'Y'` により、ループ本体に入る時点で `END-OF-FILE` は常に `'N'` であることが保証されているため、`ELSE` 節（ファイル終端時の最終口座残高更新）は実質的にデッドコードで、決して実行されません。

**結果:** ファイル内で最後に処理された口座は、利息取引（`TRANSACT-FILE`）は正しく記帳されるにもかかわらず、口座残高（`ACCOUNT-FILE`）には利息が反映されないまま処理が終了します。

このリポジトリの実装では、**この挙動を含めて原本を忠実に再現する**という判断を行いました（移行時に業務ロジックを暗黙に「修正」しない、という移行の定石に従っています）。`InterestCalcProcessor` のJavadocおよび以下のテストで、この挙動を明示的に検証しています。

- `InterestCalcProcessorTest#lastAccountInFileIsNeverBalanceUpdated_reproducesSourceDefect`
- `InterestCalcJobIntegrationTest#runsFullJobAcrossMultipleAccountsAndReproducesLastAccountDefect`

修正が必要な場合は、独立した変更（本モダナイゼーションとは別コミット・別レビュー）として対応することを推奨します。

## 数値精度について

`DIS-INT-RATE`・`TRAN-CAT-BAL`・`ACCT-CURR-BAL` 等はいずれもCOBOLの `PIC S9(n)V99` (2桁小数)。月次利息計算 `(TRAN-CAT-BAL × DIS-INT-RATE) ÷ 1200` は、COBOLの `COMPUTE`（`ROUNDED`指定なし）が小数点2桁に**切り捨て**る仕様に合わせ、`BigDecimal` + `RoundingMode.DOWN` で実装しています（四捨五入ではありません）。

## テスト

```bash
mvn test
```

- `InterestCalcProcessorTest`: 利率のDEFAULTフォールバック、切り捨て計算、口座切り替え検知、レート0時のスキップ、既知バグの再現を単体テスト（Mockitoでリポジトリをモック化）
- `InterestCalcJobIntegrationTest`: Spring Batch Test + H2で、複数口座にまたがるジョブ全体を実行し、最終口座のバグ再現を含めて検証
