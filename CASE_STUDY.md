# ケーススタディ：AWS Transform for mainframe を CardDemo で試した記録

このドキュメントは、AWS Transform for mainframeの試用からJavaコード実装までの一連の作業を、時系列の解説付きでまとめたものです。各ステップの詳細な手順は[PROCEDURE.md](./PROCEDURE.md)、成果物の一覧は[README.md](./README.md)を参照してください。ここでは「何を・なぜやったか」「何が分かったか」に重点を置いています。

## 目的

AWSのメインフレームモダナイゼーションサービス「AWS Transform for mainframe」を、AWS公式サンプルアプリ [CardDemo](https://github.com/aws-samples/aws-mainframe-modernization-carddemo)（COBOL/JCLベースのクレジットカード管理システム）を題材に実際に試し、以下を明らかにすることを目的としました。

- サービスの実際のセットアップ手順とコスト構造
- コード分析・ビジネスルール抽出・要件仕様書生成の実力
- 「要件仕様書からJavaコードまで」実際に到達できるのか、その現実的な経路

## 作業の流れ

### 1. AWS Transformの有効化とワークスペース作成

AWSコンソールからAWS Transformを有効化し、IAM Identity Center経由でユーザーを割り当て、Web applicationにログインしてワークスペースを作成しました。

### 2. CardDemoソースの準備とジョブ作成

CardDemoのソース一式（`app`フォルダ）をZIP化してS3バケットにアップロードし、「Mainframe Modernization」ジョブを作成しました。

### 3. コネクタ設定（Neptuneナレッジグラフ）

Reimagineワークフロー（後述）を使うには、コード解析結果を格納する**Amazon Neptune**クラスターが必要でした。AWS提供のCloudFormationテンプレートを使ってVPC・Neptune Serverless・IAMロール一式を構築しましたが、そのままだと月$200〜300程度のランニングコストが見込まれたため、**読み取りレプリカを省略した最小構成**に調整してデプロイしました（詳細は[PROCEDURE.md](./PROCEDURE.md)）。

### 4. Reimagineワークフローの実行

コネクタ設定後、「Reimagine」ワークフロー（コード分析→データ分析→ビジネスロジック抽出→要件仕様書生成）を実行しました。結果、CardDemoは自動的に**11のビジネスドメイン**に分解され、あわせて**約666件の要件**（EARS記法、`REQ-F-XXX`形式）が生成されました。各要件には元のCOBOLソースまでのトレーサビリティ（`traceability.yaml`）が付与されています。

同時に、コード分析でCardDemo特有の課題（本番環境固有の外部ライブラリ参照の欠落など）も検出されました。詳細は[`analysis/`](./analysis/)を参照してください。

### 5. 成果物の整理とGitHubへの初回公開

S3に出力された成果物一式（原本ソース・分析結果・要件仕様書）を取得し、以下を判断した上でこのリポジトリを作成しました。

- 匿名化処理の対応表（`hash_mapping.csv`）や内部デバッグ用データなど、公開する意味のないものは除外
- Neptuneナレッジグラフ同期用の内部RDFデータ（約56MB）は人が読む形式でないため除外
- AWSアカウントID・ARN等の機微情報が含まれていないことを最終スキャンで確認
- リポジトリは非公開（Private）で作成

### 6. 「実際にJavaコードまで生成したい」→ Custom job planを試す

Reimagineは要件仕様書の生成までで、実際のコード変換は行いません。そこで「Custom job plan」で機械的なCOBOL→Java変換（旧Blu Ageエンジンによる自動リファクタリング）を試みましたが、**Custom job planの選択肢一覧に変換系の機能が存在しない**ことが判明しました。

調査の結果、この自動変換機能を含む「AWS Mainframe Modernization self-managed experience」は、**2026年6月30日付けで新規顧客への提供が終了**しており、このセッションで新規に有効化したAWS Transformアカウントでは利用できないことが分かりました。既存顧客は引き続き利用可能ですが、新規にはコンソール上に選択肢自体が表示されません。

これはAWSが今後の戦略として「Reimagine（要件仕様化）＋ Kiro/Claude Codeなどのコーディングエージェントによる実装」に軸足を移していることを示す、実運用上重要な発見でした。

### 7. Claude Codeによるコード生成（Interest and Fee Calculationドメイン）

方針を転換し、生成済みの要件仕様書を入力にClaude Codeで実際にJavaコードを実装しました。最初のパイロットとして、11ドメインの中から**Interest and Fee Calculation**（利息・手数料計算、プログラム3本・要件29件とコンパクトで業務ロジックも明確）を選定。

実装前に要件仕様書と元のCOBOLソース（`CBACT04C.cbl`）を突き合わせたところ、**AI生成の要件仕様書には記載のない潜在バグ**を発見しました。ループ制御の構造上、ファイル終端到達時に呼ばれるはずの「最終口座の残高更新」処理が実際には到達不能なデッドコードになっており、バッチ内で最後に処理した口座だけ、計算された利息が取引としては記帳されるのに口座残高には反映されない、という不具合です。

「移行時に業務ロジックを勝手に修正しない」という移行の定石に従い、**この挙動も含めて忠実に再現する**方針を採用。Spring Boot / Spring Batchで実装し、この挙動を明示的に検証する単体・結合テストを含め、全テストが成功することを確認した上でリポジトリに反映しました。詳細は[`modernized-app/interest-fee-calculation/README.md`](./modernized-app/interest-fee-calculation/README.md)を参照してください。

## 得られた知見

1. **AWS Transformのセットアップは軽くない**：Neptuneベースのコネクタ構築だけでVPC・IAMロール・複数のVPCエンドポイントが必要で、コスト試算と構成の最小化判断が要る
2. **Reimagineは「設計書」を作る機能であり、「変換機」ではない**：要件仕様書と業務ロジックのトレーサビリティ確保が主目的で、コード生成は別の仕組み（コーディングエージェント）に委ねる設計
3. **自動COBOL→Java変換は新規顧客には提供されていない（2026年8月時点）**：Custom job planの機能一覧を見ただけでは気づきにくく、AWSのサービス提供状況の変更履歴まで確認する必要があった
4. **AIが生成した要件仕様書は「意図」を書くため、原本の実装バグまでは拾いきれないことがある**：実装前に必ず原本ソースとの突き合わせが必要（今回のケースで実際に1件発見）
5. **移行時のバグ再現は意図的な判断が必要**：「正しく直す」か「忠実に再現する」かは移行プロジェクトの方針次第であり、機械的に決めるべきではない

## 今後の展望

- 他のビジネスドメイン（例：`AccountandCardInquiryServices`）への展開
- 生成したJavaコードのレビュー・リファクタリング
- 実運用を見据えたRDB（Aurora PostgreSQL等）への切り替え、IaCテンプレートでのデプロイ環境構築
- 詳細なロードマップは会話の記録を参照（フェーズ0〜6の全体計画）

## 関連ドキュメント

- [README.md](./README.md) — リポジトリ全体の構成一覧
- [PROCEDURE.md](./PROCEDURE.md) — 再現可能な手順書（コマンド付き）
- [modernized-app/interest-fee-calculation/README.md](./modernized-app/interest-fee-calculation/README.md) — Java実装の詳細、COBOL⇔Javaマッピング、発見したバグの技術的説明
