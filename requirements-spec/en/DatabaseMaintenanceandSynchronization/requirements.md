# Database Maintenance and Synchronization — Requirements


## Global Preconditions

- All operations require valid input data and appropriate authorization.
- Processing constraints and scheduling dependencies are documented in the Job Dependencies section.


## 1. CICS Application Resource Definition and Installation
As a platform operations team, I want application resource definitions registered in the runtime configuration so that application programs, screen maps, and transaction identifiers are available for execution at runtime.

### Requirements

REQ-F-001: [Ubiquitous] The system shall invoke the configuration utility with read-write access to the configuration data store, with compatibility mode disabled, and process inline resource definitions from the input stream.

REQ-F-002: [Ubiquitous] The system shall register map set resources for login, account menu, account view, account update, and account deactivation within the CARDDEMO application group, assigning each map set a descriptive label identifying its business purpose.

REQ-F-003: [Ubiquitous] The system shall register application program resources for login, account management, card management, transaction processing, bill-pay, administration, and test functions within the CARDDEMO application group, associating each program with its transaction identifier and configuring dynamic allocation.

REQ-F-004: [Ubiquitous] The system shall register transaction routing entries that map transaction identifiers CCDM, CCT1, CCT2, CCT3, and CCT4 to their corresponding application programs within the CARDDEMO application group, and configure each transaction to accept any task data length for parameter passing.

REQ-F-005: [Ubiquitous] The system shall list all registered resources in the CARDDEMO application group upon completion of resource definition processing to verify successful installation and generate a configuration summary report.


---


## 2. Transaction Type Database Maintenance
As a batch operations team, I want transaction type records inserted, updated, and deleted in the transaction type data store based on an input file so that the database reflects the current set of valid transaction types.

### Requirements

REQ-F-006: [Ubiquitous] The system shall read transaction records sequentially from the input file, evaluate each record's type indicator, and perform the corresponding insert, update, or delete operation on the transaction type data store until the end of the input file is reached.

REQ-F-007: [Event-driven] When a record read is requested, the system shall retrieve the next record from the transaction input file into the input buffer, and set the end-of-file indicator to 'Y' when the end of the input file is reached.

REQ-F-008: [State-driven] While records remain available in the transaction input file, the system shall evaluate each record's type indicator and dispatch to the appropriate operation, then read the next record, continuing until the end of the input file is reached.

REQ-F-009: [Event-driven] When a transaction record with type indicator 'A' is ready for processing, the system shall insert the record's reference number and description into the transaction type data store (CARDDEMO.TRANSACTION_TYPE), storing the reference number in the TR_TYPE column and the description in the TR_DESCRIPTION column.

REQ-F-010: [Event-driven] When a transaction record with type indicator 'U' is ready for processing, the system shall update the matching record in the transaction type data store (CARDDEMO.TRANSACTION_TYPE), setting the TR_DESCRIPTION column to the input record's description where TR_TYPE matches the input record's reference number.

REQ-F-011: [Event-driven] When a transaction record with type indicator 'D' is processed, the system shall delete the matching transaction type record from the transaction type data store (CARDDEMO.TRANSACTION_TYPE) where the transaction type code matches the input record reference number.

REQ-F-012: [Event-driven] When a transaction record with type indicator '*' is ready for processing, the system shall skip the record and take no action against the transaction type data store.

REQ-F-013: [Event-driven] When a transaction record with a type indicator other than 'A', 'U', 'D', or '*' is ready for processing, the system shall take no action against the transaction type data store.


### Open Questions

OQ-001: The rules describe four type indicator values ('A', 'U', 'D', '*') and a catch-all "no action" branch, but do not specify whether unrecognized type indicators should be logged or flagged for investigation. Should unrecognized type indicators produce an error or warning output? — Owner: batch operations / data governance team


---


## 3. Database Maintenance and Synchronization
As a batch operations team, I want existing database plans and packages freed, the transaction-type table created, and reference data loaded into the transaction type and transaction category tables so that the database is in a consistent, fully initialized state for application use.

**Restart/Recovery:** The cleanup phase treats a return code of 8 (plans or packages do not exist) as a successful outcome. The table-load phase is sequenced: the transaction category table is loaded only if the transaction type lookup table load succeeds.

### Requirements

REQ-F-014: [Ubiquitous] The system shall invoke the plan and package cleanup utility to free existing database plans and packages prior to database creation, treating a return code of 8 (indicating no pre-existing plans or packages were found) as a successful outcome equivalent to a zero return code.

REQ-F-015: [Ubiquitous] The system shall invoke the table-creation utility to establish the transaction type data store (CARDDEMO.TRANSACTION_TYPE), reading table-definition control statements and utility parameters from cataloged datasets.

