# Statement and Report Generation — Requirements


## Global Preconditions

- All operations require valid input data and appropriate authorization.
- Processing constraints and scheduling dependencies are documented in the Job Dependencies section.


## 1. Customer Account Statement Generation
As a batch operations team, I want customer account statements generated from card cross-reference, customer, account, and transaction data so that each cardholder receives a complete, itemized statement reflecting their identity, account balance, FICO score, and transaction activity.

**Restart/Recovery:** This job reads four input data stores and writes to two output stores. There are no described transaction boundaries, rollback, or commit semantics; partial output may exist if the job is interrupted.

### Requirements

REQ-F-001: [Event-driven] When the statement generation job executes, the system shall open the statement output store and the HTML output store for writing, and initialize the in-memory transaction table and its counters to empty.

REQ-F-002: [Event-driven] When the transaction file is opened, the system shall read the first transaction record, save its card number as the reference card number, set the credit record counter to 1, and set the transaction record counter to 0.

REQ-F-003: [State-driven] While the transaction read operation is active, the system shall read each transaction record sequentially from the transaction data store, group transactions by card number into the in-memory transaction table, increment the transaction record counter for each record belonging to the same card, and advance the credit record counter and reset the transaction record counter to 1 when a new card number is encountered; for each record, the system shall store the card number, transaction identifier, and transaction detail at the appropriate position in the table.

REQ-F-004: [Event-driven] When the end of the transaction data store is reached during table loading, the system shall store the current transaction record counter as the transaction count for the last card slot in the table, and signal that cross-reference record reading should begin next.

REQ-F-005: [State-driven] While the end-of-file indicator is not 'Y', the system shall read the next cross-reference record sequentially; when a valid cross-reference record is retrieved, the system shall perform a keyed lookup of the customer record using the customer identifier from the cross-reference record and a keyed lookup of the account record using the account identifier from the cross-reference record.

REQ-F-006: [Event-driven] When a cross-reference record is read successfully, the system shall set the end-of-file indicator to 'Y' when the cross-reference data store is exhausted, and otherwise populate the card cross-reference record with the card number, customer identifier, and account identifier for downstream processing.

REQ-F-007: [Event-driven] When customer and account records have been retrieved for the current cross-reference record, the system shall assemble the customer's full name by concatenating first name, middle name, and last name (each space-delimited), copy address line 1 and address line 2 directly into the statement, assemble address line 3 by concatenating address line 3, state code, country code, and ZIP code (each space-delimited), and populate the account identifier, current balance, and FICO credit score display fields; the system shall then write the header, name, address lines, separators, section headers, account detail lines, and column header lines to the statement output store.

REQ-F-008: [Event-driven] When the statement header has been written and the transaction total accumulator has been reset to zero, the system shall scan the in-memory transaction table for all entries matching the current card number, write each matching transaction detail line to the statement output store, and accumulate the transaction amount for each matched transaction into the running total.

REQ-F-009: [Event-driven] When a transaction matching the current card number is identified in the in-memory transaction table, the system shall populate the transaction identifier, description, and amount display fields from the transaction record and write the transaction detail line to the statement output store.

REQ-F-010: [Event-driven] When all matching transactions for the current card have been written, the system shall write a separator line, a total transaction line displaying the accumulated transaction total, and a statement footer line to the statement output store.

REQ-F-011: [Event-driven] When all cross-reference records have been processed, the system shall close the transaction data store, the cross-reference data store, the customer data store, and the account data store in sequence.


### Open Questions

OQ-001: The noise_context rule (7dec4675) states that the HTML output store is opened for writing at initialization, but no rule item describes what content is written to it or when. Should HTML output generation be treated as a parallel output path producing the same statement content, or is it a separate format with distinct rules? — Owner: business/product team


---


## 2. Multi-File Record Retrieval Dispatcher
As a batch operations team, I want a centralized file access dispatcher to open, read, and close transaction, cross-reference, customer, and account data stores on behalf of the statement and report generation process so that all file operations are routed consistently and their results are returned to the caller.

