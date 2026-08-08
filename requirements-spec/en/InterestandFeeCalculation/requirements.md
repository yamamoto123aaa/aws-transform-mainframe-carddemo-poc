# Interest and Fee Calculation — Requirements


## Global Preconditions

- All operations require valid input data and appropriate authorization.
- Processing constraints and scheduling dependencies are documented in the Job Dependencies section.


## 1. Interest and Fee Calculation
As a batch operations team, I want transaction category balances processed to compute and post monthly interest charges so that account balances reflect accrued interest and downstream systems receive updated account data.

**Restart/Recovery:** The job reads the transaction category balance data store sequentially and writes interest transactions and updated account balances. If interrupted, partial updates to the account data store and transaction data store may exist with no automatic rollback.

### Requirements

REQ-F-001: [Ubiquitous] The system shall open the transaction category balance data store (AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS) in input mode, the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) in input-output mode, the card cross-reference data store (AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS) in input mode, the discount group data store (AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS) in input mode, and the transaction data store (AWS.M2.CARDDEMO.SYSTRAN) in output mode before processing begins.

REQ-F-002: [State-driven] While transaction category balance records remain available, the system shall read each record sequentially from the transaction category balance data store; if a read succeeds, set the application result to 0; if end of file is reached, set the application result to 16 and set the end-of-file flag to 'Y'; if any other error occurs, set the application result to 12.

REQ-F-003: [Event-driven] When the account ID on the current transaction category balance record differs from the last processed account ID, and this is not the first iteration, the system shall update the prior account's balance with accumulated interest before processing the new account.

REQ-F-004: [Event-driven] When the account ID on the current transaction category balance record differs from the last processed account ID and this is the first iteration, the system shall set the first-time flag to 'N', reset the total interest accumulator to zero, store the current account ID as the last processed account ID, and retrieve the account record and card cross-reference data for the new account.

REQ-F-005: [Event-driven] When an account ID is available for lookup, the system shall read the account record from the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) using the 11-digit account ID as the key.

REQ-F-006: [Event-driven] When an account ID is available for cross-reference lookup, the system shall read the card cross-reference record from the card cross-reference data store (AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS) using the account ID as the alternate key.

REQ-F-007: [Event-driven] When an account group ID, transaction type code, and transaction category code are available for interest rate lookup, the system shall read the discount group record from the discount group data store (AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS) using the account group ID, transaction type code, and transaction category code as the composite key; if the record is not found, the system shall set the account group ID to 'DEFAULT' and read the default interest rate record using the same transaction type code and transaction category code.

REQ-F-008: [Event-driven] When a non-zero interest rate is found for a transaction category, the system shall compute monthly interest as (transaction category balance × interest rate) ÷ 1200 and add the result to the total interest accumulator.

REQ-F-009: [Event-driven] When a non-zero interest rate is found for the transaction category, the system shall compute monthly interest as (transaction category balance × interest rate) ÷ 1200, add the result to the total interest accumulator, create an interest transaction record with transaction ID constructed from the parameter date concatenated with an incremented suffix counter, transaction type '01', transaction category '05', source 'System', description 'Int. for a/c ' concatenated with the account ID, amount set to the computed monthly interest, merchant fields cleared, card number populated from the card cross-reference, and original and processing timestamps set to the current date and time in DB2 format, and write the record to the transaction data store.

REQ-F-010: [Event-driven] When an interest transaction record has been created, the system shall write the transaction record to the transaction data store (AWS.M2.CARDDEMO.SYSTRAN).

REQ-F-011: [Ubiquitous] The system shall retrieve the current system date and time, extract the year, month, day, hour, minute, second, and millisecond components, and format them into a timestamp string with hyphens separating date components, periods separating time components, and trailing zeros appended, producing a string in the format YYYY-MM-DD-HH.MM.SS.mmm0000.

REQ-F-012: [Ubiquitous] The system shall add the accumulated total interest to the account's current balance, reset the current cycle credit amount to zero, reset the current cycle debit amount to zero, and rewrite the updated account record to the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS).


### Open Questions

OQ-001: Rule 8af0f965 specifies that the transaction ID is constructed from "the parameter date and an incremented suffix counter" within a 16-character alphanumeric field. The source and format of the parameter date (e.g., run date passed via job parameter, system date) are not defined in the rules. Clarification is needed to ensure the transaction ID is generated correctly and uniquely. — Owner: batch operations / business rules team

OQ-002: Rule f955f4f8 specifies that an error condition during sequential read of the transaction category balance data store sets the application result to 12. No rule describes what the system shall do when application result 12 is set (e.g., abort processing, skip the record, log and continue). Clarification is needed on the required error-handling behavior. — Owner: batch operations team