REQ-F-016: [Ubiquitous] The system shall load the transaction type data store (CARDDEMO.TRANSACTION_TYPE) from the transaction type file (TRANTYPE-FILE) input dataset by executing the SQL statements and data-load commands contained in the control dataset.

REQ-F-017: [Event-driven] When the transaction type data store load completes successfully, the system shall load the transaction category data store (CARDDEMO.TRANSACTION_TYPE_CATEGORY) from the transaction type backward data store (C-TR-TYPE-BACKWARD) input dataset by executing the SQL statements and data-load commands contained in the corresponding control dataset.

REQ-F-018: [Unwanted] If the transaction type data store load does not complete successfully, the system shall skip the transaction category data store load step.


---


## 4. Database Authorization Unload
As a batch operations team, I want the current state of authorization definitions extracted from the authorization database so that IMS recovery and audit functions have an accurate, up-to-date snapshot of authorization records.

### Requirements

REQ-F-019: [Ubiquitous] The system shall extract authorization records from the authorization database, applying conditional activation so that the database is activated only if required for the unload operation, and write the extracted records to the output dataset.


---


## 5. Pending Authorization Record Extraction and Insertion
As a batch operations team, I want pending authorization summary and detail records extracted from the authorization hierarchy and inserted into output data stores so that downstream systems have a consistent, complete copy of all pending authorization data.

### Requirements

REQ-F-020: [State-driven] While the end-of-root-segment indicator is not set to 'Y', the system shall repeatedly retrieve the next pending authorization summary record from the hierarchical authorization data store using a Get Next operation and process it.

REQ-F-021: [Event-driven] When a pending authorization summary record is successfully retrieved (status SPACES), the system shall validate the account identifier on that record before proceeding to insert it or process its child records.

REQ-F-022: [Event-driven] When a pending authorization summary record passes account identifier validation, the system shall insert the pending authorization summary record into the output data store.

REQ-F-023: [Event-driven] When the end-of-root-segment status ('GB') is returned during authorization summary retrieval, the system shall set the end-of-root-segment indicator to 'Y' to terminate root-level processing.

REQ-F-024: [State-driven] While the end-of-child-segment indicator is not set to 'Y', the system shall repeatedly retrieve the next pending authorization detail record associated with the current authorization summary using a Get Next within Parent operation and process it.

REQ-F-025: [Event-driven] When a pending authorization detail record is successfully retrieved (status SPACES), the system shall insert the pending authorization detail record into the output data store.

REQ-F-026: [Event-driven] When the end-of-child-segment status ('GE') is returned during authorization detail retrieval, the system shall set the end-of-child-segment indicator to 'Y' to signal that all child-level authorization records for the current parent have been processed.


---


## 6. Payment Audit Database Load
As a batch operations team, I want payment audit root and child segment records loaded into the IMS authorization database so that the database reflects the current state of payment audit data unloaded from upstream sources.

**Restart/Recovery:** This job performs bulk IMS database insertions. If interrupted, partial inserts may exist with no automatic rollback. Restart requires investigation of the insertion state before re-execution.

### Requirements

REQ-F-027: [Ubiquitous] The system shall open the root segment input file and the child segment input file for sequential read access.

REQ-F-028: [State-driven] While records remain in the root segment input file (Infile1 data store), the system shall read each root segment record sequentially, stage the record data as a pending authorization summary, and insert it into the IMS authorization database as a root segment; when end-of-file is reached, the system shall cease root segment processing.

REQ-F-029: [Event-driven] When a root segment record is successfully read from the Infile1 data store, the system shall insert the record into the IMS authorization database using the Insert function, supplying the authorization program communication block and the pending authorization summary data.

REQ-F-030: [State-driven] While records remain in the child segment input file (Infile2 data store), the system shall read each child segment record sequentially and validate that the embedded root segment key is numeric; when end-of-file is reached, the system shall cease child segment processing.

REQ-F-031: [Event-driven] When a child segment record is successfully read from the Infile2 data store and its root segment key is numeric, the system shall move the key to the qualified segment search argument, stage the record data as pending authorization details, and retrieve the corresponding parent root segment from the IMS authorization database using the Get Unique function.

REQ-F-032: [Event-driven] When the parent root segment is successfully retrieved from the IMS authorization database, the system shall insert the child segment record into the IMS authorization database under that parent root segment using the Insert function, supplying the authorization program communication block and the pending authorization details data.

REQ-F-033: [Ubiquitous] The system shall execute the IMS batch message processing program to load payment audit records from the root input data store (INFILE1) and child input data store (INFILE2) into the IMS database, using the payment audit load PSB (PAUDBLOD) and the authentication PSB (PSBPAUTB), and produce diagnostic output to standard print and error streams.


### Open Questions

