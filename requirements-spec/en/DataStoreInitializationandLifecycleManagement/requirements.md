# Data Store Initialization and Lifecycle Management — Requirements


## Global Preconditions

- All operations require valid input data and appropriate authorization.
- Processing constraints and scheduling dependencies are documented in the Job Dependencies section.


## 1. Account Data Store Initialization and Population
As a batch operations team, I want the account data store deleted, recreated, and populated from a sequential source on demand so that a clean, correctly configured account repository is available for production use.

**Restart/Recovery:** This job performs destructive replacement of the account data store. If interrupted after deletion but before population completes, the account data store will be absent or partially populated; there is no automatic rollback.

### Requirements

REQ-F-001: [Event-driven] When the account data store initialization job executes, the system shall remove the existing account file (account file data store).

REQ-F-002: [Event-driven] When the existing account file has been removed, the system shall define a new indexed account file (account file data store) as a keyed sequential data store.

REQ-F-003: [Event-driven] When the new account file has been defined, the system shall copy all account records from the sequential source (Account-input data store) into the account file.


---


## 2. Daily Rejection Dataset Versioning Setup
As a batch operations team, I want a versioned container defined for daily rejection records so that the system retains a rolling history of up to five daily rejection datasets and automatically removes the oldest version when the limit is exceeded.

### Requirements

REQ-F-004: [Ubiquitous] The system shall define a generation data group for daily rejection records with a maximum retention limit of five generations, configured so that when a new generation is created and the five-generation limit is reached, the oldest generation is automatically deleted.


---


## 3. Customer Data Store Initialization and Reset
As a batch operations team, I want the customer data store deleted and recreated as part of a setup or reset procedure so that customer master data can be freshly populated from a clean state.

### Requirements

REQ-F-005: [Ubiquitous] The system shall delete the customer data store cluster from the catalog unconditionally as part of a data initialization or reset sequence.

REQ-F-006: [Unwanted] If the customer data store cluster does not exist at the time of deletion, the system shall proceed without raising an error.

REQ-F-007: [Event-driven] When the customer data store cluster has been removed, the system shall create a new indexed-sequential customer data store with 1 primary cylinder and 5 secondary cylinders of allocation, 10-byte keys, 500-byte records, and sharing options that support concurrent batch and online access.


### Open Questions

OQ-001: The sharing options that "support concurrent batch and online access" are referenced in the program description but not specified in any rule item. What specific sharing option values are required? — Owner: data architecture team


---


## 4. Versioned Dataset Lifecycle Management
As a batch operations team, I want generation data groups defined for transaction datasets, backup datasets, and report datasets so that the system automatically maintains rolling retention of versioned data without manual cleanup.

### Requirements

REQ-F-008: [Ubiquitous] The system shall define a generation data group base for the daily transaction dataset (AWS.M2.CARDDEMO.TRANSACT.DALY) with a retention limit of 5 generations, automatically deleting the oldest generation when the limit is exceeded.

REQ-F-009: [Ubiquitous] The system shall define a generation data group base for the transaction records dataset (AWS.M2.CARDDEMO.SYSTRAN) with a retention limit of 5 generations, automatically deleting superseded generations when the limit is exceeded.

REQ-F-010: [Ubiquitous] The system shall define a generation data group base for the backup dataset (AWS.M2.CARDDEMO.TCATBALF.BKUP) with a retention limit of 5 generations, automatically deleting the oldest generation when the limit is exceeded.

REQ-F-011: [Ubiquitous] The system shall define a generation data group base for the transaction backup dataset (AWS.M2.CARDDEMO.TRANSACT.BKUP) with a retention limit of 5 generations, automatically deleting the oldest generation when the limit is exceeded.

REQ-F-012: [Ubiquitous] The system shall define a generation data group base for the combined transaction records dataset (AWS.M2.CARDDEMO.TRANSACT.COMBINED) with a retention limit of 5 generations, automatically deleting older generations when the limit is exceeded.

REQ-F-013: [Ubiquitous] The system shall define a generation data group base for the transaction report dataset (AWS.M2.CARDDEMO.TRANREPT) with a retention limit of 5 generations, automatically deleting older generations when the limit is exceeded.


---


## 5. Reference Data Versioned Backup Lifecycle Management
As a batch operations team, I want transaction type, transaction category, and disclosure group reference data managed under versioned backup groups so that multiple historical snapshots are retained and obsolete versions are automatically purged without unbounded storage growth.

