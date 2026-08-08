# 実施手順（AWS Transform for mainframe お試し手順）

このドキュメントは、今回CardDemoで実施した一連の作業を、別のメインフレーム資産や別プロジェクトで再現するための手順書です。

## 前提条件

- AWSアカウント
- AWS Transformを有効化できる権限（管理者権限）
- IAM Identity Centerを構成できる権限
- 対象コード（今回はCardDemoのCOBOL/JCL一式）

---

## 1. AWS Transformを有効化する

1. AWSマネジメントコンソールで「AWS Transform」を検索し、**Get started** を選択
2. IAM Identity Center（または既存のID連携、IAMのみアクセス）を設定 — この選択は後から変更不可
3. 暗号化キーを選択（デフォルトのAWS管理キーで可）
4. 有効化する機能を選択：**Web application**（エージェントUI）を有効化
5. **Enable AWS Transform** を選択

## 2. ユーザーをアサインする

1. IAM Identity Centerにユーザー/グループを追加
2. AWS Transformの **Users** タブでユーザーを割り当て
3. ユーザーは招待を承諾し、Web application URLからログイン

## 3. ワークスペースとジョブを作成する

1. Web applicationにログインし、ワークスペースを新規作成
2. ワークスペースのランディングページで **Ask AWS Transform to create a job** を選択
3. ジョブの種類として **Mainframe Modernization** を選択
4. チャットに目的を入力（例：「Transform code to Java」）。提案されたジョブタイプ・名前・目的を確認し **Yes** で確定 → **Create job**

## 4. 対象コードをS3に準備する

1. AWS Transformが有効なアカウント・リージョンと**同じ**リージョンにS3バケットを作成
2. 対象のCOBOL/JCL/コピーブック等のソースコードをZIPに圧縮
   - 今回の例（CardDemo）:
     ```bash
     git clone https://github.com/aws-samples/aws-mainframe-modernization-carddemo.git
     # app フォルダ（ソースコード一式）を carddemo フォルダにコピーしてZIP化
     ```
3. 作成したZIPをS3バケットにアップロード

## 5. コネクタを設定する（Mainframe reimagine connector）

Reimagine/Assessワークフローを使う場合、S3に加えて **Amazon Neptune**（コード解析結果を保存するナレッジグラフDB）が必要です。

### 5-1. CloudFormationでNeptune環境を構築する

AWS提供のテンプレート `neptune-kg-setup.yaml` を使用（ダウンロード元はAWS Transformのドキュメント「Set up a connector」内）。

```bash
aws cloudformation deploy \
  --template-file neptune-kg-setup.yaml \
  --stack-name <スタック名> \
  --parameter-overrides BucketName=<手順4で作成したS3バケット名> \
  --capabilities CAPABILITY_NAMED_IAM \
  --region <AWS Transformを有効化したリージョン>
```

**コスト最適化のポイント：** テンプレートにはNeptuneの読み取りレプリカ（`NeptuneReadReplica`）が含まれていますが、これは高可用性のための推奨構成であり必須ではありません。検証目的であればこのリソースを削除してからデプロイすると、Neptuneインスタンスのコストを概ね半分に抑えられます。VPCエンドポイント（Transform Agents API、Bedrock Runtime、RDS、EC2、CloudWatch用の5つ）はAWS Transform側の接続に必須のため削除できません。

### 5-2. 出力値をAWS Transformのコネクタ設定フォームに入力する

デプロイ完了後、スタックの **Outputs** から以下を取得し、AWS Transformのコネクタ設定画面に入力します。

| フォーム項目 | CloudFormation Outputs |
|---|---|
| S3 bucket ARN | （S3バケットのARN。手動で `arn:aws:s3:::<バケット名>` を入力） |
| Neptune cluster ARN | `NeptuneClusterArn` |
| Neptune cluster resource ID | `NeptuneClusterResourceId` |
| サブネット | `AppSubnetIds`（カンマ区切り） |
| セキュリティグループ | `AppSecurityGroupId` |
| Neptune データロード用IAMロール | `NeptuneS3BulkLoaderRoleArn` |
| KMS key ARN | 空欄でOK（AWS管理キー使用時） |