OQ-002: The rules do not describe the handling of a child segment record whose root segment key is non-numeric. Should such records be rejected, skipped, or written to an error output? — Owner: database maintenance team

OQ-003: The rules do not describe error handling when an IMS Insert operation fails (e.g., duplicate key, PCB status indicating error). Should the job abort, skip the record, or flag it for investigation? — Owner: database maintenance team

OQ-004: The rules do not describe error handling when the parent root segment retrieval (Get Unique) fails for a child record. Should the child record be skipped, the job aborted, or the record flagged? — Owner: database maintenance team


---


## 7. Database Transaction Table Maintenance
As a batch operations team, I want transaction type records added, updated, and deleted in the transaction type data store based on batch input so that the transaction reference data remains current and accurate.

**Restart/Recovery:** The job processes records sequentially from the input file and applies each operation directly to the transaction type data store. If interrupted, partial updates may exist with no automatic rollback.

### Requirements

REQ-F-034: [Ubiquitous] The system shall read transaction records sequentially from the input file, evaluate each record's type indicator, and perform the corresponding insert, update, or delete operation on the transaction type database table.

REQ-F-035: [State-driven] While records remain available in the Tr-record data store, the system shall retrieve each record sequentially and evaluate its operation code to determine the action to perform against the transaction type data store (`CARDDEMO.TRANSACTION_TYPE`), continuing until end of file is reached.

REQ-F-036: [Event-driven] When a transaction record with operation code 'A' is processed, the system shall insert a new record into the transaction type data store, populating the transaction type code with the input record reference number and the transaction description with the input record description.

REQ-F-037: [Event-driven] When a transaction record with type 'U' (update) is processed, the system shall update the transaction type record in the transaction type data store (`CARDDEMO.TRANSACTION_TYPE`), setting the description to the input record description where the transaction type matches the input record reference number.

REQ-F-038: [Event-driven] When a transaction record with operation code 'D' is processed, the system shall delete the record from the transaction type data store where the transaction type code matches the input record reference number.

REQ-F-039: [Event-driven] When a menu option is selected, the system shall restrict the options available to the user based on the user's authorization level, preventing access to functions the user is not authorized to use.

REQ-F-040: [Unwanted] If the user is not authorized for any menu option, the system shall display no selectable options to that user.

REQ-F-041: [Event-driven] When a record read is requested, the system shall retrieve the next record from the transaction record data store (TR-RECORD) into the input buffer, and set the last record indicator to 'Y' when end of file is reached.


---


## 8. Authorization Record Loading into Hierarchical Database
As a batch operations team, I want authorization root and child segment records loaded from sequential input files into the hierarchical authorization database so that the database reflects the full set of authorization records with referential integrity preserved between parent and child segments.

### Requirements

REQ-F-043: [State-driven] While records remain in the root segment input file, the system shall read each root segment record sequentially and insert it directly into the authorization database as a root segment.

REQ-F-044: [Event-driven] When a root segment record is successfully read from the root segment input file, the system shall insert that record into the authorization database using an insert operation.

REQ-F-045: [Event-driven] When the root segment input file reaches end-of-file, the system shall cease root segment processing.

REQ-F-046: [State-driven] While records remain in the child segment input file, the system shall read each child segment record sequentially and validate that the embedded root segment key is numeric before proceeding with insertion.

REQ-F-047: [Unwanted] If the root segment key embedded in a child segment record is not numeric, the system shall not attempt to insert that child segment into the authorization database.

REQ-F-048: [Event-driven] When a child segment record with a numeric root segment key is successfully read, the system shall perform a qualified Get Unique lookup in the authorization database to retrieve the parent root segment identified by that key.

REQ-F-049: [Event-driven] When the parent root segment is successfully retrieved from the authorization database, the system shall insert the child segment record into the authorization database under that parent root segment.

REQ-F-050: [Unwanted] If the parent root segment retrieval does not succeed, the system shall not insert the child segment record.

REQ-F-051: [Event-driven] When the child segment input file reaches end-of-file, the system shall cease child segment processing.


### Open Questions

OQ-005: The rules do not describe handling for file status codes other than '00'/SPACES (success) and '10' (end-of-file) for either input file. What action should the system take when an unexpected file status is encountered during reading? — Owner: batch operations / data management team

OQ-006: The rules do not describe what happens when a root segment insert fails (e.g., duplicate key or database error). Should the record be skipped, the job aborted, or the record written to a reject store? — Owner: batch operations / data management team

OQ-007: The rules do not describe what happens when a child segment insert fails after a successful parent retrieval. Should the record be skipped or the job aborted? — Owner: batch operations / data management team


---


## 9. Pending Authorization Batch Extraction
As a batch operations team, I want pending authorization records extracted from the hierarchical database and written to output files so that downstream load processes receive complete, structured authorization data for further processing.