### Requirements

REQ-F-014: [Ubiquitous] The system shall define a versioned backup group for transaction type reference data with a retention limit of 5 generations, configured so that the oldest generation is automatically deleted when the retention limit is exceeded.

REQ-F-015: [Ubiquitous] The system shall define a versioned backup group for transaction category reference data with a retention limit of 5 generations, configured so that the oldest generation is automatically deleted when the retention limit is exceeded.

REQ-F-016: [Ubiquitous] The system shall define a versioned backup group for disclosure group reference data with a retention limit of 5 generations, configured so that excess non-current generations are automatically deleted when the retention limit is exceeded.

REQ-F-017: [Event-driven] When the transaction type versioned backup group definition succeeds, the system shall copy transaction type records from the transaction type file source to the first generation of the transaction type versioned backup group, allocating the backup with fixed-block format using 60-byte logical records and 600-byte blocks.

REQ-F-018: [Event-driven] When the transaction category versioned backup group definition succeeds, the system shall copy transaction category records from the source dataset to the first generation of the transaction category versioned backup group.

REQ-F-019: [Ubiquitous] The system shall invoke the catalog utility to prepare the environment for the disclosure group backup operation before any disclosure group backup copy is attempted.

REQ-F-020: [Event-driven] When the catalog preparation step for the disclosure group backup completes successfully, the system shall copy disclosure group records from the Discgrp-file data store source to a new generation of the disclosure group versioned backup group and catalog the backup for retention.


---


## 6. Disclosure Group Data Store Initialization
As a batch operations team, I want the disclosure group keyed data store deleted, redefined, and repopulated from a sequential source file so that a clean, fully initialized data store is available for production access.

### Requirements

REQ-F-021: [Event-driven] When the initialization job executes, the system shall delete the disclosure group VSAM cluster if it exists, or proceed without error if it does not.

REQ-F-022: [Event-driven] When the disclosure group indexed-sequential data store is defined, the system shall allocate it with a 16-byte key at offset 0, 50-byte fixed records, 1 primary and 5 secondary cylinders on volume AWSHJ1, shared access for up to 2 concurrent readers and 3 concurrent writers, and automatic erasure of freed space.

REQ-F-023: [Event-driven] When the new disclosure group keyed data store has been defined, the system shall copy all records from the disclosure group sequential file (Discgrp-file data store) into the newly defined keyed data store.


---


## 7. User Security Data Store Initialization
As a batch operations team, I want the user security data store created and populated with foundational user records so that user authentication and authorization can function correctly from initial deployment.

**Category:** setup
**Data flow:** Reads embedded user records, writes to a sequential staging store, then defines and populates a keyed (VSAM indexed) user security store.
**Migration relevance:** Defines the initial security data state required for all user authentication and authorization operations.

### Requirements

REQ-F-024: [Event-driven] When the user security file loader executes, the system shall load ten embedded user records — five administrators (ADMIN001 through ADMIN005) and five standard users (USER0001 through USER0005), each containing a user identifier, first name, last name, and password — into a sequential data store.

REQ-F-025: [Event-driven] When the sequential staging store has been populated, the system shall delete any existing keyed user security store and define a new keyed user security store before copying records into it.

REQ-F-026: [Event-driven] When the sequential dataset has been populated, the system shall copy all user records from the sequential dataset into the VSAM indexed cluster, establishing key-based indexing by user identifier.


---


## 8. User Security Data Store Initialization
As a batch operations team, I want user security data stores created and populated from a generated source file so that the foundational security infrastructure required for user authentication and authorization is established.

**Category:** setup
**Data flow:** Generates a sequential file containing user security records, then uses that file as the source to populate two VSAM data stores — one entry-sequenced and one relative-record.
**Modern equivalent:** The initialization pattern (generate source data, delete any pre-existing target stores, define new stores, load records) must be preserved; the underlying storage mechanism is implementation-specific.

### Requirements

REQ-F-027: [Event-driven] When the user security data store initialization job executes, the system shall generate a sequential file containing administrator and standard user records, each with credentials and access level information.

REQ-F-028: [Event-driven] When the sequential file has been generated, the system shall delete any existing entry-sequenced user security cluster and define a new entry-sequenced cluster in its place.

