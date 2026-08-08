# データモデル

*12個のケイパビリティにわたる33個のデータエンティティ。*

| データストア | タイプ | アクセス元 |
|-----------|------|-------------|
| AWS.M2.CARDDEMO.ACCTDATA.ARRYPS | QSAM | AccountandCardInquiryServices |
| AWS.M2.CARDDEMO.ACCTDATA.IMPORT | データセット | DataImportandExportOperations |
| AWS.M2.CARDDEMO.ACCTDATA.PSCOMP | QSAM | AccountandCardInquiryServices |
| AWS.M2.CARDDEMO.ACCTDATA.VBPS | QSAM | AccountandCardInquiryServices |
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | VSAM KSDS | AccountandCardInquiryServices, AuthorizationManagement, DataImportandExportOperations, InteractiveNavigationandScreenControl, InterestandFeeCalculation, StatementandReportGeneration, TransactionProcessingandPosting |
| AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.PATH | VSAM PATH | InteractiveNavigationandScreenControl |
| AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | VSAM KSDS | AccountandCardInquiryServices, DataImportandExportOperations, InteractiveNavigationandScreenControl |
| AWS.M2.CARDDEMO.CARDXREF.IMPORT | データセット | DataImportandExportOperations |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH | VSAM PATH | AuthorizationManagement, InteractiveNavigationandScreenControl |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | VSAM KSDS | AccountandCardInquiryServices, AuthorizationManagement, DataImportandExportOperations, InteractiveNavigationandScreenControl, InterestandFeeCalculation, StatementandReportGeneration, TransactionProcessingandPosting |
| AWS.M2.CARDDEMO.CUSTDATA.IMPORT | データセット | DataImportandExportOperations |
| AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | VSAM KSDS | AccountandCardInquiryServices, AuthorizationManagement, DataImportandExportOperations, InteractiveNavigationandScreenControl, StatementandReportGeneration |
| AWS.M2.CARDDEMO.DALYREJS | QSAM | TransactionProcessingandPosting |
| AWS.M2.CARDDEMO.DALYTRAN.PS | Non VSAM | TransactionProcessingandPosting |
| AWS.M2.CARDDEMO.DATEPARM | QSAM | StatementandReportGeneration |
| AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | VSAM KSDS | InterestandFeeCalculation |
| AWS.M2.CARDDEMO.EXPORT.DATA | VSAM KSDS | DataImportandExportOperations |
| AWS.M2.CARDDEMO.IMPORT.ERRORS | QSAM | DataImportandExportOperations |
| AWS.M2.CARDDEMO.PAUTDB.CHILD.FILEO | QSAM | DatabaseMaintenanceandSynchronization |
| AWS.M2.CARDDEMO.PAUTDB.ROOT.FILEO | QSAM | DatabaseMaintenanceandSynchronization |
| AWS.M2.CARDDEMO.STATEMNT.HTML | QSAM | StatementandReportGeneration |
| AWS.M2.CARDDEMO.STATEMNT.PS | QSAM | StatementandReportGeneration |
| AWS.M2.CARDDEMO.SYSTRAN | GDG Base | InterestandFeeCalculation |
| AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | VSAM KSDS | InterestandFeeCalculation, TransactionProcessingandPosting |
| AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | VSAM KSDS | StatementandReportGeneration |
| AWS.M2.CARDDEMO.TRANREPT | GDG Base | StatementandReportGeneration |
| AWS.M2.CARDDEMO.TRANSACT.DALY | GDG Base | StatementandReportGeneration |
| AWS.M2.CARDDEMO.TRANSACT.IMPORT | データセット | DataImportandExportOperations |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | VSAM KSDS | DataImportandExportOperations, InteractiveNavigationandScreenControl, TransactionProcessingandPosting |
| AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | VSAM KSDS | StatementandReportGeneration |
| AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS | VSAM-KSDS | StatementandReportGeneration |
| AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | VSAM KSDS | InteractiveNavigationandScreenControl, Unclassified |
| INPFILE | QSAM | DatabaseMaintenanceandSynchronization |