# Data Import and Export Operations — Requirements


## Global Preconditions

- All operations require valid input data and appropriate authorization.
- Processing constraints and scheduling dependencies are documented in the Job Dependencies section.


## 1. Multi-Source Data Export
As a batch operations team, I want customer, account, card, transaction, and cross-reference data exported from all source data stores into a unified export data store daily so that downstream import processes receive a complete, consistently formatted snapshot of all data categories.

**Restart/Recovery:** The export destination is fully recreated on each run (delete then define). If the export phase is interrupted, the export data store (AWS.M2.CARDDEMO.EXPORT.DATA) may contain a partial set of records with no automatic rollback. A full re-run is required to produce a complete export.

### Requirements

REQ-F-001: [Ubiquitous] The system shall delete the existing export data store (AWS.M2.CARDDEMO.EXPORT.DATA) if present and define a new keyed data store configured with a 4-byte key at offset 28, 10 primary and 5 secondary cylinders of storage, 10% free space, and concurrent read/write sharing before any export records are written.

REQ-F-002: [Ubiquitous] The system shall generate a single 26-character ISO 8601 timestamp at the start of each batch run by formatting the current system date as YYYY-MM-DD and the current system time as HH:MM:SS, concatenating them with a space separator and a '.00' suffix; this timestamp shall be applied to all export records produced during the run.

REQ-F-003: [Ubiquitous] The system shall open the customer store (AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS), account store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS), card cross-reference store (AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS), transaction store (AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS), and card store (AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS) for sequential input, and open the export data store (AWS.M2.CARDDEMO.EXPORT.DATA) for sequential output before processing begins.

REQ-F-004: [State-driven] While customer records remain available in the customer store, the system shall read each customer record sequentially, transform it into the standardized export format with record type 'C', the batch-run timestamp, an incremented sequence number, branch identifier '0001', and region code 'NORTH', and write the assembled export record to the export data store.

REQ-F-005: [State-driven] While account records remain available in the account store, the system shall read each account record sequentially, transform it into the standardized export format with record type 'A', the batch-run timestamp, an incremented sequence number, branch identifier '0001', and region code 'NORTH', and write the assembled export record to the export data store.

REQ-F-006: [State-driven] While cross-reference records remain available in the card cross-reference store, the system shall read each cross-reference record sequentially, transform it into the standardized export format with record type 'X', the batch-run timestamp, an incremented sequence number, branch identifier '0001', and region code 'NORTH', and write the assembled export record to the export data store.

REQ-F-007: [State-driven] While transaction records remain available in the transaction store, the system shall read each transaction record sequentially, transform it into the standardized export format with record type 'T', the batch-run timestamp, an incremented sequence number, branch identifier '0001', and region code 'NORTH', and write the assembled export record to the export data store.

REQ-F-008: [State-driven] While card records remain available in the card store, the system shall read each card record sequentially, transform it into the standardized export format with record type 'D', the batch-run timestamp, an incremented sequence number, branch identifier '0001', and region code 'NORTH', and write the assembled export record to the export data store.

REQ-F-009: [Ubiquitous] The system shall read customer, account, card, transaction, and cross-reference records from all five source data stores and write all consolidated export records to the export data store (AWS.M2.CARDDEMO.EXPORT.DATA) so that the downstream import process (CBIMPORT) receives a complete export.


### Open Questions

OQ-001: The export data store schema contains an `EXPORT-DATE`, `EXPORT-DATE-TIME-SEP`, and `EXPORT-TIME` as separate fields in addition to the 26-character `EXPORT-TIMESTAMP`. The rules describe only the combined 26-character timestamp. Clarification is needed on whether the separate date and time fields are populated independently or derived from the combined timestamp. — Owner: data architecture team

OQ-002: The rules specify branch identifier '0001' and region code 'NORTH' as fixed values applied to all export records regardless of data category. It is unclear whether these are configurable parameters or hard-coded constants that must be preserved exactly. — Owner: business operations team

OQ-003: The sequence number applied to each export record is described as incremented per record across all data categories. It is unclear whether the sequence restarts at each data category boundary or is a single monotonically increasing counter across the entire batch run. — Owner: data architecture team


---


## 2. Customer Data Import and File Splitting
As a batch operations team, I want customer export data read from a multi-record export file and split into normalized output stores so that customer, account, card cross-reference, and transaction data are available for downstream processing, with erroneous records captured for investigation.

**Data flow:** Reads the export data store (AWS.M2.CARDDEMO.EXPORT.DATA, written by CBEXPORT) and writes to the customer data store (AWS.M2.CARDDEMO.CUSTDATA.IMPORT), account data store (AWS.M2.CARDDEMO.ACCTDATA.IMPORT), card cross-reference store (AWS.M2.CARDDEMO.CARDXREF.IMPORT), transaction data store (AWS.M2.CARDDEMO.TRANSACT.IMPORT), and error output store (AWS.M2.CARDDEMO.IMPORT.ERRORS).

