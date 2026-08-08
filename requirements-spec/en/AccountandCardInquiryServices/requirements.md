# Account and Card Inquiry Services — Requirements


## Global Preconditions

- All operations require valid input data and appropriate authorization.
- Processing constraints and scheduling dependencies are documented in the Job Dependencies section.


## 1. Account Record Processing and Enriched Output
As a batch operations team, I want account records read sequentially, enriched with converted dates and defaulted values, and written to an output file so that downstream processes receive complete, consistently formatted account data.

### Requirements

REQ-F-001: [Ubiquitous] The system shall open the account data store (ACCTFILE-FILE) in input mode before any account records are read.

REQ-F-002: [Ubiquitous] The system shall open the output data store (OUT-FILE) in output mode before any processed account records are written.

REQ-F-003: [State-driven] While the end-of-file flag is 'N', the system shall repeatedly retrieve the next account record from the account data store and process it until the end-of-file flag is set to 'Y'.

REQ-F-004: [Event-driven] When the next account record is read successfully (result code 0), the system shall copy the account identifier, active status, current balance, credit limit, cash credit limit, open date, expiration date, current cycle credit amount, and group identifier to the output record.

REQ-F-005: [Event-driven] When populating the output record, the system shall invoke the date conversion service to transform the account reissue date from YYYY-MM-DD format (input type '2') to YYYYMMDD format (output type '2') and copy the converted date to the output record.

REQ-F-006: [Unwanted] If the current cycle debit amount on the source account record is zero, the system shall substitute the default value of 2525.00 in the output record; otherwise the system shall copy the source current cycle debit amount.

REQ-F-007: [Ubiquitous] The system shall write the fully populated output account record to the output data store (OUT-FILE).

REQ-F-008: [Event-driven] When a read of the account data store returns end-of-file, the system shall set the application result code to 16 and set the end-of-file flag to 'Y' to terminate processing.

REQ-F-009: [Event-driven] When a read of the account data store returns an error status other than success or end-of-file, the system shall set the application result code to 12.


---


## 2. Account Balance Array File Production
As a batch operations team, I want account records read sequentially and written to an array-formatted output file with hardcoded cycle debit and balance values so that downstream consumers receive the array-structured account data.

### Requirements

REQ-F-010: [Ubiquitous] The system shall open the account data store in input mode before any account records are read for array output.

REQ-F-011: [Ubiquitous] The system shall open the array output store (ARRY-FILE) for sequential output before any array records are written.

REQ-F-012: [State-driven] While the end-of-file flag is 'N', the system shall repeatedly retrieve the next account record from the account data store and write it to the array output store until all records have been processed.

REQ-F-013: [Event-driven] When an account record is successfully read, the system shall copy the account identifier to the array output record, set array occurrence 1 current balance to the source current balance and cycle debit to 1005.00, set array occurrence 2 current balance to the source current balance and cycle debit to 1525.00, and set array occurrence 3 balance to -1025.00 and cycle debit to -2500.00.

REQ-F-014: [Event-driven] When the array output record has been fully assembled, the system shall write it to the array output store.

REQ-F-015: [Event-driven] When a read of the account data store returns end-of-file during array processing, the system shall set the application result code to 16 and set the end-of-file flag to 'Y'.

REQ-F-016: [Event-driven] When a read of the account data store returns an error status other than success or end-of-file during array processing, the system shall set the application result code to 12.


---


## 3. Account Data Variable-Block Export
As a batch operations team, I want two variable-length output records written per account to a variable-block output file so that downstream systems receive both account identity/status and account financial details in separate records.

### Requirements

REQ-F-017: [Ubiquitous] The system shall open the account data store in input mode before any account records are read for variable-block export.

REQ-F-018: [Ubiquitous] The system shall open the variable-block output file (VBRC-FILE) for sequential output before any variable-block records are written.

REQ-F-019: [State-driven] While the end-of-file indicator is not 'Y', the system shall retrieve the next account record from the account data store and write its two variable-block output records.

REQ-F-020: [Event-driven] When an account record is successfully read for variable-block export, the system shall copy the account reissue date to the date conversion input area and to the formatted reissue date work area.

REQ-F-021: [Event-driven] When an account record is successfully read and the account record area is populated, the system shall populate variable-block record 1 with the account identifier and active status, and populate variable-block record 2 with the account identifier, current balance, credit limit, and reissue year.