**注意：** Neptune Serverless・VPCインターフェースエンドポイントは稼働中ずっと課金が発生します。検証終了後は忘れずにスタックを削除してください（`aws cloudformation delete-stack --stack-name <スタック名>`）。

## 6. S3 URIを投入する

1. コネクタ設定が完了すると、ジョブパネルに **Kick off** ステップが表示される
2. 「Kick off」が表示されたタイミングで、チャットまたはUIフォームにS3のZIPファイルのURI（例：`s3://<バケット名>/demo.zip`）を入力
3. ファイル種別を聞かれたら、通常は **ソースコード** を選択（SMFレコード・SCRTファイルは運用実績データやライセンス課金用データなので、アプリケーションコードの場合は該当しない）

## 7. ワークフローを実行する

「Reimagine」ワークフローは以下の順で自動的に進みます。

1. **Assess**（アセスメント） — コード・依存関係の分析
2. **Extract business logic**（ビジネスロジック抽出） — S3にJSON形式で結果を保存
3. **Generate requirements**（要件生成） — ビジネスドメインごとに `requirements.md` を生成（EARS記法）
4. **Traceability** — `traceability.yaml` で要件と元コードの対応を追跡可能

**重要：** このワークフローは要件仕様書の生成までで、**実際のJavaコード変換は行いません**。コードまで自動生成したい場合は、別途「Custom job plan」でAnalyze code → Decomposition → Refactor（→ 任意でReforge）を実行する必要があります。

## 8. 成果物をS3から取得する

ジョブの成果物は `s3://<バケット名>/transform-output/<ジョブID>/` 配下に格納されます。

```bash
aws s3 sync s3://<バケット名>/transform-output/ ./transform-output-all \
  --exclude "*/inputs/*" \
  --exclude "neptune-quads/*"
```

- `*/inputs/*` … 投入した元コードのコピー（手元に既にあるため重複回避で除外。ジョブIDのプレフィックスが付くため `inputs/*` ではなく `*/inputs/*` と指定する点に注意）
- `neptune-quads/*` … Neptune同期用の内部RDFデータ（人が読む用途ではない、数十MB単位になりがち）

## 9. 後片付け

検証が終わったら、課金対象リソースを削除します。

```bash
aws cloudformation delete-stack --stack-name <手順5-1のスタック名>
```

---

## つまずいたポイント（今回の学び）

- **SSOトークンの期限切れ**：作業を挟むと`aws sso login --profile <プロファイル名>`の再実行が必要になることがある
- **コネクタ設定フォームが出るタイミング**：ジョブ作成直後ではなく、プランの実行が進み「Kick off」ステップが表示されるまで待つ必要がある
- **`inputs/*`の除外パターン**：S3の出力パスにはジョブID（UUID）のプレフィックスが付くため、`--exclude`パターンは`*/inputs/*`のようにワイルドカードを前に付ける必要がある
- **Reimagine ≠ 自動コード変換**：Reimagineワークフローは要件仕様書の生成までで、Javaコードそのものは生成しない（Kiro等のコーディングエージェントか、Custom job planのRefactorが別途必要）
- **Custom job planに「Refactor」が無い（新規顧客の場合）**：自動COBOL→Java変換（旧Blu Ageエンジン、「AWS Transform for mainframe refactor」）は、AWS Mainframe Modernization self-managed experienceの一部として提供されていたが、2026年6月30日付で新規顧客への提供が終了。新規に有効化したAWS TransformのCustom job planには、分析・ドキュメント生成・テスト計画系の機能（Analyze code / Analyze data / Analyze activity metrics / Generate technical documentation / Extract business logic / Decompose code / Plan test cases / Generate test data collection scripts / Generate test automation scripts）のみが表示され、コード変換機能は含まれない。実際にコードが欲しい場合は、生成済みの要件仕様書（`requirements-spec/`）をKiroやClaude Codeなどのコーディングエージェントに渡して実装する経路が現実的（本リポジトリの`modernized-app/`はその実例）
