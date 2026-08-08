# AWS Transform for mainframe — CardDemo PoC

AWS Transform for mainframe を使い、AWS公式のメインフレームサンプルアプリ [CardDemo](https://github.com/aws-samples/aws-mainframe-modernization-carddemo)（COBOL/JCL）に対してモダナイゼーション検証（Reimagineワークフロー）を実施した際の、元ソースと生成物一式です。

## 手順書

別のメインフレーム資産・別プロジェクトで同じ流れを再現するための詳細手順は [PROCEDURE.md](./PROCEDURE.md) を参照してください（AWS Transform有効化 → ワークスペース/ジョブ作成 → コネクタ設定（Neptune含む）→ 実行 → 成果物取得 → 後片付けまで）。

## やったこと

1. AWS Transformを有効化し、ワークスペース・S3バケット・Neptune連携用のコネクタ（CloudFormationテンプレートで構築）をセットアップ
2. CardDemoのソースコードをS3に配置し、Mainframe Modernizationジョブを作成
3. コード分析（Analyze code）→ データ分析（Data analysis）→ ビジネスロジック抽出（Extract business logic）→ 要件仕様書生成（Generate requirements）の「Reimagine」ワークフローを実行
4. 生成された要件仕様書は英語版・日本語訳版の両方を取得

**注意:** 今回実行したのは「Reimagine」ワークフローです。これは要件仕様書（人間可読なMarkdown+YAML）を生成するところまでで、**実際のJavaコードへの自動変換は行っていません**。コード生成は、別途Custom job plan（Refactor/Reforge）を実行するか、この要件仕様書をKiroなどのコーディングエージェントに渡して行う想定です。

## フォルダ構成

| フォルダ | 内容 |
|---|---|
| `original-source/` | CardDemoの元COBOL/JCL/BMS/コピーブック等のソースコード一式（[aws-samples/aws-mainframe-modernization-carddemo](https://github.com/aws-samples/aws-mainframe-modernization-carddemo) の `main` ブランチそのまま） |
| `analysis/code-analysis/` | コード分析結果（ファイル分類、依存関係、重複ID、欠落ファイル、コードベース課題など） |
| `analysis/logs/` | 分析処理時のログ（依存関係マッピング、循環的複雑度、重複ID、同名異義語など） |
| `analysis/data-dictionary/` | データ項目のメタデータ辞書（COBOLコピーブック・DDLのフィールド定義） |
| `analysis/data-lineage/` | データセット・JCL・プログラム間の参照関係（データリネージ） |
| `business-rules/` | ビジネスルール抽出結果のサマリー（`application.json`） |
| `requirements-spec/en/` | 生成された要件仕様書（英語版）。11のビジネスドメインごとに `requirements.md`（EARS記法の要件一覧）、`discovery/`（画面・バッチジョブ・データストア一覧）、`traceability.yaml`（要件と元コードの対応関係） |
| `requirements-spec/ja/` | 同上の日本語訳版 |

## 除外したもの（意図的）

以下は、内容の重複・容量・内部実装の詳細情報であるため今回は含めていません（必要であれば元のS3バケットから別途取得可能です）。

- ビジネスルール抽出のCOBOLファイル単位の中間処理データ（`stage1〜stage10` 系JSON、約800ファイル・55MB） — 内部パイプラインの途中経過であり、最終成果物である `requirements-spec/` に集約済みのため
- `hash_mapping.csv`（匿名化コードベースのハッシュ⇔元パス対応表） — 匿名化処理の対応表を一緒に公開すると匿名化の意味がなくなるため除外
- 監査用SQLiteデータベース（`bre_audit_latest.db` 等）、難読化ログ、デバッグ用zip — 内部診断用データのため
- データファイル（EBCDIC/ASCIIのサンプル口座・カード・取引データ）— CardDemo付属のダミーデータで機密情報ではありませんが、今回の要件仕様書生成には直接使っていないため割愛
- Neptuneナレッジグラフ同期用データ（`.nq` ファイル、約56MB） — Neptune内部同期専用のRDFデータで人が読む形式ではないため

## セキュリティ上の配慮

- AWSアカウントID、IAMロールARN、S3/Neptune/VPCなどのリソース識別子は本リポジトリのどこにも含まれていないことを確認済みです
- 認証情報・アクセスキーの類は含まれていません
- リポジトリは非公開（Private）で作成しています