REQ-F-022: [Event-driven] When variable-block record 1 is assembled for an account, the system shall set the record length to 12, copy variable-block record 1 into the output buffer, and write the buffer to the variable-block output file.

REQ-F-023: [Event-driven] When variable-block record 2 is assembled for an account, the system shall set the record length to 39, copy variable-block record 2 into the output buffer, and write the buffer to the variable-block output file.

REQ-F-024: [Event-driven] When a read of the account data store returns end-of-file during variable-block export, the system shall set the application result code to 16 and set the end-of-file indicator to 'Y' to terminate the processing loop.

REQ-F-025: [Event-driven] When a read of the account data store returns an I/O error during variable-block export, the system shall set the application result code to 12.


---


## 4. Date Conversion Service
As a batch operations team, I want date format conversion validated and applied correctly so that output records contain dates in the required format.

### Requirements

REQ-F-026: [Event-driven] When the date conversion service receives an input type indicator of '2', the system shall route to the YYYY-MM-DD date format handler; when the input type indicator is '1', the system shall route to the YYYYMMDD date format handler; when the input type indicator is neither '1' nor '2', the system shall route to the error handler.

REQ-F-027: [Unwanted] If the input type indicator is neither '1' nor '2', or the output type indicator is incompatible with the input type, the system shall populate the error message field with 'INVALID INPUT'.


---


## 5. Card File Batch Retrieval and Display
As a batch operations team, I want all card records read sequentially from the card data store and reported so that the full contents of the card file can be reviewed and verified.

**Restart/Recovery:** The job reads the card data store (AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS) in input mode only; no writes are performed. If interrupted, the job may be restarted from the beginning without side effects.

### Requirements

REQ-F-028: [Ubiquitous] The system shall open the card data store (AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS) in read-only sequential access mode before any record retrieval is attempted.

REQ-F-029: [State-driven] While more records remain available in the card data store, the system shall retrieve the next card record sequentially and display the card record when successfully retrieved, continuing until the end of the card data store is reached.

REQ-F-030: [Event-driven] When a sequential read of the card data store succeeds, the system shall set the application result code to 0.

REQ-F-031: [Event-driven] When a read operation is initiated on the card file and end-of-file is reached, the system shall set the application result code to 16 and set the end-of-file flag to 'Y' to signal termination of file processing.

REQ-F-032: [Event-driven] When a sequential read of the card data store encounters any condition other than success or end-of-file, the system shall set the application result code to 12.

REQ-F-033: [Event-driven] When the application result code indicates end-of-file (value 16), the system shall terminate sequential record processing.

REQ-F-034: [Event-driven] When a card record is successfully retrieved and the end-of-file indicator is not set, the system shall output that card record.


---


## 6. Card Cross-Reference File Retrieval
As a batch operations team, I want all card cross-reference records retrieved and reported sequentially so that the full contents of the card cross-reference data store are available for review and downstream consumption.

### Requirements

REQ-F-035: [Ubiquitous] The system shall open the card cross-reference data store (XREFFILE-FILE) in input mode for sequential access before any records are read.

REQ-F-036: [State-driven] While the end-of-file condition has not been reached, the system shall repeatedly read the next card cross-reference record sequentially from the card cross-reference data store.

REQ-F-037: [Event-driven] When a card cross-reference record is successfully read from the file (file status is '00'), the system shall display the card cross-reference record.

REQ-F-038: [Event-driven] When the end of the card cross-reference data store is reached, the system shall cease further read attempts and terminate file processing.

REQ-F-039: [Event-driven] When a file I/O error occurs during sequential reading of the card cross-reference data store, the system shall cease further read attempts and terminate file processing.


---


## 7. Customer File Sequential Read and Display
As a batch operations team, I want all customer records read sequentially from the customer data store and displayed so that the full customer population can be reported or verified in a single batch run.

**Restart/Recovery:** This job reads the customer data store (AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS) sequentially in input mode. Processing terminates at end-of-file. There is no write-back to the data store; the job is non-destructive and inherently restartable.

### Requirements

REQ-F-040: [Ubiquitous] The system shall open the customer data store (AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS) in input mode before any record retrieval begins.

REQ-F-041: [State-driven] While more customer records are available in the file, the system shall retrieve the next customer record and, if the retrieval succeeds, display the customer record; this continues until the end-of-file condition is reached.