### Requirements

REQ-F-010: [Ubiquitous] The system shall read the multi-record export file, validate and parse each record, and distribute records to the appropriate output store based on record type: customer records to the customer data store, account records to the account data store, card cross-reference records to the card cross-reference store, transaction records to the transaction data store, and any rejected or erroneous records to the error output store.

REQ-F-011: [State-driven] While export records remain available in the export data store, the system shall repeatedly read and dispatch each export record to the appropriate type-specific handler, terminating when the end-of-file condition is reached.

REQ-F-012: [Event-driven] When an export record is read from the export data store, the system shall evaluate the record type indicator and dispatch to the appropriate handler: type 'C' (card) to card processing, type 'A' (account) to account processing, type 'X' (cross-reference) to cross-reference processing, type 'T' (transaction) to transaction processing, type 'D' (customer) to customer processing, and any other type to unknown-record processing.

REQ-F-013: [Event-driven] When an export record with record type 'A' is dispatched, the system shall map the export account fields — account identifier, active status, current balance, credit limit, cash credit limit, open date, expiration date, reissue date, current cycle credit, current cycle debit, address ZIP code, and group identifier — to the account data store and write the assembled record.

REQ-F-014: [Event-driven] When an export record with record type 'C' is dispatched, the system shall map all customer data fields from the export record to the customer data store and write the assembled customer record.

REQ-F-015: [Event-driven] When an export record with record type 'D' is dispatched, the system shall map the card number, account identifier, card verification value code, embossed name, expiration date, and active status from the export record to the card output store and write the assembled card record.

REQ-F-016: [Event-driven] When an export record with record type 'X' is dispatched, the system shall map the card number, customer identifier, and account identifier from the export record to the card cross-reference store and write the assembled cross-reference record.

REQ-F-017: [Event-driven] When an export record with record type 'T' is dispatched, the system shall map all export transaction fields to the transaction data store and write the assembled transaction record.

REQ-F-018: [Event-driven] When an export record with an unrecognized record type is encountered, the system shall capture the current timestamp, the unknown record type, the sequence number, and a descriptive message into an error record structure and write the formatted error record to the error output store.


### Open Questions

OQ-004: The rules describe record type 'C' as both 'card' (group 2) and 'customer' (group 4), and record type 'D' as both 'card data' (group 2) and 'customer' (group 4). The consolidated dispatch rule (group 6) treats 'C' as card and 'D' as customer. The correct mapping of type codes to record categories should be confirmed with the data owner to avoid misrouting. — Owner: data/integration team


---


## 3. Transaction Type and Category Data Extraction and Backup
As a batch operations team, I want transaction type and transaction category data backed up and refreshed from the database daily so that current reference data is available for downstream processing and prior versions are retained for recovery.

**Restart/Recovery:** Each step executes conditionally on the success of the prior step. If any step fails, subsequent steps are skipped, preserving the integrity of both the backup and the freshly extracted output datasets.

### Requirements

REQ-F-019: [Ubiquitous] The system shall copy the current transaction type file to a new generation data group member, preserving all records.

REQ-F-020: [Event-driven] When the transaction type file backup completes successfully, the system shall copy the current transaction category file to a new generation data group member, preserving all records.

REQ-F-021: [Event-driven] When the transaction category file backup completes successfully, the system shall delete the transaction type output dataset and the transaction category output dataset from the previous run.

REQ-F-022: [Event-driven] When the previous-run output cleanup step succeeds, the system shall retrieve all records from the transaction type data store (CARDDEMO.TRANSACTION_TYPE), concatenate the type code with the description and padding to produce a 60-character record, sort the records by type code, and write the results to the transaction type file.

REQ-F-023: [Event-driven] When the transaction type extraction step completes with a return code less than 4, the system shall retrieve all records from the transaction category data store (CARDDEMO.TRANSACTION_TYPE_CATEGORY), concatenate the type code, category code, category data padded to 50 characters, and 4 zero-padding characters to produce a 60-character record, sort the records by type code and category code, and write the results to the transaction category output dataset.


### Non-Functional Requirements

REQ-N-001: [State-driven] While any step in the extraction and backup sequence has failed, the system shall skip all subsequent steps so that partially completed state does not overwrite or corrupt backup or output datasets.


---



## Job Dependencies

Batch processing schedules and execution dependencies (source: Control-M).

### WEEKLY-TransactionTypesDBRefresh

**Schedule:** Weekly, completes by 23:00
**Recovery:** RERUN

| Step | Job |
|------|-----|
| 1 | MNTTRDB2 |
| 2 | TRANEXTR |

### Cross-Schedule Dependencies

- WEEKLY-TransactionTypesDBRefresh/MNTTRDB2 → WEEKLY-DisclosureGroupsRefresh/CLOSEFIL

