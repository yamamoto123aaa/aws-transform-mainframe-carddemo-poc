# Utility and Support Services — Requirements


## Global Preconditions

- All operations require valid input data and appropriate authorization.
- Processing constraints and scheduling dependencies are documented in the Job Dependencies section.


## 1. Date Format Transformation and Validation
As a batch operations team, I want date values transformed between separator-free and hyphen-delimited formats so that downstream processes receive dates in the required representation and invalid requests are clearly identified.

### Requirements

REQ-F-001: [Event-driven] When the input type indicator is received, the system shall route to the handler for dates without separators (YYYYMMDD format) when the indicator is '1', route to the handler for dates with hyphens (YYYY-MM-DD format) when the indicator is '2', or route to the error handler when the indicator is neither '1' nor '2'.

REQ-F-002: [Unwanted] If the input type indicator is neither '1' nor '2', the system shall populate the error message field with 'INVALID INPUT'.

REQ-F-003: [Unwanted] If the output type indicator is incompatible with the input type, the system shall populate the error message field with 'INVALID INPUT'.

REQ-F-004: [Ubiquitous] The system shall restore the caller's context, set the return code to zero, and return control to the caller upon completion.


---


## 2. Wait Time Service Invocation
As a batch operations team, I want a controlled execution pause capability so that batch processing coordination can introduce timed delays of a specified duration.

### Requirements

REQ-F-005: [Ubiquitous] The system shall accept an 8-character wait-time parameter from standard input and store it as the internal wait-time value to be passed to the wait-time service.

REQ-F-006: [Ubiquitous] The system shall delegate to the wait-time service, passing the internal wait-time value, to pause execution for the duration specified; the wait-time value is interpreted as milliseconds.

REQ-F-007: [Ubiquitous] The wait-time service shall extract the delay interval value from the caller's parameter area, store it in the timer event control block, and invoke the interval control timer to suspend execution for that duration.

REQ-F-008: [Ubiquitous] When the interval control timer expires, the system shall restore the caller's execution context and return control to the caller with a success return code.


---


## 3. Message Queue Date-Time Request-Reply Service
As a batch operations team, I want incoming date-time requests processed from a message queue and replied to with the current system date and time so that consuming services receive accurate, formatted date and time information on demand.

**Restart/Recovery:** All three queues (input, output, and error) are opened before message processing begins. On termination, each queue is closed only if it was successfully opened. If any queue operation or message operation fails, error details are sent to the error queue.

### Requirements

REQ-F-009: [Event-driven] When the input queue open operation is initiated, the system shall open the input queue with shared input access and context-save options; on success, store the queue handle and set the REPLY-QUEUE-OPEN flag; on failure, capture the condition and reason codes and record the error message 'INP MQOPEN ERR'.

REQ-F-010: [Event-driven] When the output queue open operation is initiated, the system shall open the output queue with put access and context-pass options; on success, store the queue handle and set the RESP-QUEUE-OPEN flag; on failure, capture the condition and reason codes and record the error message 'OUT MQOPEN ERR'.

REQ-F-011: [Event-driven] When the error queue open operation is initiated, the system shall open the error queue with put access and context-pass options, setting the error queue name to 'CARD.DEMO.ERROR'; on success, store the queue handle and set the ERR-QUEUE-OPEN flag; on failure, capture the condition and reason codes and record the error message 'ERR MQOPEN ERR'.

REQ-F-012: [State-driven] While messages remain available in the input queue (NO-MORE-MSGS flag is not set), the system shall repeatedly retrieve, process, and reply to messages until the NO-MORE-MSGS flag is set.

REQ-F-013: [State-driven] While messages remain available in the input queue, the system shall retrieve the next message from the input queue with a 5-second wait interval; on success, extract the message ID, correlation ID, and reply-to queue name, save them for the reply, and move the message buffer to the request message area; when no more messages are available (reason code 2033), set the NO-MORE-MSGS flag; on other failures, capture the condition and reason codes and record the error message 'INP MQGET ERR:'.

REQ-F-014: [Ubiquitous] The system shall capture the current system date and time, format the date as MM/DD/YYYY and the time as HH:MM:SS, and construct a reply message containing 'SYSTEM DATE : ' followed by the formatted date and 'SYSTEM TIME : ' followed by the formatted time.

REQ-F-015: [Event-driven] When the reply message is ready to be sent, the system shall send the reply message to the output queue with the saved message ID and correlation ID, using string format and queue-manager character set; on success, record the condition and reason codes; on failure, capture the condition and reason codes and record the error message 'MQPUT ERR'.

REQ-F-016: [Event-driven] When an error condition is detected, the system shall send an error message to the error queue containing the error details (paragraph name, return message, condition code, reason code, and queue name), using string format and queue-manager character set; on success, record the condition and reason codes; on failure, capture the condition and reason codes and record the error message 'MQPUT ERR'.

REQ-F-017: [Ubiquitous] The system shall close the input queue if the REPLY-QUEUE-OPEN flag is set, close the output queue if the RESP-QUEUE-OPEN flag is set, and close the error queue if the ERR-QUEUE-OPEN flag is set.

REQ-F-018: [Event-driven] When the input queue close operation is initiated, the system shall close the input queue using the stored input queue handle; on success, record the condition and reason codes; on failure, capture the condition and reason codes and record the error message 'MQCLOSE ERR'.