REQ-F-042: [Event-driven] When the read operation returns an end-of-file condition, the system shall terminate sequential processing of the customer data store.

REQ-F-043: [Event-driven] When the read operation returns an error condition, the system shall set the operation result to error and cease further retrieval from the customer data store.


### Open Questions

OQ-001: Rule `3a22d1e6_e177_4dd5_8934_805356c1f26e` states that a read failure sets the result to error, but no downstream handling (e.g., job abend, error report, retry) is described beyond ceasing retrieval. What is the required business response to a read error — should the job terminate abnormally, log the error, or continue? — Owner: batch operations / business analyst


---


## 8. Message Queue-Based Account Inquiry Processing
As a batch operations team, I want account inquiry requests consumed from an input message queue, resolved against the account data store, and replied to via an output message queue so that downstream clients receive accurate account details or meaningful error responses asynchronously.

**Restart/Recovery:** Each message is committed individually before the next message is retrieved. If the program terminates mid-processing, messages not yet committed remain available in the input queue for reprocessing.

### Requirements

REQ-F-044: [Event-driven] When the program begins initialization, the system shall open the error queue 'CARD.DEMO.ERROR' for output, store the resulting queue handle, set the error queue open flag to TRUE on success, and send error diagnostic information (condition code, reason code, and queue name) to the error display area with the message 'ERR MQOPEN ERR' on failure.

REQ-F-045: [Event-driven] When the program is ready to open queues, the system shall open the input queue for shared input, store the resulting queue handle, set the reply queue open flag to TRUE on success, and log error details (condition code, reason code, and queue name) with the message 'INP MQOPEN ERR' on failure.

REQ-F-046: [Event-driven] When the program is ready to open queues, the system shall open the reply queue 'CARD.DEMO.REPLY.ACCT' for output, store the resulting queue handle, set the response queue open flag to TRUE on success, and log error details (condition code, reason code, and queue name) with the message 'OUT MQOPEN ERR' on failure.

REQ-F-047: [State-driven] While messages remain available in the input queue, the system shall commit the previous message's processing before retrieving the next message from the input queue.

REQ-F-048: [State-driven] While messages remain available in the input queue, the system shall retrieve the next message using a 5-second wait interval, extract the message identifier, correlation identifier, and reply-to queue name on success, set the no-more-messages flag when no messages are available (reason code 2033), and log error details with the message 'INP MQGET ERR:' for any other failure.

REQ-F-049: [Event-driven] When an account inquiry request is received, the system shall validate that the function code equals 'INQA' and that the account identifier is non-zero before proceeding to account retrieval; if either condition is not met, the system shall route the request to the invalid-request error handler.

REQ-F-050: [Event-driven] When the request parameters have been validated and the account identifier is available, the system shall read the account record from the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) using the account identifier as the retrieval key and capture the response code to determine the outcome.

REQ-F-051: [Event-driven] When the account record is successfully retrieved from the account data store, the system shall extract the account identifier, active status, current balance, credit limit, cash credit limit, open date, expiration date, reissue date, current-cycle credit, current-cycle debit, and group identifier from the retrieved record and populate the reply message with these values, then prepare the reply for transmission.

REQ-F-052: [Event-driven] When the account record is not found in the account data store, the system shall compose an error reply message containing 'INVALID REQUEST PARAMETERS' and the requested account identifier, then prepare the error reply for transmission.

REQ-F-053: [Unwanted] If the account record retrieval fails with a technical error (a response code other than success or not-found), the system shall capture the response code and reason code, record the input queue name, set the error message to 'ERROR WHILE READING ACCTFILE', log the error, and terminate the program.

REQ-F-054: [Event-driven] When the account inquiry request fails parameter validation (function code is not 'INQA' or account identifier is zero), the system shall compose an error reply message containing 'INVALID REQUEST PARAMETERS', the requested account identifier, and the function code provided, then prepare the error reply for transmission.

REQ-F-055: [Event-driven] When a reply message has been prepared, the system shall send the reply message to the output queue, populating the message descriptor with the saved message identifier and correlation identifier from the original request and setting the message format to 'MQSTR '; on failure, the system shall log error details (condition code, reason code, and queue name) with the message 'MQPUT ERR'.

