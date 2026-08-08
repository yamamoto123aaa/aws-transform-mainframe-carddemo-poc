# Batch File and Resource Management — Requirements


## Global Preconditions

- All operations require valid input data and appropriate authorization.
- Processing constraints and scheduling dependencies are documented in the Job Dependencies section.


## 1. Card Data Store Alternate Index Lifecycle Management
As a batch operations team, I want the card data store's alternate index rebuilt from a clean state so that account-based lookups remain consistent and conflict-free after each execution.

### Requirements

REQ-F-001: [Event-driven] When the alternate index rebuild executes, the system shall remove the existing alternate index from the card data store if one is present, allowing the subsequent creation step to proceed without conflict; if no alternate index exists, the removal step shall be skipped without error.

REQ-F-002: [Ubiquitous] The system shall define a new alternate index on the card data store keyed on the account ID field (bytes 11–16 of the record), with non-unique key support to allow multiple card records per account, automatic upgrade enabled so the index is maintained when the primary store is updated, and 5-cylinder storage allocation.


---


## 2. Card Data Store Alternate Index Path Definition
As a batch operations team, I want an alternate index path defined so that applications can query card records by account ID through the alternate index.

### Requirements

REQ-F-003: [Ubiquitous] The system shall define a path named AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.PATH that references the alternate index AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX, enabling indexed access to card records through this alternate key.


---


## 3. CICS Region File State Coordination for Batch Access
As a batch operations team, I want CICS region files closed before batch processing and reopened afterward so that batch jobs have exclusive access to card data without interference from concurrent CICS transactions.

### Requirements

REQ-F-004: [Event-driven] When batch processing is about to begin, the system shall invoke the operator command interface to close the card data file (CARDDAT) and the card index file (CARDAIX) in the CICS region, enabling exclusive batch access.

REQ-F-005: [Event-driven] When batch processing has completed, the system shall invoke the operator command interface to open the card data file (CARDDAT) and the card index file (CARDAIX) in the CICS region, restoring online access to those files.


### Open Questions

OQ-001: The rules describe both a close operation and an open operation for CARDDAT and CARDAIX, but the sequencing dependency between these operations and the data store rebuild steps (REQ-F-001 through REQ-F-003) is not explicitly stated. Should the close always precede the rebuild and the open always follow it as a guaranteed ordering constraint? — Owner: batch operations / infrastructure team

OQ-002: The rule for the alternate index definition specifies "5-cylinder storage allocation." It is unclear whether this is a platform-specific storage sizing artifact that should be carried forward as a capacity constraint or whether it should be translated to an equivalent modern storage specification. — Owner: infrastructure / modernization team


---


## 4. Close Transaction Files for Batch Processing
As a batch operations team, I want the transaction processing region's shared files closed before batch execution begins so that the batch process has guaranteed exclusive access to those resources.

### Requirements

REQ-F-006: [Ubiquitous] The system shall close the transaction log file, the cross-reference index file, the account file, the account index file, and the user security store file in the transaction processing region to establish exclusive batch access before batch processing proceeds.


### Non-Functional Requirements

REQ-N-001: [State-driven] While the batch process is executing, the system shall maintain all five files in a closed state within the transaction processing region so that no concurrent online activity can access those resources.


### Open Questions

OQ-003: The rule references a physical store `ISFIN` but this identifier does not appear in the authoritative data entity vocabulary. Clarification is needed to map the five named files (transaction log, cross-reference index, account data, account index, user security store) to their canonical data store names. — Owner: data architecture team


---


## 5. Customer Data File Lifecycle Management for Batch Processing
As a batch operations team, I want the customer data file closed in the CICS region before batch processing begins and reopened afterward so that batch jobs have exclusive access to the file during processing and online users regain access when batch completes.

### Requirements

REQ-F-007: [Ubiquitous] The system shall submit a file-closure directive to the CICS region instructing it to close the customer file (CUSTFILE-FILE) to enable exclusive batch access.

REQ-F-008: [Ubiquitous] The system shall invoke the operator command facility to open the customer file (CUSTFILE-FILE) in the CICS region, allowing batch processing to proceed with direct file access.


### Open Questions

OQ-004: The file-closure directive is submitted asynchronously and the job does not validate whether the closure succeeds or fails before batch processing proceeds. Should the modernized system introduce a confirmation or polling mechanism to verify the file state before allowing batch access? — Owner: batch operations / CICS integration team