### Requirements

REQ-F-012: [Ubiquitous] The system shall evaluate the data definition identifier supplied in the request control area and route the file operation request to the corresponding file-processing procedure for one of four supported data stores: the transaction data store (`TRNX-FILE`), the cross-reference data store (`XREF-FILE`), the customer data store (`CUST-FILE`), or the account data store (`ACCT-FILE`).

REQ-F-013: [Unwanted] If the data definition identifier does not match any of the four supported values, the system shall take no action and return control to the caller.

REQ-F-014: [Event-driven] When a transaction data store operation is requested, the system shall perform the specified operation — open for input, sequential read of the next record, or close — on the transaction data store and return the resulting file status code to the caller.

REQ-F-015: [Event-driven] When a cross-reference data store operation is requested, the system shall perform the specified operation — open for input, sequential read of the next record, or close — on the cross-reference data store (AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS) and return the resulting file status code to the caller.

REQ-F-016: [Event-driven] When a customer data store operation is requested, the system shall perform the specified operation — open for input, keyed read by customer ID (9-digit numeric), or close — on the customer data store (AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS) and return the resulting file status code to the caller.

REQ-F-017: [Event-driven] When a keyed read is requested on the customer data store, the system shall extract the access key from the request control area using the supplied key length indicator, use it as the customer ID to retrieve the matching customer record, and return the record to the caller.

REQ-F-018: [Event-driven] When an account data store operation is requested, the system shall perform the specified operation — open for input, keyed read by account ID (11-digit numeric), or close — on the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) and return the resulting file status code to the caller.

REQ-F-019: [Event-driven] When a keyed read is requested on the account data store, the system shall extract the access key from the request control area using the supplied key length indicator, use it as the account ID to retrieve the matching account record, and return the record to the caller.


---


## 3. Transaction Report Generation with Multi-Level Totals
As a batch operations team, I want transaction records enriched with account, type, and category information and written to a formatted report with page, account, and grand totals so that business analysts have a complete, organized view of transaction activity for a given reporting period.

**Restart/Recovery:** This job produces a report output; if interrupted, the report must be regenerated from the beginning. No partial-update state is maintained in source data stores.

### Requirements

REQ-F-020: [Ubiquitous] The system shall open the transaction data store (TRANSACT.DALY), card cross-reference data store (CARDXREF.VSAM.KSDS), transaction type data store (TRANTYPE.VSAM.KSDS), transaction category data store (TRANCATG.VSAM.KSDS), and date-parameters data store for input, and the transaction report data store (TRANREPT) for output before processing begins.

REQ-F-021: [Event-driven] When the date-parameters data store is read, the system shall retrieve the report start date and report end date to govern the reporting period.

REQ-F-022: [Event-driven] When the first transaction record is processed, the system shall record that initialization is complete, move the report start date and report end date to the report name header, and write the report header section to the transaction report data store.

REQ-F-023: [Ubiquitous] The system shall write the report header section to the transaction report data store consisting of: the report name header (containing the report start date and report end date), a blank line, the transaction column header line, and a separator line, and shall increment the line counter by four.

REQ-F-024: [State-driven] While the end-of-file flag is 'N', the system shall read the next transaction record from the transaction data store and process it.

REQ-F-025: [Event-driven] When a transaction record is read successfully, the system shall set the application result code to 0.

REQ-F-026: [Event-driven] When end-of-file is reached on the transaction data store, the system shall set the application result code to 16 and the end-of-file flag to 'Y'.

REQ-F-027: [Event-driven] When a file error occurs reading the transaction data store, the system shall set the application result code to 12.

REQ-F-028: [Event-driven] When the card number on the current transaction differs from the previously processed card number and this is not the first transaction, the system shall write the accumulated account total for the previous card number, update the current card number to the new card number, and perform lookups for account ID, transaction type description, and transaction category description.