REQ-F-029: [Event-driven] When the sequential file has been generated, the system shall delete any existing relative-record user security cluster and define a new relative-record cluster in its place.

REQ-F-030: [Event-driven] When the entry-sequenced user security cluster has been defined, the system shall copy all user security records from the sequential file into the entry-sequenced data store.

REQ-F-031: [Event-driven] When the relative-record user security cluster has been defined, the system shall copy all user security records from the sequential file into the relative-record data store.


---


## 9. Transaction Report Version Group Definition
As a batch operations team, I want a versioned group structure defined for transaction report datasets so that up to 10 historical generations of transaction reports are automatically retained and older versions are aged out when the limit is exceeded.

### Requirements

REQ-F-032: [Ubiquitous] The system shall define a generation data group named AWS.M2.CARDDEMO.TRANREPT with a maximum retention limit of 10 generations, enabling automatic version management and retention of historical transaction report datasets.


---


## 10. Transaction Category Balance Data Store Setup
As a batch operations team, I want the transaction category balance data store removed, redefined, and repopulated from a flat file so that a clean, fully initialized data store is available for production use.

**Restart/Recovery:** The removal step executes unconditionally; if no prior instance exists, the step completes without error. The define and populate steps depend on successful completion of the prior step. If the job is interrupted after population begins, partial data may exist in the indexed-sequential data store with no automatic rollback.

### Requirements

REQ-F-033: [Ubiquitous] The system shall remove the transaction category balance indexed-sequential data store if it exists, allowing subsequent definition and population steps to proceed without conflicts.

REQ-F-034: [Ubiquitous] The system shall define a new transaction category balance indexed-sequential data store with a 17-byte key starting at offset 0, 50-byte fixed records, 1 primary cylinder plus 5 secondary extents, concurrent-read/exclusive-write sharing, and automatic space erasure of freed space.

REQ-F-035: [Ubiquitous] The system shall copy all transaction category balance records from the flat sequential source (Tcatbal-file data store) into the newly defined transaction category balance indexed-sequential data store, keyed by the 17-byte key field, accessing the source with shared disposition and the target with exclusive disposition.


### Non-Functional Requirements

REQ-N-001: [State-driven] While the population step is executing, the system shall access the transaction category balance indexed-sequential data store with exclusive write disposition to ensure data consistency during the initial load.


---


## 11. Transaction Master Data Store Initialization and Lifecycle Management
As a batch operations team, I want the transaction master data store and its alternate index cleaned up and recreated from a known-empty state so that subsequent transaction processing begins against a fresh, uncorrupted structure.

### Requirements

REQ-F-036: [Ubiquitous] The system shall delete the alternate index for the transaction master file (AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX), completing successfully whether the index exists or not.

REQ-F-037: [Unwanted] If the alternate index does not exist at the time of deletion, the system shall treat the operation as successful and continue without error.

REQ-F-038: [Event-driven] When the deletion of the transaction master store completes with a condition code less than 4, the system shall create a new, empty indexed-sequential transaction master store ready for transaction processing.

REQ-F-039: [Ubiquitous] The system shall delete the transaction master store (AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS) unconditionally before recreating it.


---


## 12. Transaction Category Reference Data Store Initialization
As a batch operations team, I want the transaction category reference data store deleted, recreated, and repopulated from a source file so that a clean, fully initialized dataset is available for transaction categorization lookups.

**Category:** setup
**Data flow:** Reads transaction category records from a flat-file source; writes to the transaction category keyed data store (Trancatg-file data store).
**Modern equivalent:** The initialization pattern (delete-if-exists, create fresh, load from source) must be preserved; the physical storage mechanism is implementation-specific.

### Requirements

REQ-F-040: [Event-driven] When the transaction category data store initialization executes, the system shall delete the existing transaction category data store if it exists, and treat the deletion as successful if the data store is not found.

REQ-F-041: [Event-driven] When the prior transaction category keyed data store has been removed, the system shall define a new indexed-sequential transaction category keyed data store with a 6-byte key and 60-byte fixed-length records.

REQ-F-042: [Event-driven] When the new transaction category keyed data store has been defined, the system shall copy all transaction category records from the flat-file source into the transaction category keyed data store.


---