REQ-F-056: [Event-driven] When an error condition has been detected, the system shall send the error diagnostic information (error paragraph name, application return message, condition code, reason code, and queue name) to the error queue, setting the message format to 'MQSTR '; on failure, the system shall log error details (condition code, reason code, and queue name) with the message 'MQPUT ERR'.

REQ-F-057: [Event-driven] When the program is terminating and the input queue is open (reply queue open flag is set), the system shall close the input queue and log error details (condition code, reason code, and queue name) with the message 'MQCLOSE ERR' on failure.

REQ-F-058: [Event-driven] When the program is terminating and the output queue is open (response queue open flag is set), the system shall close the output queue and log error details (condition code, reason code, and queue name) with the message 'MQCLOSE ERR' on failure.

REQ-F-059: [Event-driven] When the program is terminating and the error queue is open (error queue open flag is set), the system shall close the error queue and log error details (condition code, reason code, and queue name) with the message 'MQCLOSE ERR' on failure.


### Non-Functional Requirements

REQ-N-001: [State-driven] While messages remain available in the input queue, the system shall commit each message's processing before retrieving the next message, ensuring that each message is processed as an individual transaction boundary.


### Open Questions

OQ-002: The not-found error reply text is 'INVALID REQUEST PARAMETERS' (same as the malformed-request reply). Should the not-found case use a distinct message such as 'ACCOUNT NOT FOUND' to allow clients to distinguish the two error conditions? — Owner: Account Inquiry Services product owner

OQ-003: The rule for account retrieval technical error states the program terminates; it is unclear whether any partial reply or error queue message is sent before termination in this case. — Owner: Account Inquiry Services product owner


---


## 9. Account Master File Processing and Output Generation
As a batch operations team, I want account records read from the account master data store and written to multiple output formats so that downstream consumers receive account data in fixed-record, array-format, and variable-record layouts.

### Requirements

REQ-F-060: [Ubiquitous] The system shall open the Acctfile-file data store in input mode to enable sequential reading of account records.

REQ-F-061: [Ubiquitous] The system shall open the output data store (Out-acct-rec data store) in output mode to enable writing of processed account records.

REQ-F-062: [Ubiquitous] The system shall open the Arr-array-rec data store for sequential output to enable writing of array-formatted account records.

REQ-F-063: [Ubiquitous] The system shall open the Vbr-rec data store for sequential output to enable writing of variable-block account records.

REQ-F-064: [State-driven] While the end-of-file flag indicates more records are available, the system shall sequentially read each account record from the Acctfile-file data store, process it, and write results to all three output stores until all records have been processed.

REQ-F-065: [Event-driven] When a sequential read of the Acctfile-file data store succeeds, the system shall set the application result to 0 (success) and proceed with record processing.

REQ-F-066: [Event-driven] When a sequential read of the Acctfile-file data store reaches end-of-file, the system shall set the application result to 16 and set the end-of-file flag to 'Y' to terminate the processing loop.

REQ-F-067: [Event-driven] When a sequential read of the Acctfile-file data store returns any status other than success or end-of-file, the system shall set the application result to 12 (I/O error).

REQ-F-068: [Event-driven] When populating the output account record, the system shall copy the account identifier, active status, current balance, credit limit, cash credit limit, open date, expiration date, current cycle credit amount, and group identifier directly to the output record; invoke the date conversion service to transform the reissue date from YYYY-MM-DD format to YYYYMMDD format and copy the converted date to the output record; and replace the current cycle debit amount with the default value 2525.00 when the source current cycle debit amount is zero, otherwise copying the source value.

REQ-F-069: [Event-driven] When an account record is successfully read, the system shall convert the reissue date from YYYY-MM-DD format to YYYYMMDD format by delegating to the date conversion service, then copy the converted date to the fixed-record output.

REQ-F-070: [Event-driven] When the current cycle debit amount on a successfully read account record is zero, the system shall substitute the default value 2525.00 in the fixed-record output; otherwise the system shall copy the source value.

REQ-F-071: [Ubiquitous] The system shall write each populated fixed-record output record to the output data store (Out-acct-rec data store).

REQ-F-072: [Event-driven] When an account record is successfully read, the system shall assemble an array output record by copying the account identifier, setting array occurrence 1 current balance from the source current balance and cycle debit to 1005.00, setting array occurrence 2 current balance from the source current balance and cycle debit to 1525.00, and setting array occurrence 3 balance to -1025.00 and cycle debit to -2500.00.