REQ-F-029: [Event-driven] When a card number lookup is required, the system shall read the card cross-reference data store using the card number (16-character alphanumeric) as the key to retrieve the associated account ID (11-digit numeric).

REQ-F-030: [Event-driven] When a transaction type lookup is required, the system shall read the transaction type data store using the transaction type code (2-character alphanumeric) as the key to retrieve the associated transaction type description.

REQ-F-031: [Event-driven] When a transaction category lookup is required, the system shall read the transaction category data store using the composite key of transaction type code (2-character alphanumeric) and transaction category code (4-digit numeric) to retrieve the associated transaction category description.

REQ-F-032: [Ubiquitous] The system shall write a transaction detail line to the transaction report data store populated with the transaction ID, account ID, transaction type code and description, transaction category code and description, transaction source, and transaction amount, and shall increment the line counter by one.

REQ-F-033: [Ubiquitous] The system shall add the transaction amount to both the page total accumulator and the account total accumulator for each transaction detail line written.

REQ-F-034: [Event-driven] When the line counter reaches a multiple of 20 lines, the system shall write the page total, reset the page total accumulator to zero, and write the report header section for the new page.

REQ-F-035: [Ubiquitous] The system shall write the page total line to the transaction report data store by moving the accumulated page total amount to the page total report record, add the page total to the grand total accumulator, reset the page total accumulator to zero, write a separator line, and increment the line counter by two.

REQ-F-036: [Ubiquitous] The system shall write the account total line to the transaction report data store by moving the accumulated account total amount to the account total report record, reset the account total accumulator to zero, write a separator line, and increment the line counter by two.

REQ-F-037: [Event-driven] When end-of-file is reached on the transaction data store, the system shall add the final transaction amount to the page total and account total accumulators, write the final page total, and write the grand total.

REQ-F-038: [Ubiquitous] The system shall write the grand total line to the transaction report data store by moving the accumulated grand total amount to the grand total report record.

REQ-F-039: [Event-driven] When the date-parameters data store read succeeds, the system shall set the application result code to 0.

REQ-F-040: [Event-driven] When end-of-file is reached on the date-parameters data store, the system shall set the application result code to 16 and the end-of-file flag to 'Y'.

REQ-F-041: [Event-driven] When a file error occurs reading the date-parameters data store, the system shall set the application result code to 12.


---


## 4. Card Transaction Statement Generation
As a batch operations team, I want card transaction statements generated from sorted transaction data enriched with card cross-reference, account, and customer information so that customers receive accurate, complete statements in both text and HTML formats.

**Restart/Recovery:** Previous statement output files are deleted before each run. The transaction cluster is deleted and recreated at the start of each run. If any step fails, subsequent steps do not execute.

### Requirements

REQ-F-042: [Ubiquitous] The system shall delete the existing keyed transaction data store and define a new indexed transaction data store with a 32-byte key, 350-byte fixed records, concurrent-read/exclusive-write sharing, and 1 primary plus 5 secondary cylinders of storage.

REQ-F-043: [Ubiquitous] The system shall sort transaction records from the source transaction data store by card number (16 bytes at offset 263, ascending) as the primary key and transaction ID (16 bytes at offset 1, ascending) as the secondary key, reconstructing each output record with card number in positions 1–16, transaction ID in positions 17–278, and a 50-byte segment from offset 279, and write the sorted records to the sequential transaction data store.

REQ-F-044: [Event-driven] When the copy step completes successfully, the system shall delete the HTML statement output file and the text statement output file from the previous run.

REQ-F-045: [Event-driven] When the cleanup step completes successfully, the system shall open the transaction file (transaction file data store), the card cross-reference file (Xreffile-file data store), the customer master file (customer file data store), and the account file (account file data store) in preparation for statement generation.