## 13. Transaction Data Store Alternate Index Initialization
As a batch operations team, I want an alternate index created on the transaction data store and made accessible via a named path so that applications can efficiently retrieve transaction records by processed timestamp without performing full sequential scans.

### Requirements

REQ-F-043: [Ubiquitous] The system shall create an alternate index on the transaction data store keyed by processed timestamp, configured as non-unique to allow multiple transactions to share the same timestamp value, and with automatic upgrade capability to maintain index consistency when the base data store is updated.

REQ-F-044: [Ubiquitous] The system shall define a path named `AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX.PATH` that references the alternate index `AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX`, establishing indexed access to transaction records through that alternate index.

REQ-F-045: [Ubiquitous] The system shall build the alternate index over the transaction data store by reading all records from the primary dataset and writing the resulting index structure to the alternate-index dataset (`AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX`).


### Open Questions

OQ-002: The rule specifies a key starting at byte 26 with length 304 for the processed timestamp field. A 304-byte key length is unusually large for a timestamp; should this be confirmed as intentional or investigated as a possible transcription error? — Owner: data architecture team


---


## 14. Transaction Type Dataset Initialization
As a batch operations team, I want the transaction type reference dataset prepared in a clean, consistent state so that applications can reliably access current transaction type records.

### Requirements

REQ-F-046: [Event-driven] When the transaction type dataset initialization job executes, the system shall delete the existing transaction type file (TRANTYPE-FILE) if one is present before creating a new version.

REQ-F-047: [Event-driven] When the existing transaction type file has been removed, the system shall define a new transaction type file as a keyed sequential data store ready to receive transaction type records.

REQ-F-048: [Event-driven] When the new transaction type file has been defined, the system shall copy all transaction type records from the flat file source into the transaction type file.


### Open Questions

OQ-003: The rule describing the physical structure of the transaction type file (2-byte keys, 60-byte records, erase protection, volume allocation) has been classified as platform mechanics. Confirm whether any of these values — particularly key length or record size — carry business meaning that must be preserved in the modernized data store schema. — Owner: data architecture team


---


## 15. Card Cross-Reference Alternate Index Lifecycle Management
As a batch operations team, I want the card cross-reference alternate index rebuilt from a clean state so that secondary-key lookups by account ID remain accurate and consistent with the current primary dataset.

### Requirements

REQ-F-049: [Ubiquitous] The system shall remove the existing alternate index on the card cross-reference data store (AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX) if one is present, leaving the underlying primary dataset unaffected; if no alternate index exists, the deletion shall be skipped without error.

REQ-F-050: [Ubiquitous] The system shall create a new alternate index on the card cross-reference primary dataset (AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS) keyed on account ID extracted from positions 11–25 of each record, with non-unique keys, upgrade capability, 50-byte fixed records, 10% free space on the index component, 20% free space on the data component, 5 cylinders primary allocation and 1 cylinder secondary allocation on volume AWSHJ1, and both data and index component structures.

REQ-F-051: [Ubiquitous] The system shall define a path named AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH that establishes the logical relationship between the alternate index (AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX) and its base cluster, enabling access to the alternate index in relation to the primary dataset.


### Open Questions

OQ-004: The alternate index key is specified as positions 11–25 of each record (15 bytes). The primary dataset uses 16-byte keys. Should the alternate index key length be confirmed as 15 bytes, or is there an off-by-one discrepancy? — Owner: data architecture team

OQ-005: Volume AWSHJ1 is referenced for storage allocation of the alternate index. In a modernized environment, volume-level placement is typically abstracted. Should the storage allocation constraints (5 cylinders primary, 1 cylinder secondary) be translated to equivalent capacity targets, or deferred to the platform? — Owner: infrastructure/migration team


---



## Job Dependencies

Batch processing schedules and execution dependencies (source: Control-M).

### DAILY-TransactionBackup

**Schedule:** Daily, completes by 23:00
**Recovery:** RERUN

| Step | Job |
|------|-----|
| 1 | TRANBKP |

### WEEKLY-DisclosureGroupsRefresh

**Schedule:** Weekly, completes by 23:00
**Recovery:** RERUN

| Step | Job |
|------|-----|
| 1 | DISCGRP |

### Cross-Schedule Dependencies

- WEEKLY-TransactionTypesDBRefresh/MNTTRDB2 → WEEKLY-DisclosureGroupsRefresh/CLOSEFIL

