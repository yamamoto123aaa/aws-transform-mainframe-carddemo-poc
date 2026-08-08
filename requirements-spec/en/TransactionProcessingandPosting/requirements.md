# Transaction Processing and Posting — Requirements


## Global Preconditions

- All operations require valid input data and appropriate authorization.
- Processing constraints and scheduling dependencies are documented in the Job Dependencies section.


## 1. Daily Transaction Card Verification and Account Lookup
As a batch operations team, I want daily transaction records validated against card cross-reference and account data so that only transactions with verified cards and existing accounts are processed, and unverifiable transactions are flagged for investigation.

**Restart/Recovery:** This job reads the Dalytran-file data store sequentially and performs read-only lookups against the Xref-file data store and Account-file data store. No writes are performed; the job may be restarted from the beginning without side effects.

### Requirements

REQ-F-001: [Ubiquitous] The system shall open the Dalytran-file data store for sequential input, the Xref-file data store for random-access input, and the Account-file data store for random-access input before processing any transaction records.

REQ-F-002: [State-driven] While daily transaction records remain available in the Dalytran-file data store, the system shall retrieve each transaction record sequentially and use its card number to perform a cross-reference lookup in the Xref-file data store to obtain the associated account identifier.

REQ-F-003: [Event-driven] When a transaction record is successfully read from the Dalytran-file data store, the system shall copy the card number from the transaction record to the cross-reference lookup key and initiate a lookup in the Xref-file data store.

REQ-F-004: [Event-driven] When a card number lookup is performed against the Xref-file data store and the record is found, the system shall populate the cross-reference record with the associated account identifier and customer identifier.

REQ-F-005: [Event-driven] When a card number lookup is performed against the Xref-file data store and the record is not found, the system shall set the cross-reference read status to 4, log an error message containing the card number and transaction identifier, and skip the transaction.

REQ-F-006: [Event-driven] When the cross-reference read status is 0 (successful lookup), the system shall copy the account identifier from the cross-reference record to the account lookup key and retrieve the account record from the Account-file data store.

REQ-F-007: [Event-driven] When an account identifier lookup is performed against the Account-file data store and the record is found, the system shall populate the account record and continue processing.

REQ-F-008: [Unwanted] If the account read status is not equal to 0, the system shall display an error message containing the text 'ACCOUNT ', the account identifier, and ' NOT FOUND'.

REQ-F-009: [Event-driven] When a read of the Dalytran-file data store reaches end of file, the system shall set the application result to end-of-file (16), set the end-of-daily-transaction-file flag to 'Y', and terminate the processing loop.

REQ-F-010: [Event-driven] When a read of the Dalytran-file data store encounters an I/O error (any file status other than success or end-of-file), the system shall set the application result to error (12).


---


## 2. Daily Transaction Validation and Posting
As a batch operations team, I want daily transaction records validated against card and account data and posted to account balances so that approved transactions are reflected in account balances and category totals, and rejected transactions are captured with their failure reasons for investigation.

**Restart/Recovery:** The posting phase updates the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS), the transaction category balance data store (AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS), and the transaction archive (AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS) in place. No rule items describe automatic rollback; if interrupted, partial updates may exist.

### Requirements

REQ-F-011: [Ubiquitous] The system shall open the daily transaction input data store (AWS.M2.CARDDEMO.DALYTRAN.PS), the card cross-reference data store (AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS), the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS), the transaction category balance data store (AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS), the transaction archive data store (AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS), and the daily rejection data store (AWS.M2.CARDDEMO.DALYREJS) for processing before any transaction records are read.

REQ-F-012: [State-driven] While transaction records remain available in the daily transaction input data store, the system shall retrieve the next transaction record, validate it, and either post it or write it to the daily rejection data store; processing shall continue until end-of-file is reached.

REQ-F-013: [Event-driven] When a transaction record is read successfully, the system shall set the retrieval result to success (0); when end-of-file is reached, the system shall set the retrieval result to 16 and set the end-of-file flag to 'Y'; when any other read error occurs, the system shall set the retrieval result to 12.

REQ-F-014: [Event-driven] When a transaction record is ready for validation, the system shall look up the card number (16-character alphanumeric) in the card cross-reference data store; if the card number is not found, the system shall set the validation failure reason to 100 ('INVALID CARD NUMBER FOUND').

REQ-F-015: [Event-driven] When the card number is found in the card cross-reference data store, the system shall retrieve the associated account ID (11-digit numeric) and look up the account record in the account data store; if the account is not found, the system shall set the validation failure reason to 101 ('ACCOUNT RECORD NOT FOUND').