REQ-F-019: [Event-driven] When the output queue close operation is initiated, the system shall close the output queue using the stored output queue handle; on success, record the condition and reason codes; on failure, capture the condition and reason codes and record the error message 'MQCLOSE ERR'.

REQ-F-020: [Event-driven] When the error queue close operation is initiated, the system shall close the error queue using the stored error queue handle; on failure, capture the condition and reason codes and record the error message 'MQCLOSE ERR'.


### Open Questions

OQ-001: The noise_context rule indicates the reply queue name is hardcoded to 'CARD.DEMO.REPLY.DATE' during initialization, while the output queue is opened using a queue handle derived from the reply-to queue name extracted from each incoming request message. Should the hardcoded reply queue name serve as a default when no reply-to queue name is present in the request, or does it serve a different purpose? — Owner: integration/messaging team


---


## 4. Date Validation and Lillian Conversion
As a batch operations team, I want date strings validated and converted to Lillian format so that callers receive a structured result describing whether a given date is valid and, if not, the nature of the failure.

### Requirements

REQ-F-021: [Ubiquitous] The system shall accept an input date string, a format mask specifying the date structure, and a result area from the caller as the inputs for the date validation workflow.

REQ-F-022: [Ubiquitous] The system shall delegate to the date conversion service to convert the input date string to Lillian format and validate it against the provided format mask, then extract the severity code and message number from the returned feedback structure and store them in the message record.

REQ-F-023: [Event-driven] When the date conversion service returns a feedback code indicating the validation outcome, the system shall map the feedback code to a result description using the following mapping: the invalid-date condition maps to `'Date is valid'`; the insufficient-data condition maps to `'Insufficient'`; the bad-date-value condition maps to `'Datevalue error'`; the invalid-era condition maps to `'Invalid Era'`; the unsupported-range condition maps to `'Unsupp. Range'`; the invalid-month condition maps to `'Invalid month'`; the bad-picture-string condition maps to `'Bad Pic String'`; the non-numeric-data condition maps to `'Nonnumeric data'`; the year-in-era-zero condition maps to `'YearInEra is 0'`; and any other feedback code maps to `'Date is invalid'`.

REQ-F-024: [Ubiquitous] The system shall return the formatted message record — containing the validation result description, severity code, message number, test date, and format mask — to the caller via the result area.


### Open Questions

OQ-002: Rule 9134afe8 maps the invalid-date feedback code to the result description `'Date is valid'`. This appears counterintuitive — an "invalid-date condition" producing a "Date is valid" message may be a naming inversion in the source. Confirm whether this mapping is intentional or whether the condition label and result description are swapped. — Owner: business/domain SME


---


## 5. FTP File Transfer to Remote Server
As a batch operations team, I want a designated mainframe dataset transferred to a remote FTP server so that the file is available on the remote system for downstream consumption.

### Requirements

REQ-F-025: [Ubiquitous] The system shall execute an FTP session that connects to the remote server at address 172.31.21.124 using the provided credentials, changes to the /ftpfolder directory on the remote server, uploads the source dataset to the remote system as welcome.txt, and closes the connection upon completion.


### Open Questions

OQ-003: The rule specifies a hardcoded remote server address (172.31.21.124), credentials (carddemousr / ftpdemo1), remote directory (/ftpfolder), and target filename (welcome.txt). Should any of these values be externalized as configuration parameters to support environment-specific deployments? — Owner: integration/operations team

OQ-004: The rule does not describe error handling behavior (e.g., connection failure, authentication failure, upload failure). Should the modernized implementation define retry logic or failure notification? — Owner: integration/operations team


---


## 6. Interval Timer Delay Execution
As a batch operations team, I want a reusable delay utility that pauses batch execution for a caller-specified interval so that batch processing coordination can introduce controlled wait periods between operations.

### Requirements

REQ-F-026: [Ubiquitous] The system shall accept a delay interval value from the caller's parameter area and store it into the timer event control block for use during the pause operation.

REQ-F-027: [Event-driven] When the delay interval value has been loaded into the timer event control block, the system shall pause execution for the duration specified by that interval and resume processing only after the interval expires.

REQ-F-028: [Event-driven] When the timer interval completes, the system shall restore the caller's registers, set the return code to success, and return control to the caller.


---


## 7. Text-to-PDF Conversion
As a batch operations team, I want text files converted to PDF format so that output documents are available in a viewable PDF format for downstream consumption.

### Requirements

REQ-F-029: [Ubiquitous] The system shall read the input text file from the input dataset, convert it to PDF format with browsing enabled, and write the resulting PDF to the target output dataset.


---


## 8. Batch Processing Pause for Time-Based Sequencing
As a batch operations team, I want a configurable wait step available in batch job streams so that dependent batch steps can be sequenced with controlled time-based intervals.

### Requirements

REQ-F-030: [Ubiquitous] The system shall accept a wait-time duration from control input and suspend batch execution for that specified duration, where the duration is expressed in centiseconds (3600 centiseconds = 36 seconds).

REQ-F-031: [Ubiquitous] The system shall delegate the pause operation to the wait-time service, passing the accepted wait-time value as a parameter so that execution is suspended for the specified duration before processing continues.


---