OQ-005: The noise-context rules describe a customer data store initialization sequence (delete existing store, define new indexed-sequential store, copy records from flat file source). These rules were classified as working storage initialization mechanics, but they may represent a business-meaningful data store rebuild operation. Should these be treated as functional requirements for a customer data store refresh process? — Owner: data management team


---


## 6. JCL Member Copy to Print Queue
As a batch operations team, I want a JCL member copied to the print queue with internal reader processing so that the member is available for job submission or archival.

### Requirements

REQ-F-009: [Ubiquitous] The system shall read each record from the source JCL member and write it unchanged to print class A with internal reader processing enabled.


---


## 7. Open Shared Files for Exclusive Batch Access
As a batch operations team, I want the transaction, cross-reference, account, cross-account index, and user security files opened in the CICS region before batch processing begins so that batch jobs can access these shared resources without contention from online users.

### Requirements

REQ-F-010: [Ubiquitous] The system shall send operator commands to the CICS region to open the transaction file (TRANSACT), cross-reference file (CCXREF), account data file (ACCTDAT), cross-account index file (CXACAIX), and user security file (USRSEC), marking each file with open status to enable batch access.


### Open Questions

OQ-006: The rule references five specific files (TRANSACT, CCXREF, ACCTDAT, CXACAIX, USRSEC) that must be opened in the CICS region. In a modernized environment without a CICS region, what is the equivalent mechanism for coordinating exclusive batch access to these shared data stores? — Owner: architecture/modernization team

OQ-007: The rule states files are marked with "open status" to signal readiness for batch processing. Should the modernized system maintain an explicit file-availability status flag that downstream batch jobs check before proceeding? — Owner: batch operations/architecture team


---


## 8. Transaction File Lifecycle Management for Batch Processing
As a batch operations team, I want transaction files in the CICS region closed before batch processing begins and reopened afterward so that the batch job has exclusive access to transaction data without interference from online activity.

### Requirements

REQ-F-011: [Event-driven] When the batch job executes the file-closure phase, the system shall issue operator commands to the CICS region to close the TRANSACT file and the CXACAIX file, granting the batch job exclusive access to those resources.

REQ-F-012: [Ubiquitous] The system shall open the TRANSACT file and the CXACAIX file in the CICS region via operator commands, making both files available for exclusive batch processing.


### Open Questions

OQ-008: The rules describe both a close phase and an open phase for the same two files (TRANSACT and CXACAIX), but the ordering relationship between these phases and the intervening batch processing steps is not explicitly stated. Should the requirements specify that the close must precede all data processing steps and the open must follow them? — Owner: batch operations team


---


## 9. Transaction Timestamp Index Rebuild
As a batch operations team, I want the alternate index on the transaction master file rebuilt from scratch so that efficient lookups by processed timestamp remain accurate and consistent with the current state of the transaction data.

### Requirements

REQ-F-013: [Ubiquitous] The system shall delete the existing alternate index on the transaction master file before creating a new one, ensuring no conflicts arise from a prior index definition.

REQ-F-014: [Ubiquitous] The system shall create a new alternate index on the transaction master file keyed by processed timestamp, defined as non-unique to allow multiple transactions to share the same timestamp, with automatic maintenance enabled so that the index is updated whenever the base transaction cluster is modified.


---


## 10. Alternate Index Path Definition for Transaction Records
As a batch operations team, I want an alternate index path defined that links the transaction alternate index to its base cluster so that applications can route queries through the alternate index when accessing transaction records.

### Requirements

REQ-F-015: [Ubiquitous] The system shall define an alternate index path that establishes the logical relationship between the transaction alternate index and the transaction base cluster, enabling indexed access to transaction records through the alternate index.


---


## 11. Transaction Data Store Initialization and Indexing
As a batch operations team, I want the transaction data store removed, redefined, populated from a source file, and indexed so that a clean, properly configured store is available for transaction processing.

### Requirements

REQ-F-016: [Ubiquitous] The system shall remove the existing transaction data store cluster if one exists, then define a new indexed transaction data store cluster with a 16-byte key.

REQ-F-017: [Ubiquitous] The system shall copy all transaction records from the source file to the transaction data store (transaction file).

REQ-F-018: [Ubiquitous] The system shall build an alternate index on the transaction data store to enable efficient secondary access to transaction records.


---