REQ-F-046: [Event-driven] When a transaction record is read, the system shall store the transaction identifier and detail data at the current card and transaction position in the in-memory transaction table; if the card number matches the previously saved card number, the system shall increment the transaction record counter, otherwise the system shall store the current transaction count for the current card slot, advance the credit record counter to the next card slot, reset the transaction record counter to 1, and on end-of-file invoke the exit routine to finalize the last card's transaction count.

REQ-F-047: [Event-driven] When the end of the transaction file is reached during table loading, the system shall record the transaction count for the last card slot in the in-memory table and transition to reading cross-reference records.

REQ-F-048: [State-driven] While cross-reference records remain available, the system shall read each cross-reference record sequentially and, for each record, perform a keyed lookup of the customer master file using the customer identifier and a keyed lookup of the account data store using the account identifier.

REQ-F-049: [Event-driven] When the end of the cross-reference file is reached, the system shall set the end-of-file indicator to 'Y' to terminate the statement generation loop.

REQ-F-050: [Event-driven] When customer and account records have been retrieved for a cross-reference record, the system shall assemble the statement header by constructing the customer full name from first, middle, and last name fields; populating address lines 1, 2, and 3 (including state code, country code, and ZIP code); and writing the header, name, address lines, separators, section headers, account identifier, current balance, FICO credit score, and column headers to the statement output store (Fd-stmtfile-rec data store).

REQ-F-051: [Event-driven] When the statement header has been written and the transaction total accumulator has been reset to zero, the system shall scan the in-memory transaction table for entries matching the current card number, write each matching transaction detail line containing the transaction identifier, description, and amount to the statement output store, accumulate the total transaction amount, and then write a separator line, a total transaction line showing the accumulated amount, and a statement footer line to the statement output store.

REQ-F-052: [Event-driven] When a transaction matching the current card number is identified in the in-memory table, the system shall populate the transaction identifier, description, and amount display fields from the current transaction record and write the transaction detail line to the statement output store.

REQ-F-053: [Event-driven] When all cross-reference records have been processed, the system shall close the transaction file, the cross-reference file, the customer master file, and the account data store.


---


## 5. Transaction Category Balance Report Generation
As a batch operations team, I want transaction category balance records retrieved, sorted, and formatted into a report so that account-level category balances are available for business analysis in a consistent, ordered format.

**Restart/Recovery:** This job executes two sequential phases. The first phase copies records to an intermediate store; the second phase sorts and reformats those records into the report output. If interrupted, the job must be restarted from the beginning, as no checkpoint mechanism is described.

### Requirements

REQ-F-054: [Ubiquitous] The system shall retrieve transaction category balance records from the Tcatbal-file data store and write them to an intermediate backup store in preparation for sorting and formatting.

REQ-F-055: [Ubiquitous] The system shall sort the transaction category balance records from the intermediate backup store in ascending order by account identifier, then by transaction type code, then by category code.

REQ-F-056: [Ubiquitous] The system shall reformat each sorted transaction category balance record to include the account identifier, transaction type code, category code, and balance amount — with the balance amount formatted to display a decimal point and trailing zeros suppressed — and write the reformatted records to the report dataset (Fd-tran-cat-bal-record data store).


---


## 6. Transaction Report Generation and Filtering
As a batch operations team, I want processed transaction records filtered by date range, sorted by card number, and enriched with reference data so that a formatted transaction report is produced for business analysis.

**Restart/Recovery:** This job processes data sequentially in three phases (backup, filter/sort, report generation). If interrupted, the backup phase is re-runnable from the source store. The report generation phase reads from the filtered daily transaction store; if interrupted, the report output store may be partially written with no automatic rollback.

### Requirements

REQ-F-057: [Ubiquitous] The system shall copy transaction records from the source transaction store to a backup dataset, preserving the original data structure and content, before any filtering or sorting operations are performed.

REQ-F-058: [Ubiquitous] The system shall filter the backed-up transaction records to include only those with a transaction processing date between 2022-01-01 and 2022-07-06 (inclusive), sort the filtered records in ascending order by card number, and write the sorted output to the daily transaction data store (DALYTRAN).