---


## 2. Interest and Fee Calculation
As a batch operations team, I want transaction category balances processed to compute accrued interest and applicable fees daily so that account balances reflect the correct interest charges and interest transaction records are available for downstream processing.

**Restart/Recovery:** The job processes transaction category balance records sequentially. Account balance updates are written in place; if interrupted, partial updates may exist with no automatic rollback.

### Requirements

REQ-F-013: [Ubiquitous] The system shall execute the interest and fee calculation process using business date 2022071800 as the calculation period control parameter, reading transaction category balance records from the Tcatbal-file data store, account records from the Account-file data store, card cross-reference records from the Xref-file data store, and discount group definitions from the Discgrp-file data store, then writing computed interest transaction records to the Transact-file data store.

REQ-F-014: [State-driven] While transaction category balance records remain available in the Tcatbal-file data store, the system shall read each record sequentially and process it for interest calculation.

REQ-F-015: [Event-driven] When a transaction category balance record is read and the account ID differs from the previously processed account ID, the system shall update the prior account's balance in the Account-file data store with the accumulated interest total, reset the interest accumulator to zero, store the current account ID as the last processed account, and retrieve the account record and card cross-reference record for the new account.

REQ-F-016: [Event-driven] When the first transaction category balance record is encountered (first iteration), the system shall set the first-time flag to 'N' and skip the prior-account balance update that would otherwise occur on an account break.

REQ-F-017: [Event-driven] When an account ID is available for lookup, the system shall read the account record from the Account-file data store using the account ID as the key.

REQ-F-018: [Event-driven] When an account ID is available for cross-reference lookup, the system shall read the card cross-reference record from the Xref-file data store using the account ID as the alternate key.

REQ-F-019: [Event-driven] When an account group ID, transaction type code, and transaction category code are available for interest rate lookup, the system shall read the discount group record from the Discgrp-file data store using the composite key of account group ID, transaction type code, and transaction category code.

REQ-F-020: [Unwanted] If the discount group record is not found using the account group ID, transaction type code, and transaction category code, the system shall set the account group ID to 'DEFAULT' and perform a second read from the Discgrp-file data store to retrieve the default interest rate for that transaction type and category.

REQ-F-021: [Event-driven] When a non-zero interest rate is found for a transaction category, the system shall compute monthly interest as (transaction category balance × interest rate) ÷ 1200 and add the result to the total interest accumulator.

REQ-F-022: [Event-driven] When monthly interest has been computed for a transaction category, the system shall create an interest transaction record with: transaction ID constructed from the business date parameter concatenated with an incremented suffix counter; transaction type code '01'; transaction category code '05'; transaction source 'System'; transaction description 'Int. for a/c ' concatenated with the account ID; transaction amount set to the computed monthly interest; merchant ID, name, city, and ZIP cleared or set to zero; card number populated from the card cross-reference record; and original and processing timestamps set to the current date and time formatted as YYYY-MM-DD-HH.MM.SS.mmm0000.

REQ-F-023: [Event-driven] When an interest transaction record has been created, the system shall write it to the Transact-file data store.

REQ-F-024: [Event-driven] When end-of-file is reached on the Tcatbal-file data store, the system shall update the final account's balance in the Account-file data store with the accumulated interest total before terminating processing.

REQ-F-025: [Ubiquitous] The system shall update the account record in the Account-file data store by adding the accumulated total interest to the account's current balance, resetting the current cycle credit amount to zero, and resetting the current cycle debit amount to zero, then rewriting the updated record.

REQ-F-026: [Event-driven] When a read operation on the Tcatbal-file data store returns an end-of-file condition, the system shall set the application result to 16 and set the end-of-file flag to 'Y' to terminate the processing loop.

REQ-F-027: [Unwanted] If a read operation on the Tcatbal-file data store returns an error condition other than end-of-file, the system shall set the application result to 12.

REQ-F-028: [Ubiquitous] The system shall retrieve the current system date and time and format them as a DB2-compatible timestamp string in the format YYYY-MM-DD-HH.MM.SS.mmm0000, using hyphens as date separators and periods as time separators, for use in interest transaction records.


### Non-Functional Requirements

REQ-N-001: [Unwanted] If the interest and fee calculation process is interrupted before the final account's balance update is written to the Account-file data store, partial account balance updates may exist with no automatic rollback; the system shall support reprocessing from the beginning of the Tcatbal-file data store to recover to a consistent state.


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