REQ-F-073: [Event-driven] When the array output record has been fully assembled, the system shall write it to the Arr-array-rec data store.

REQ-F-074: [Event-driven] When an account record is successfully read, the system shall copy the account reissue date to both the date conversion input area and the formatted reissue date work area.

REQ-F-075: [Event-driven] When an account record is successfully read, the system shall assemble variable-block record 1 containing the account identifier and active status, and variable-block record 2 containing the account identifier, current balance, credit limit, and reissue year.

REQ-F-076: [Event-driven] When variable-block record 1 is assembled, the system shall set the record length to 12, copy the record into the output buffer, and write the buffer to the Vbr-rec data store.

REQ-F-077: [Event-driven] When variable-block record 2 is assembled, the system shall set the record length to 39, copy the record into the output buffer, and write the buffer to the Vbr-rec data store.


### Open Questions

OQ-004: Array occurrences 4 and 5 are not populated by the current processing rules. Should these positions be left empty, zeroed, or populated from source account data in the modernized system? — Owner: business/data owner

OQ-005: The fixed cycle debit values for array occurrences 1 (1005.00), 2 (1525.00), and 3 (-1025.00 balance / -2500.00 debit) are hardcoded. Are these intended as permanent business constants or should they be configurable? — Owner: business owner

OQ-006: The default current cycle debit amount of 2525.00 applied when the source value is zero — is this a permanent business default or a placeholder value? — Owner: business owner

OQ-007: When an I/O error occurs (application result 12) during account record reading, the rules do not describe a subsequent action. Should processing halt, skip the record, or raise an alert? — Owner: batch operations team


---


## 10. Card Master File Batch Retrieval
As a batch operations team, I want card master records read sequentially from the card data store so that all card records are retrieved and made available for downstream processing and output.

### Requirements

REQ-F-078: [Ubiquitous] The system shall open the card file in input mode for sequential access.

REQ-F-079: [State-driven] While the end-of-file condition has not been reached, the system shall retrieve the next sequential card record from the Cardfile-file data store and write the retrieved record to output.

REQ-F-080: [Event-driven] When a read operation on the Cardfile-file data store completes successfully, the system shall set the application result code to 0.

REQ-F-081: [Event-driven] When a read operation on the Cardfile-file data store reaches end-of-file, the system shall set the application result code to 16 and set the end-of-file flag to 'Y' to terminate record processing.

REQ-F-082: [Event-driven] When a read operation on the Cardfile-file data store encounters any condition other than success or end-of-file, the system shall set the application result code to 12.

REQ-F-083: [Event-driven] When a card record is successfully retrieved and the end-of-file flag is 'N', the system shall write the card record to output.


---


## 11. Customer Master File Retrieval
As a batch operations team, I want customer records read sequentially from the customer master data store and output produced for each record so that the full customer population is processed and made available for downstream consumption.

### Requirements

REQ-F-084: [Ubiquitous] The system shall open the customer file (CUSTFILE-FILE) in input mode before retrieving any customer records.

REQ-F-085: [State-driven] While more customer records are available in the customer file, the system shall retrieve the next sequential customer record and, upon successful retrieval, output the customer record.

REQ-F-086: [Event-driven] When the read operation returns an end-of-file condition, the system shall cease retrieval and terminate processing of the customer file.

REQ-F-087: [Event-driven] When a customer record is successfully retrieved from the customer file, the system shall output that customer record.


---


## 12. Card Cross-Reference Data Retrieval
As a batch operations team, I want all card cross-reference records read and processed sequentially from the cross-reference master data store so that the full contents of the cross-reference file are retrieved and made available for downstream consumption.

### Requirements

REQ-F-088: [Ubiquitous] The system shall open the Xreffile-file data store in input mode for sequential access before any records are read.

REQ-F-089: [State-driven] While the end-of-file condition has not been reached, the system shall repeatedly read the next card cross-reference record from the Xreffile-file data store and output the retrieved record.

REQ-F-090: [Event-driven] When a card cross-reference record is successfully read from the Xreffile-file data store (file status '00'), the system shall output the card cross-reference record to make the retrieved data available.

REQ-F-091: [Event-driven] When the read operation indicates end-of-file, the system shall set the end-of-file flag to 'Y' and cease further read attempts from the Xreffile-file data store.


---