REQ-F-016: [Event-driven] When the account record is found, the system shall compute a temporary balance as current cycle credit minus current cycle debit plus the transaction amount; if this temporary balance exceeds the account's credit limit, the system shall set the validation failure reason to 102 ('OVERLIMIT TRANSACTION').

REQ-F-017: [Event-driven] When the account record is found and the credit limit check passes, the system shall compare the account expiration date against the date portion (first 10 characters) of the transaction's original timestamp; if the account expiration date is earlier than the transaction date, the system shall set the validation failure reason to 103 ('TRANSACTION RECEIVED AFTER ACCT EXPIRATION').

REQ-F-018: [Event-driven] When all validation checks pass, the system shall set the validation failure reason to 0.

REQ-F-019: [Event-driven] When a transaction fails validation, the system shall assemble a rejection record containing the full transaction data and the validation failure reason code and description, then write the rejection record to the daily rejection data store (AWS.M2.CARDDEMO.DALYREJS).

REQ-F-020: [Event-driven] When a transaction passes all validation checks and is ready to be posted, the system shall attempt to read the transaction category balance record from the transaction category balance data store using the composite key of account ID (11-digit numeric), transaction type code (2-character alphanumeric), and transaction category code (4-digit numeric); if no record exists, the system shall create a new record with the transaction amount as the initial category balance; if a record exists, the system shall add the transaction amount to the existing category balance and rewrite the updated record.

REQ-F-021: [Event-driven] When a transaction passes all validation checks, the system shall add the transaction amount to the account's current balance; if the transaction amount is zero or positive, the system shall add it to the current cycle credit; if the transaction amount is negative, the system shall add its absolute value to the current cycle debit; the system shall then rewrite the updated account record to the account data store.

REQ-F-022: [Event-driven] When a transaction is approved and account balances have been updated, the system shall assemble the transaction archive record by copying the transaction ID (16-character alphanumeric), type code (2-character alphanumeric), category code (4-digit numeric), source (10-character alphanumeric), description (100-character alphanumeric), amount (11-digit decimal), merchant ID (9-digit numeric), merchant name (50-character alphanumeric), merchant city (50-character alphanumeric), merchant ZIP code (10-character alphanumeric), and card number (16-character alphanumeric) from the daily transaction record, and shall obtain the current system date and time formatted as a DB2-compatible timestamp string (with hyphens as date separators, dots as time separators, and a '0000' suffix) and store it as the processing timestamp.

REQ-F-023: [Event-driven] When the transaction archive record has been assembled, the system shall write it to the transaction archive data store (AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS).


### Open Questions

OQ-001: The validation failure reason codes 100–103 are defined in the rules. Are there additional failure reason codes defined elsewhere in the system that this job must also handle? — Owner: business/domain team

OQ-002: The rules state that the temporary balance check uses "current cycle credit minus current cycle debit plus transaction amount." For debit transactions (negative amounts), this means the temporary balance decreases. Should the credit limit check apply only to credit transactions, or to all transaction types? — Owner: business/domain team

OQ-003: No rule item describes rollback or compensating logic if the write to the transaction archive fails after the account data store has already been updated. Should partial-update recovery be addressed? — Owner: architecture/operations team


---


## 3. Transaction Consolidation and Indexing
As a batch operations team, I want transaction records from the backup store and the system-generated store consolidated, sorted, and loaded into the transaction master index daily so that all transactions are available for indexed retrieval by transaction identifier.

### Requirements

REQ-F-024: [Ubiquitous] The system shall read transaction records from the transaction backup store and the system-generated transaction store, consolidate them into a single dataset, sort the consolidated records by transaction identifier in ascending order, and write the sorted result to the combined transaction dataset.

REQ-F-025: [Ubiquitous] The system shall copy the consolidated and sorted transaction records from the combined transaction dataset into the transaction master index store, making the records available for indexed retrieval by transaction identifier.


---


## 4. Daily Transaction Posting and Balance Update
As a batch operations team, I want daily transaction records validated and posted to account balances so that accepted transactions are permanently recorded and rejected transactions are captured with their failure reasons for investigation.

**Restart/Recovery:** The posting phase updates the account file (Account-file data store) and transaction category balance file (Tcatbal-file data store) in place. If interrupted, partial updates may exist with no automatic rollback.

### Requirements