### Requirements

REQ-F-052: [Ubiquitous] The system shall open the authorization summary output file for sequential writing before the main processing loop begins.

REQ-F-053: [Ubiquitous] The system shall open the authorization detail output file for sequential writing before the main processing loop begins.

REQ-F-054: [State-driven] While the end-of-root-segment condition has not been reached, the system shall retrieve the next pending authorization summary (root segment) from the hierarchical database.

REQ-F-055: [Event-driven] When a root segment is successfully retrieved, the system shall validate that the account identifier is numeric; if the account identifier is numeric, the system shall write the summary record to the authorization summary output file and initiate child-segment processing for that account.

REQ-F-056: [Event-driven] When a root segment is successfully retrieved but the account identifier is not numeric, the system shall skip child-segment processing for that account.

REQ-F-057: [Event-driven] When the database signals end-of-data for root segments (status 'GB'), the system shall set the end-of-root-segment flag to terminate main processing.

REQ-F-058: [State-driven] While the end-of-child-segment condition has not been reached for the current account, the system shall retrieve the next authorization detail (child segment) associated with the current root segment.

REQ-F-059: [Event-driven] When a child segment is successfully retrieved, the system shall write the child segment data to the authorization detail output file.

REQ-F-060: [Event-driven] When the database signals end-of-child-group for the current parent account (status 'GE'), the system shall set the end-of-child-segment flag to terminate detail processing for that account and return to root-segment processing.


---


## 10. IMS Database Unload and Pending Authorization Record Extraction
As a batch operations team, I want pending authorization summary and detail records extracted from the IMS database hierarchy and written to output data stores so that downstream database maintenance processes have a consistent, complete copy of authorization data.

### Requirements

REQ-F-061: [Ubiquitous] The system shall invoke the IMS database unload program through the IMS runtime controller, allocating all required runtime libraries, database definition stores, and GSAM data resources before executing the unload process.

REQ-F-062: [State-driven] While the end-of-root-segment indicator is not set to 'Y', the system shall repeatedly retrieve the next pending authorization summary record and process it until all root-level authorization records have been exhausted.

REQ-F-063: [Event-driven] When a pending authorization summary record is successfully retrieved (status SPACES), the system shall validate the account identifier and, if valid, insert the authorization summary record into the output data store.

REQ-F-064: [Event-driven] When the end-of-root-segment status ('GB') is returned during authorization summary retrieval, the system shall set the end-of-root-segment indicator to 'Y' to terminate root-level processing.

REQ-F-065: [State-driven] While the end-of-child-segment indicator is not set to 'Y', the system shall repeatedly retrieve the next pending authorization detail record associated with the current authorization summary until all child-level records for that parent have been exhausted.

REQ-F-066: [Event-driven] When a pending authorization detail record is successfully retrieved (status SPACES), the system shall insert the authorization detail record into the output data store.

REQ-F-067: [Event-driven] When the end-of-child-segment status ('GE') is returned during authorization detail retrieval, the system shall set the end-of-child-segment indicator to 'Y' to terminate child-level processing for the current parent.


---


## 11. Authorization Database Unload to Sequential Files
As a batch operations team, I want the authorization database unloaded to sequential output files so that downstream load jobs receive a complete, structured extract of root and child authorization records.

**Data flow:** Reads the authorization database (hierarchical, DLI mode); writes root authorization summary records to the root output file and child authorization detail records to the child output file. Both output files are consumed by downstream load jobs.

### Requirements

REQ-F-068: [Ubiquitous] The system shall extract authorization database records in DLI mode using the PAUTBUNL unload specification with the disable option applied, writing root (summary) records to the root output file and child (detail) records to the child output file.

REQ-F-069: [State-driven] While the end-of-root-segment flag is not set, the system shall retrieve the next authorization summary (root) record from the authorization database.

REQ-F-070: [Event-driven] When an authorization summary record is successfully retrieved (database status is spaces), the system shall validate that the account identifier is numeric; if valid, write the summary record to the root output file and initiate retrieval of associated child records; if invalid, skip child-segment processing for that account.

REQ-F-071: [Event-driven] When end-of-data is reached on the root segment (database status 'GB'), the system shall set the end-of-root-segment flag to terminate root-level processing.

REQ-F-072: [State-driven] While the end-of-child-segment flag is not set, the system shall retrieve the next authorization detail (child) record within the current parent account.

REQ-F-073: [Event-driven] When an authorization detail record is successfully retrieved (database status is spaces), the system shall write the child segment data to the child output file.

REQ-F-074: [Event-driven] When end-of-child-group is reached (database status 'GE'), the system shall set the end-of-child-segment flag to terminate child-level processing for the current parent account and return to root-segment processing.


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