REQ-F-059: [Ubiquitous] The system shall open the transaction file (Transact-file data store), card cross-reference file (Xref-file data store), transaction type file (transaction type file), transaction category file (Trancatg-file data store), and date-parameters file (Date-parms-file data store) for input, and the report file for output, at the start of report generation processing.

REQ-F-060: [Event-driven] When the first transaction record is processed, the system shall set the first-time flag to 'N', move the report start and end dates from the date-parameters file to the report header, and write the report header lines to the report output store.

REQ-F-061: [Ubiquitous] The system shall write report header lines consisting of the report name header (containing the report start date and end date), a blank line, a transaction column header line, and a separator line, and shall increment the line counter by four.

REQ-F-062: [Event-driven] When a card number lookup is required, the system shall read the card cross-reference file (Xref-file data store) using the card number as the key to retrieve the associated account ID.

REQ-F-063: [Event-driven] When a transaction type lookup is required, the system shall read the transaction type file using the transaction type code as the key to retrieve the associated transaction type description.

REQ-F-064: [Event-driven] When a transaction category lookup is required, the system shall read the transaction category file (Trancatg-file data store) using the composite key of transaction type code and transaction category code to retrieve the associated transaction category description.

REQ-F-065: [Event-driven] When the card number of the current transaction differs from the previously processed card number and this is not the first transaction, the system shall write the accumulated account total for the previous card number, update the current card number to the new card number, and perform lookups for account ID, transaction type description, and transaction category description.

REQ-F-066: [Ubiquitous] The system shall populate each transaction detail line with the transaction ID, account ID, transaction type code and description, transaction category code and description, transaction source, and transaction amount, and write the record to the report output store, incrementing the line counter by one.

REQ-F-067: [Ubiquitous] The system shall add the transaction amount to both the page total accumulator and the account total accumulator for each transaction detail line written.

REQ-F-068: [Event-driven] When the line counter reaches a multiple of 20 lines, the system shall write the page total, reset the page total to zero, and write the report header lines for the new page.

REQ-F-069: [Ubiquitous] The system shall write the page total line by moving the accumulated page total amount to the page total report record, write the record to the report output store, add the page total to the grand total accumulator, reset the page total to zero, write a separator line, and increment the line counter by two.

REQ-F-070: [Ubiquitous] The system shall write the account total line by moving the accumulated account total amount to the account total report record, write the record to the report output store, reset the account total to zero, write a separator line, and increment the line counter by two.

REQ-F-071: [Event-driven] When end-of-file is reached on the transaction file, the system shall add the final transaction amount to the page total and account total accumulators, write the final page total, and write the grand total to the report output store.

REQ-F-072: [Ubiquitous] The system shall write the grand total line by moving the accumulated grand total amount to the grand total report record and writing the record to the report output store.

REQ-F-073: [State-driven] While the end-of-file flag is 'N', the system shall read the next transaction record from the Transact-file data store, detect card number changes, perform reference data lookups, write transaction detail lines, and handle page breaks; when end-of-file is reached, the system shall write the final page total and grand total.

REQ-F-074: [Event-driven] When the next transaction record is read from the Transact-file data store, the system shall set the application result code to 0 on a successful read, set it to 16 and the end-of-file flag to 'Y' when end-of-file is reached, and set it to 12 when a file error occurs.

REQ-F-075: [Event-driven] When the date-parameters file (Date-parms-file data store) is read, the system shall set the application result code to 0 on a successful read, set it to 16 and the end-of-file flag to 'Y' when end-of-file is reached, and set it to 12 when a file error occurs.


### Open Questions

OQ-002: The date range filter (2022-01-01 to 2022-07-06) is hardcoded in the rule. Should this range be driven by the date-parameters file at runtime, or is a fixed range the intended behavior for this job? — Owner: business/operations team


---