REQ-F-026: [Ubiquitous] The system shall open the daily transaction data store, the Xref-file data store, the Account-file data store, and the Tcatbal-file data store for processing before reading any transaction records.

REQ-F-027: [Ubiquitous] The system shall open the Transact-file data store for writing accepted transactions and the Fd-rejs-record data store for writing rejected transactions before processing begins.

REQ-F-028: [State-driven] While transaction records remain available in the daily transaction data store, the system shall retrieve the next transaction record, validate it, and either post it or write it to the rejection output; processing shall repeat until end-of-file is reached.

REQ-F-029: [Event-driven] When a transaction record is read from the daily transaction data store and the read succeeds, the system shall set the retrieval result to success (0); when end-of-file is reached, the system shall set the retrieval result to 16 and set the end-of-file flag to 'Y'; when any other read error occurs, the system shall set the retrieval result to 12.

REQ-F-030: [Event-driven] When a transaction record is ready for validation, the system shall look up the card number in the Xref-file data store; if the card number is not found, the system shall set the validation failure reason to 100 ('INVALID CARD NUMBER FOUND').

REQ-F-031: [Event-driven] When the card number is found in the Xref-file data store, the system shall look up the associated account record in the Account-file data store using the retrieved account ID; if the account is not found, the system shall set the validation failure reason to 101 ('ACCOUNT RECORD NOT FOUND').

REQ-F-032: [Event-driven] When the account record is found, the system shall compute a temporary balance as current cycle credit minus current cycle debit plus the transaction amount; if this temporary balance exceeds the account's credit limit, the system shall set the validation failure reason to 102 ('OVERLIMIT TRANSACTION').

REQ-F-033: [Event-driven] When the account record is found and the credit limit check passes, the system shall compare the account expiration date to the date portion of the transaction's original timestamp; if the expiration date is earlier than the transaction date, the system shall set the validation failure reason to 103 ('TRANSACTION RECEIVED AFTER ACCT EXPIRATION').

REQ-F-034: [Event-driven] When all validation checks pass, the system shall set the validation failure reason to 0 and proceed to post the transaction.

REQ-F-035: [Event-driven] When a transaction passes validation and is ready to be posted, the system shall attempt to read the transaction category balance record from the Tcatbal-file data store using the composite key of account ID, transaction type code, and transaction category code; if no record exists, the system shall create a new record with the transaction amount as the initial category balance; if a record exists, the system shall add the transaction amount to the existing category balance and rewrite the updated record.

REQ-F-036: [Event-driven] When a transaction is posted to the Tcatbal-file data store, the system shall add the transaction amount to the account's current balance in the Account-file data store; if the transaction amount is zero or positive, the system shall also add it to the current cycle credit field; if the transaction amount is negative, the system shall add its absolute value to the current cycle debit field; the system shall then rewrite the updated account record.

REQ-F-037: [Ubiquitous] The system shall assemble the transaction record for archiving by copying the transaction ID, type code, category code, source, description, amount, merchant ID, merchant name, merchant city, merchant ZIP code, and card number from the daily transaction record, and by obtaining the current system date and time formatted as a DB2-compatible timestamp (date separators: hyphens; time separators: dots; suffix: '0000') to store as the processing timestamp.

REQ-F-038: [Event-driven] When the transaction record is assembled, the system shall write it to the Transact-file data store.

REQ-F-039: [Event-driven] When a transaction fails validation, the system shall assemble a rejection record by copying the transaction data and the validation trailer (containing the failure reason code and description), then write the rejection record to the Fd-rejs-record data store.


### Non-Functional Requirements

REQ-N-001: [Unwanted] If the posting job is interrupted after partial updates to the Account-file data store or Tcatbal-file data store, the system shall not automatically roll back partial updates; the interrupted state must be detectable for manual recovery.


### Open Questions

OQ-004: The rules describe two overlapping validation flows (groups 1–3 of CBTRN02C) with slightly different field names for the same checks (e.g., `ACCT-CURR-BAL` vs. generic current balance). Are these two separate code paths or a single canonical validation sequence? — Owner: transaction processing team

OQ-005: The rules do not specify the behavior when a read error (result code 12) occurs on the daily transaction data store — should processing halt, skip the record, or write to the rejection file? — Owner: batch operations team


---



## Job Dependencies

Batch processing schedules and execution dependencies (source: Control-M).

### MONTHLY-InterestCalculation

**Schedule:** Monthly, completes by 23:00
**Recovery:** RERUN

| Step | Job |
|------|-----|
| 1 | INTCALC |
| 2 | COMBTRAN |

