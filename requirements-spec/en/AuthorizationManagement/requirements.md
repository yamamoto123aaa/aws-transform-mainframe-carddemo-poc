# Authorization Management — Requirements


## Navigation Context

Users typically enter the Authorization Management function from a main menu (COMEN01C), landing on the authorization selection screen (COPAUS0C/COPAU00) where they can search by account ID and browse paginated lists of pending authorizations. From the selection screen, users can navigate forward to authorization detail screens via COPAUS1C, or return to the menu by pressing PF3; unauthenticated or session-expired users are redirected to the signon screen (COSGN00C) as a fallback. Batch processing operates independently of the online navigation path, with the JCL job CBPAUP0J invoking CBPAUP0C on a scheduled basis to purge expired authorization records from the IMS database, and COPAUA0C running as a separate batch process to consume and respond to authorization requests from the message queue. Online and batch components share common IMS database structures and VSAM master files, meaning batch cleanup and authorization processing directly affect the data visible to online users.


## Global Preconditions

- **Authentication:** The user must be signed on with a valid session before accessing any online authorization screens; unauthenticated sessions are redirected to COSGN00C
- **Account existence:** A valid, active account record must exist in `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` for any account-level authorization inquiry or processing to proceed
- **Card cross-reference availability:** The card-to-account cross-reference file (`AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` / AIX path) must be accessible and consistent for card-based lookups to resolve correctly
- **Customer master availability:** A corresponding customer record must exist in `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` for any operation that requires customer identity or profile data
- **IMS database availability:** The IMS authorization database (governed by the PSB referenced in CBPAUP0J) must be online and accessible for both online inquiry and batch processing operations
- **Authorization summary integrity:** Authorization summary records (CIPAUSMY) must be consistent with their associated detail records (CIPAUDTY) before any read, update, or delete operation is performed
- **Message queue connectivity (batch):** For COPAUA0C, the MQ infrastructure must be available and the relevant queues open and reachable before authorization request processing can begin
- **Expiry threshold configuration:** For batch cleanup (CBPAUP0C), either a valid expiry threshold parameter must be supplied as input or the default of 5 days will be applied; the system date must be accurate and accessible
- **Sufficient batch job authority:** The batch jobs (CBPAUP0J) must execute with credentials that have read/write/delete authority over the IMS authorization database and access to required IMS control regions (DFSRRC00)
- **Communication area (COMMAREA) validity:** For all online programs, a properly initialized COMMAREA (COCOM01Y) must be passed between programs to maintain session state and navigation context


## 1. Authorization Screen Navigation and Control Transfer
As an authorization management user, I want the system to route me to the correct destination when I press a function key on the authorization screen so that I can navigate efficiently between authorization views and the main menu.

### Requirements

REQ-F-001: [Event-driven] When the program is invoked with a non-zero-length communication area, the system shall load the incoming communication area into the local session context for subsequent navigation decisions.

REQ-F-002: [Event-driven] When the program is re-entering and the user presses PF3, the system shall set the destination program to the menu program.

REQ-F-003: [Event-driven] When the destination program is unset, the system shall use the signon screen program as the fallback destination before transferring control.

REQ-F-004: [Event-driven] When the destination program has been determined, the system shall populate the session context with the originating transaction identifier, originating program name, and program context indicator set to initial entry, then transfer control to the destination program passing the updated session context.


---


## 2. Authorization Selection Screen Input and Validation
As an authorization management user, I want to select a pending authorization from a list and be navigated to its detail screen so that I can review or act on individual authorization records.

### Requirements

REQ-F-005: [Ubiquitous] The system shall retrieve user input from the authorization selection screen, including the account identifier and the five authorization selection flags.

REQ-F-006: [Event-driven] When the user submits the authorization selection form, the system shall verify that the account identifier is not empty; if empty, the system shall clear the account identifier field and display the message 'Please enter Acct Id...'.

REQ-F-007: [Event-driven] When the account identifier has been confirmed as non-empty, the system shall verify that the account identifier contains only numeric characters; if non-numeric, the system shall clear the account identifier field and display the message 'Acct Id must be Numeric ...'.

REQ-F-008: [Ubiquitous] The system shall evaluate the five authorization selection options in sequence; if option 1 is selected, retrieve the authorization key for option 1; if option 2 is selected, retrieve the authorization key for option 2; if option 3 is selected, retrieve the authorization key for option 3; if option 4 is selected, retrieve the authorization key for option 4; if option 5 is selected, retrieve the authorization key for option 5; if no option is selected, clear both the selection flag and the selected-value field.

REQ-F-009: [Event-driven] When the user selects authorization option 1, the system shall record the selection flag and store the authorization key from position 1 of the authorization keys array in the selected-value field.

REQ-F-010: [Event-driven] When the user selects authorization option 2, the system shall record the selection flag and store the authorization key from position 2 of the authorization keys array in the selected-value field.

REQ-F-011: [Event-driven] When the user selects authorization option 3, the system shall record the selection flag and store the authorization key from position 3 of the authorization keys array in the selected-value field.

REQ-F-012: [Event-driven] When the user selects authorization option 4, the system shall record the selection flag and store the authorization key from position 4 of the authorization keys array in the selected-value field.

REQ-F-013: [Event-driven] When the user selects authorization option 5, the system shall record the selection flag and store the authorization key from position 5 of the authorization keys array in the selected-value field.

REQ-F-014: [Event-driven] When no authorization option is selected, the system shall clear both the selection flag and the selected-value field to indicate no selection.

REQ-F-015: [Event-driven] When the authorization selection evaluation is complete, the system shall verify that both the selection flag and the selected-value field are non-empty; if either is empty, the system shall skip navigation to the authorization details screen.

REQ-F-016: [Event-driven] When the selection flag is 'S' or 's', the system shall set the destination program to the authorization details screen program, set the originating transaction identifier to CPVS, set the originating program name to the authorization summary screen program, set the program context indicator to initial entry, and transfer control to the destination program with the session context.

REQ-F-017: [Ubiquitous] The system shall copy the validated account identifier from the screen input into the working account identifier variable and the session context account identifier field.

REQ-F-018: [Event-driven] When the program receives control with a non-zero-length communication area, the system shall copy the incoming communication area into the working session context; if the communication area length is zero, the system shall perform first-time initialization.

REQ-F-019: [Event-driven] When a communication area is received from the caller, the system shall extract the account identifier from the session context, validate that it is numeric, store it in the working account identifier field if numeric or clear it if non-numeric, then check the program re-entry context flag to determine whether to gather account details on first entry or receive user input on re-entry.

REQ-F-020: [Event-driven] When the user presses a function key on the authorization selection screen, the system shall route to the Enter-key handler when Enter is pressed, to the PF3 handler when PF3 is pressed, to the PF7 handler when PF7 is pressed, to the PF8 handler when PF8 is pressed, or to an alternative handler for any other key.


---


## 3. Pending Authorization List Display and Navigation
As an authorization management user, I want to view a paginated list of pending authorizations for a customer account so that I can review authorization activity and select records for detailed inspection.

### Requirements

REQ-F-021: [Event-driven] When account and customer details need to be gathered, the system shall retrieve the card cross-reference record from the card cross-reference data store (AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH) using the account identifier as the key to obtain the customer identifier; if the record is not found, display an error message indicating the account was not found in the cross-reference store; if any other error occurs, display an error message with the response and reason codes.

REQ-F-022: [Event-driven] When the card cross-reference record has been retrieved, the system shall retrieve the account record from the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) using the account identifier as the key to obtain credit limits; if the record is not found, display an error message indicating the account was not found; if any other error occurs, display an error message with the response and reason codes.

REQ-F-023: [Event-driven] When the account record has been retrieved, the system shall retrieve the customer record from the customer data store (AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS) using the customer identifier as the key to obtain the customer name and address information; if the record is not found, display an error message indicating the customer was not found; if any other error occurs, display an error message with the response and reason codes.

REQ-F-024: [Event-driven] When account and customer details need to be gathered and displayed, the system shall format and display the customer name by concatenating first name, middle initial, and last name; format and display the customer address by concatenating address lines and state code; display the customer phone number; format and display the account credit limit and cash credit limit.

REQ-F-025: [Event-driven] When the authorization summary needs to be retrieved, the system shall schedule the IMS program control block, move the account identifier to the pending authorization summary record, and execute a GU operation to retrieve the authorization summary record using the account identifier as the search key; if successful, set the pending authorization summary segment found flag to 'Y'; if the segment is not found, set the flag to 'N'; if any other error occurs, set the error flag to 'Y', construct an error message with the error code, and display the screen with the error message.

REQ-F-026: [Event-driven] When the authorization summary is found, the system shall display the approved authorization count, declined authorization count, credit balance, cash balance, approved authorization amount, and declined authorization amount; if the authorization summary is not found, display zeros for all authorization summary fields.

REQ-F-027: [Event-driven] When account details need to be gathered and displayed, the system shall set the account identifier input field length to -1; if the account identifier is not low-values, retrieve the card cross-reference record, account record, customer record, and authorization summary, and if the authorization summary is found, populate the authorization list on the screen; if the account identifier is low-values, skip all data retrieval.

REQ-F-028: [Event-driven] When the IMS program control block needs to be scheduled for database access, the system shall retrieve the DIB status and move it to the IMS return code field; if the PSB has been scheduled more than once, retrieve the DIB status again to refresh the status; if the return code does not indicate success, set the error flag to 'Y', construct an error message with the error code, set the account identifier input field length to -1, and display the screen with the error message.

REQ-F-029: [Event-driven] When the next authorization record needs to be retrieved, the system shall execute a GNP operation to retrieve the next authorization record from the pending authorization details database; if successful, set the end-of-file flag to 'N'; if the segment is not found or end of database is reached, set the end-of-file flag to 'Y'; if any other error occurs, set the error flag to 'Y', construct an error message with the error code, set the account identifier input field length to -1, and display the screen with the error message.

REQ-F-030: [Event-driven] When the authorization cursor needs to be repositioned to a saved authorization key, the system shall move the saved authorization key to the authorization key field in the pending authorization details record and execute a GNP operation with a WHERE clause to retrieve the matching authorization record; if successful, set the end-of-file flag to 'N'; if the segment is not found or end of database is reached, set the end-of-file flag to 'Y'; if any other error occurs, set the error flag to 'Y', construct an error message with the error code, set the account identifier input field length to -1, and display the screen with the error message.

REQ-F-031: [Ubiquitous] The system shall extract the authorization amount, reformat the authorization original time from HHMMSS to HH:MM:SS format, and reformat the authorization original date from YYMMDD to MM/DD/YY format, storing both in working variables for subsequent screen population.

REQ-F-032: [Event-driven] When the authorization response code is evaluated, the system shall set the authorization approval status to 'A' when the response code is '00', or set it to 'D' for any other response code value.

REQ-F-033: [Ubiquitous] The system shall clear all five authorization list display rows by setting each row's selection field to protected and blanking all transaction identifier, date, time, type, approval status, match status, and amount fields before populating with retrieved data.

REQ-F-034: [Event-driven] When the index counter is 1, the system shall populate the first authorization row with transaction identifier, formatted date, formatted time, authorization type, approval status, match status, and amount from the pending authorization record, and set the selection field to unprotected.

REQ-F-035: [Event-driven] When the index counter is 2, the system shall populate the second authorization row with transaction identifier, formatted date, formatted time, authorization type, approval status, match status, and amount from the pending authorization record, and set the selection field to unprotected.

REQ-F-036: [Event-driven] When the index counter is 3, the system shall populate the third authorization row with transaction identifier, formatted date, formatted time, authorization type, approval status, match status, and amount from the pending authorization record, and set the selection field to unprotected.

REQ-F-037: [Event-driven] When the index counter is 4, the system shall populate the fourth authorization row with transaction identifier, formatted date, formatted time, authorization type, approval status, match status, and amount from the pending authorization record, and set the selection field to unprotected.

REQ-F-038: [Event-driven] When the index counter is 5 or any other value within the loop, the system shall populate the fifth authorization row with transaction identifier, formatted date, formatted time, authorization type, approval status, match status, and amount from the pending authorization record, and set the selection field to unprotected; when the index counter is outside the range 1–5, the system shall take no action.

REQ-F-039: [Event-driven] When the user presses ENTER on the pending authorization screen, the system shall extract the account identifier and store it in the session context, then evaluate each of the five authorization rows to identify which row the user selected; when a row is selected, record the selection marker and store the corresponding authorization key; when no row is selected, clear both the selection marker and the authorization key to spaces.

REQ-F-040: [Event-driven] When a valid authorization row selection is present (both selection marker and authorization key are non-blank and non-low-value), the system shall validate that the selection marker is either 'S' or 's'; if the marker is any other value, record an error message stating 'Invalid selection. Valid value is S'.

REQ-F-041: [Event-driven] When the user presses a function key on the authorization list screen, the system shall receive the user's input, evaluate the attention identifier, process the account identifier and authorization selection when ENTER is pressed, redisplay the screen when PF3 is pressed, navigate to the previous page when PF7 is pressed, navigate to the next page when PF8 is pressed, and display an error message indicating an invalid key was pressed for any other key.

REQ-F-042: [Ubiquitous] The system shall retrieve the current date and time, reformat the date into MM/DD/YY format and the time into HH:MM:SS format, and populate the screen header fields with the screen titles, transaction identifier, program name, formatted date, and formatted time.

REQ-F-043: [State-driven] While no error flag is set and authorization records are available, the system shall initialize the index counter to 1 and enter a loop that continues until the index counter exceeds 5, the end-of-file flag is set, or an error flag is set; for each iteration, if the PF7 key was pressed and the index counter is 1, reposition to the saved authorization key, otherwise fetch the next authorization record; if a record is successfully retrieved and no error flag is set, populate the screen fields for that authorization and increment the index counter; after the loop completes, if more authorizations are available and no error flag is set, fetch one additional authorization record to determine whether a next page exists.


---


## 4. Authorization Detail Screen Navigation and Fraud Status Management
As an authorization management user, I want to view authorization details and toggle the fraud status of a pending authorization so that I can investigate and flag suspicious transactions.

### Requirements

REQ-F-044: [Event-driven] When the authorization detail program is re-invoked with an existing session context and the program context indicates re-entry, the system shall extract the session context, clear the fraud data field, evaluate the terminal key pressed, and when PF3 is pressed, set the destination program to the authorization summary module and transfer control to that program.

REQ-F-045: [Ubiquitous] The system shall set the originating transaction identifier to CPVD, set the originating program to the authorization detail module, set the program context to entry status, and transfer control to the destination program with the session context.

REQ-F-046: [Event-driven] When the authorization detail program is invoked with an empty communication area, the system shall initialize the session context, set the destination program to the authorization summary module, and transfer control to that program.

REQ-F-047: [Event-driven] When the program receives a non-empty communication area from the caller, the system shall restore the session context from the communication area and route to either initial display or input processing based on the program context flag.

REQ-F-048: [State-driven] While the program is re-entering with user input from the authorization view screen, the system shall receive the user input, evaluate the function key pressed, route to the corresponding action handler (ENTER, PF5, PF8, or invalid key), and refresh the screen display.

REQ-F-049: [Event-driven] When the ENTER key is pressed or an invalid key is pressed, the system shall validate that the account identifier is numeric and the selected authorization key is not empty or low-values; if valid, extract the account identifier and authorization key and retrieve the authorization record; if invalid, set the error flag to indicate an error condition.

REQ-F-050: [Event-driven] When the user presses the ENTER key on the authorization view screen, the system shall process the ENTER key action to retrieve and display the current authorization details.

REQ-F-051: [Event-driven] When the user presses the PF8 key on the authorization view screen, the system shall retrieve the next authorization record and refresh the screen display.

REQ-F-052: [Unwanted] If the user presses an unrecognized key on the authorization view screen, the system shall process the ENTER key action as fallback, display the invalid key error message, and refresh the screen display.

REQ-F-053: [Ubiquitous] The system shall prepare the account identifier and authorization key as lookup parameters before executing database queries for authorization records.

REQ-F-054: [Ubiquitous] The system shall schedule the IMS program control block before accessing the authorization database; if scheduling fails, set the error flag and display an error message.

REQ-F-055: [Event-driven] When the program initiates a database query for the authorization summary, the system shall retrieve the authorization summary by account identifier using a GU operation; if successful, clear the end-of-file flag; if the segment is not found or end of database is reached, set the end-of-file flag; if a system error occurs, set the error flag, construct an error message with the return code, and display the authorization view screen.

REQ-F-056: [Event-driven] When the authorization summary retrieval succeeds and the program initiates a database query for the authorization details, the system shall retrieve the authorization details by authorization key using a GNP operation; if successful, clear the end-of-file flag; if the segment is not found or end of database is reached, set the end-of-file flag; if a system error occurs, set the error flag, construct an error message with the return code, and display the authorization view screen.

REQ-F-057: [Ubiquitous] The system shall retrieve the next authorization details record from the database in sequence; set the end-of-file flag to 'N' if found, or 'Y' if not found or end of database is reached; if an error occurs, set the error flag and display an error message.

REQ-F-058: [Event-driven] When the error flag is clear, the system shall extract the card number, authorization date, authorization time, and approved amount from the pending authorization record; reformat the date into MM/DD/YY format and the time into HH:MM:SS format; and display all values on the authorization view screen.

REQ-F-059: [Ubiquitous] The system shall extract processing code, POS entry mode, authorization source, and merchant category code; reformat the card expiry date into MM/YY format; extract authorization type, transaction identifier, and match status; and extract and display merchant name, merchant identifier, merchant city, merchant state, and merchant ZIP code on the authorization view screen.

REQ-F-060: [Event-driven] When the decline reason table is searched for a matching code, the system shall display the reason code and description formatted as 'code-description' when found; when not found, display '9999-ERROR'.

REQ-F-061: [Event-driven] When the authorization response code is evaluated, the system shall set the authorization approval status to 'A' (approved) when the response code is '00', or set it to 'D' (declined) for any other response code value.

REQ-F-062: [Ubiquitous] The system shall retrieve the current system date and time, format the date into MM/DD/YY format and the time into HH:MM:SS format, and populate the screen header fields with the titles, transaction identifier, program name, formatted date, and formatted time.

REQ-F-063: [Event-driven] When the PF5 key is pressed, the system shall retrieve the authorization record for the selected account and authorization key, check whether the fraud flag is set, toggle the fraud flag (set if not set, clear if set), assemble the fraud-action parameters, delegate to the fraud-processing service to update the fraud status, and if the update succeeds, update the authorization details in the database; if the update fails, display the error message returned by the fraud-processing service.

REQ-F-064: [Event-driven] When a CICS transaction arrives with a communication area and the user presses PF5 on a re-entry transaction, the system shall extract the session context into the card-demo context record and dispatch to the fraud-marking handler.

REQ-F-065: [Event-driven] When the fraud-marking handler is invoked after PF5 is pressed, the system shall retrieve the pending authorization record, toggle the fraud status (confirmed to removed, or removed to confirmed), delegate to the fraud-processing service to validate the change, and invoke the update handler when the service succeeds.

REQ-F-066: [Ubiquitous] The system shall move the fraud authorization record into the pending authorization details buffer and execute a DLI REPL operation to update the authorization detail segment in the database.

REQ-F-067: [Ubiquitous] The system shall move the fraud authorization record to the authorization details record, update the authorization record in the database, and if successful, set a message indicating whether the fraud flag was removed ('AUTH FRAUD REMOVED...') or the authorization was marked as fraudulent ('AUTH MARKED FRAUD...'); if an error occurs, set the error flag, construct an error message indicating a system error during fraud tagging with a rollback notification and the IMS return code, and display the error message.

REQ-F-068: [Event-driven] When the read-authorization-record handler is invoked, the system shall execute a GU operation to retrieve the pending authorization summary by account identifier; if successful, set the not-end-of-file flag; if not found or end of database is reached, set the end-of-file flag; when not-end-of-file is set, execute a GNP operation to retrieve the pending authorization detail by authorization key.


---


## 5. Fraud Authorization Record Insertion and Update
As an authorization management system, I want fraud events to be recorded in the fraud authorization store so that flagged authorizations are persisted with full transaction and merchant details.

### Requirements

REQ-F-069: [Event-driven] When a fraud authorization record is submitted for recording, the system shall extract the year, month, and day from the original authorization date; compute the authorization time by subtracting the input authorization time numeric from 999999999; and decompose the result into hour, minute, second, and millisecond components to form the formatted authorization timestamp.

REQ-F-070: [Event-driven] When the authorization timestamp has been derived, the system shall populate all fields of the fraud authorization record from the input parameters, including card number, authorization timestamp, authorization type, card expiry date, message type, message source, authorization identifier code, authorization response code, authorization response reason, processing code, transaction amount, approved amount, merchant category code, acquirer country code, POS entry mode, merchant identifier, merchant name, merchant city, merchant state, merchant ZIP code, transaction identifier, match status, fraud indicator, account identifier, and customer identifier.

REQ-F-071: [Event-driven] When the fraud authorization record is fully assembled, the system shall insert the complete fraud authorization record into the fraud authorization store (CARDDEMO.AUTHFRDS), recording all transaction, card, merchant, and fraud classification details along with the current date as the fraud report date.

REQ-F-072: [Ubiquitous] The system shall retrieve the current system time, format it as an 8-character date in MM/DD/YY format, and record it as the fraud report date on the fraud authorization record.

REQ-F-073: [Event-driven] When the fraud record insert operation completes successfully, the system shall set the fraud update status to succeeded and record the fraud action message as 'ADD SUCCESS'.

REQ-F-074: [Event-driven] When the fraud record insert fails with a duplicate key error (SQL return code -803), the system shall delegate processing to the fraud update routine to attempt an update of the existing fraud record.

REQ-F-075: [Unwanted] If the fraud record insert fails with an unexpected database error (not a duplicate key), the system shall set the fraud update status to failed, capture the SQL return code and SQL state code, and compose a system error message in the fraud action message.

REQ-F-076: [Event-driven] When a duplicate-key conflict is detected (SQL return code equals -803), the system shall invoke the fraud record update to overwrite the existing entry in the authorization fraud store.

REQ-F-077: [Event-driven] When a duplicate-key conflict is confirmed and the fraud record update is invoked, the system shall update the fraud indicator and set the fraud report date to the current date in the authorization fraud store (CARDDEMO.AUTHFRDS) for the row matching the card number and authorization timestamp, where the stored timestamp is parsed using the format 'YY-MM-DD HH24.MI.SSNNNNNN'.

REQ-F-078: [Ubiquitous] The system shall extract the year, month, and day from the original authorization date; compute the time-of-day by subtracting the input authorization time numeric from 999999999 and decompose it into hour, minute, second, and millisecond components; then populate the fraud record with the card number, the assembled timestamp, and the fraud action code.

REQ-F-079: [Event-driven] When the fraud record update operation completes, the system shall set the fraud update status to succeeded and record 'UPDT SUCCESS' in the fraud action message when the update succeeds; or set the status to failed, capture the SQL return code and SQL state code, and compose an update error message in the fraud action message when the update fails.


---


## 6. Sign-on Authentication and Session Routing
As a user, I want to authenticate with my credentials so that I am routed to the appropriate menu based on my user type.

### Requirements

REQ-F-080: [Event-driven] When the user submits the signon form by pressing ENTER, the system shall receive the input, validate that the user identifier and password are both non-empty, display the message 'Please enter User ID ...' and redisplay the screen if the user identifier is empty, display the message 'Please enter Password ...' and redisplay the screen if the password is empty, convert both fields to uppercase and store them for verification if both are populated, and proceed to security file lookup when validation succeeds.

REQ-F-081: [Event-driven] When the user identifier and password have been collected and validated as non-empty, the system shall read the user security data store (WS-USRSEC-FILE) using the user identifier as the key; if the read succeeds and the retrieved password matches the entered password, populate the session context with the user's transaction identifier, program name, user identifier, and user type, and transfer control to the administrator menu program if the user type is administrator, or to the general menu program otherwise; if the read fails with not-found, invoke the not-found error-handling branch; if the read fails with any other error, invoke the system error-handling branch.

REQ-F-082: [Event-driven] When the user submits valid non-empty credentials, the system shall read the user security data store using the user identifier as the key; if the passwords do not match, display 'Wrong Password. Try again ...' and redisplay the screen; if the user identifier is not found, display 'User not found. Try again ...' and redisplay the screen; if a system error occurs, display 'Unable to verify the User ...' and redisplay the screen.

REQ-F-083: [Event-driven] When the Enter key is pressed on the sign-on screen, the system shall invoke the enter-key processing logic to collect and validate credentials.

REQ-F-084: [Event-driven] When the user presses a key on the terminal, the system shall route to the signon-entry processor when ENTER is pressed, or display an invalid-key error message and redisplay the screen for any other unrecognized key.

REQ-F-085: [Ubiquitous] The system shall set the error flag to off at program entry before any validation processing begins.


---


## 7. Authorization Detail Screen Navigation
As an authorization management user, I want to navigate from the authorization detail view back to the authorization summary screen so that I can review the list of pending authorizations.

### Requirements

REQ-F-086: [Event-driven] When the program is invoked with an empty communication area, the system shall initialize the communication area, set the destination program to the authorization summary module, and transfer control to that program.

REQ-F-087: [Event-driven] When the program is re-invoked with an existing communication area and the program context indicates re-entry, the system shall extract the communication area, clear the fraud data field, evaluate the terminal key pressed, and when PF3 is pressed, set the destination program to the authorization summary module and transfer control to that program.

REQ-F-088: [Ubiquitous] The system shall set the originating transaction identifier to CPVD, set the originating program to the authorization detail module, set the program context to entry status, and transfer control to the destination program with the communication area.


---


## 8. Authorization Detail Display and Fraud Detection
As an authorization management user, I want to view pending authorization transaction details including merchant and fraud status information so that I can identify and act on potentially fraudulent authorizations.

### Requirements

REQ-F-089: [Event-driven] When the program receives a non-empty communication area from the caller, the system shall restore the session context from the communication area and route to either initial display or input processing based on the program context flag.

REQ-F-090: [Ubiquitous] The system shall schedule the program specification block for database access before executing any database queries; if scheduling fails, the system shall set the error flag and display an error message.

REQ-F-091: [Ubiquitous] The system shall prepare the account identifier and authorization key as lookup parameters before executing database queries.

REQ-F-092: [Event-driven] When the program initiates a database query for the authorization summary, the system shall retrieve the authorization summary by account identifier from the pending authorization data store; when successful, clear the end-of-file flag; when the segment is not found or end-of-database is reached, set the end-of-file flag; when a system error occurs, set the error flag, construct an error message with the return code, and display the authorization view screen.

REQ-F-093: [Event-driven] When the authorization summary retrieval succeeds and the program initiates a database query for the authorization details, the system shall retrieve the authorization details by authorization key; when successful, clear the end-of-file flag; when the segment is not found or end-of-database is reached, set the end-of-file flag; when a system error occurs, set the error flag, construct an error message with the return code, and display the authorization view screen.

REQ-F-094: [Ubiquitous] The system shall retrieve the next authorization details record from the pending authorization data store in sequence; set the end-of-file flag to indicate no more records if not found or end-of-database is reached; if an error occurs, set the error flag and display an error message.

REQ-F-095: [Event-driven] When the error flag is clear, the system shall extract the card number, authorization date, authorization time, and approved amount from the pending authorization record; reformat the date into MM/DD/YY format and the time into HH:MM:SS format; and display all values on the authorization view screen.

REQ-F-096: [Ubiquitous] The system shall extract processing code, POS entry mode, authorization source, and merchant category code; reformat the card expiry date into MM/YY format; extract authorization type, transaction ID, and match status; and extract and display merchant name, merchant ID, merchant city, merchant state, and merchant ZIP code on the authorization view screen.

REQ-F-097: [Event-driven] When the decline reason table is searched for a matching code, the system shall search the decline reason table for a matching decline code; when found, display the reason code and description formatted as 'code-description'; when not found, display '9999-ERROR'.

REQ-F-098: [Event-driven] When the authorization response code is evaluated, the system shall set the authorization approval status to 'A' (approved) when the response code is '00', or set it to 'D' (declined) for any other response code value.

REQ-F-099: [Ubiquitous] The system shall retrieve the current system date and time, format the date into MM/DD/YY format and the time into HH:MM:SS format, and populate the screen header fields with the titles, transaction identifier, program name, formatted date, and formatted time.

REQ-F-100: [Ubiquitous] The system shall receive the authorization screen input from the terminal into the screen input buffer.

REQ-F-101: [State-driven] While the program is re-entering with user input from the authorization view screen, the system shall receive the user input, evaluate the function key pressed, route to the corresponding action handler (ENTER, PF5, PF8, or invalid key), and refresh the screen display.

REQ-F-102: [Event-driven] When the ENTER key is pressed or an invalid key is pressed, the system shall validate that the account identifier is numeric and the selected authorization key is not empty or low-values; if valid, extract the account identifier and authorization key and retrieve the authorization record; if invalid, set the error flag to indicate an error condition.

REQ-F-103: [Event-driven] When the user presses the ENTER key on the authorization view screen, the system shall process the ENTER key action to retrieve authorization details and refresh the screen display.

REQ-F-104: [Event-driven] When the user presses the PF8 key on the authorization view screen, the system shall process the PF8 key action to retrieve the next authorization record and refresh the screen display.

REQ-F-105: [Unwanted] If the user presses an unrecognized key on the authorization view screen, the system shall process the ENTER key action as fallback, display the invalid key error message, and refresh the screen display.


---


## 9. Authorization Fraud Status Update
As an authorization management user, I want to toggle the fraud status of a pending authorization so that confirmed fraudulent transactions are flagged and erroneous fraud flags can be removed.

### Requirements

REQ-F-106: [Event-driven] When a communication area is received and the user presses PF5 on a re-entry transaction, the system shall extract the communication area into the session context record and dispatch to the fraud-marking handler.

REQ-F-107: [Event-driven] When the PF5 key is pressed, the system shall retrieve the authorization record, check whether the fraud flag is set, toggle the fraud flag (set if not set, clear if set), assemble the fraud-action parameters, delegate to the fraud-processing service to validate the change, and if the update succeeds, update the authorization details in the pending authorization data store; if the update fails, display the error message.

REQ-F-108: [Event-driven] When the fraud-marking handler is invoked after PF5 is pressed, the system shall retrieve the pending authorization record, toggle the fraud status (confirmed to removed, or removed to confirmed), delegate to the fraud-processing service to validate the change, and invoke the update handler when the service succeeds.

REQ-F-109: [Event-driven] When the read-authorization-record handler is invoked, the system shall execute a GU operation to retrieve the pending authorization summary by account identifier; evaluate the return code (success sets not-end-of-file, not-found or end-of-database sets end-of-file); and when not-end-of-file is set, execute a GNP operation to retrieve the pending authorization detail by authorization key.

REQ-F-110: [Ubiquitous] The system shall move the fraud authorization record into the pending authorization details buffer and execute a replace operation to update the authorization detail segment in the pending authorization data store with the new fraud status.

REQ-F-111: [Ubiquitous] The system shall move the fraud authorization record to the authorization details record, update the authorization record in the pending authorization data store, and if successful, set a message indicating whether the fraud flag was removed ('AUTH FRAUD REMOVED...') or the authorization was marked as fraudulent ('AUTH MARKED FRAUD...'); if an error occurs, set the error flag, construct an error message indicating a system error during fraud tagging with a rollback notification and the return code, and display the error message.


---


## 10. Authorization Selection and Summary Navigation (Authorization Summary Screen)
As an authorization management user, I want to select a pending authorization from the summary list and navigate to its detail screen so that I can review and act on individual authorization records.

### Requirements

REQ-F-112: [Event-driven] When the program receives control with a communication area, the system shall copy the incoming communication area into the working communication area when the length is non-zero, or execute first-time initialization when the length is zero.

REQ-F-113: [Event-driven] When a communication area is received from the caller, the system shall extract the account identifier from the communication area; validate that it is numeric; if numeric, store it in the account identifier field and display it; if non-numeric, clear the account identifier field and display a space; then check the program re-entry context flag to determine whether to gather account details on first entry or receive user input on re-entry.

REQ-F-114: [Event-driven] When the program is re-entering and the user presses PF3, the system shall set the destination program to the menu program.

REQ-F-115: [Ubiquitous] The system shall retrieve the user's input from the authorization selection screen, including the account identifier and the five authorization selection flags.

REQ-F-116: [Ubiquitous] The system shall retrieve user input from the authorization selection screen, store the input in the screen buffer, and capture the response and reason codes.

REQ-F-117: [Event-driven] When the user presses a function key on the authorization selection screen, the system shall evaluate the key pressed and route to the ENTER-key handler when ENTER is pressed, or route to alternative handlers for PF3, PF7, PF8, or other keys.

REQ-F-118: [Event-driven] When the user presses ENTER on the pending authorization screen, the system shall extract the account identifier and store it in the shared context; evaluate each of the five authorization rows to identify which row the user selected; when a row is selected, record the user's selection marker and store the corresponding authorization key; when no row is selected, clear both the selection marker and the authorization key to spaces.

REQ-F-119: [Event-driven] When the user submits the authorization selection form, the system shall verify that the account identifier is not empty; if empty, execute the error handler; if not empty, proceed to numeric validation.

REQ-F-120: [Event-driven] When the account identifier has been confirmed as non-empty, the system shall verify that the account identifier contains only numeric characters; if non-numeric, execute the error handler; if numeric, proceed to selection processing.

REQ-F-121: [Event-driven] When the user presses ENTER with an empty account identifier field, the system shall clear the account identifier field and display the message 'Please enter Acct Id...'.

REQ-F-122: [Event-driven] When the user enters a non-numeric account identifier, the system shall clear the account identifier field and display the message 'Acct Id must be Numeric ...'.

REQ-F-123: [Ubiquitous] The system shall evaluate the five authorization selection options and extract the selected authorization key; if option 1 is selected, retrieve key 1; if option 2 is selected, retrieve key 2; if option 3 is selected, retrieve key 3; if option 4 is selected, retrieve key 4; if option 5 is selected, retrieve key 5; if no option is selected, clear both the selection flag and the selected value.

REQ-F-124: [Event-driven] When no authorization option is selected, the system shall clear the selection flag and selected-value field to indicate no selection.

REQ-F-125: [Event-driven] When a valid authorization row selection is present (both selection marker and authorization key are non-blank and non-low-value), the system shall validate that the selection marker is either 'S' or 's'; when the marker is 'S' or 's', accept the selection; when the marker is any other value, record an error message stating 'Invalid selection. Valid value is S'.

REQ-F-126: [Event-driven] When the authorization selection evaluation is complete, the system shall verify that both the selection flag and the selected-value field are non-empty; if both are populated, proceed to program navigation; if either is empty, skip navigation.

REQ-F-127: [Event-driven] When the selection flag is 'S' or 's', the system shall set the destination program to the authorization details screen program, set the originating transaction identifier to CPVS, set the originating program name to the authorization summary screen program, set the program context indicator to initial entry, and transfer control to the destination program with the communication area.

REQ-F-128: [Event-driven] When the authorization summary selection is confirmed, the system shall populate the communication area with the destination program name, originating transaction identifier, originating program name, and program context indicator, then transfer control to the authorization details program.

REQ-F-129: [Ubiquitous] The system shall copy the validated account identifier from the screen input into the working storage variable and the communication area.


---


## 11. Pending Authorization List Display
As an authorization management user, I want to view a paginated list of pending authorizations for an account so that I can identify which authorizations require review.

### Requirements

REQ-F-130: [Event-driven] When account and customer details need to be gathered and displayed, the system shall retrieve the card cross-reference record from the card cross-reference data store to obtain the customer identifier; retrieve the account record from the account file to obtain credit limits; retrieve the customer record from the customer file to obtain customer name and address information; format and display the customer name, address, and phone number; format and display the account credit limit and cash credit limit; retrieve the authorization summary from the pending authorization summary data store; if the authorization summary is found, display the approved authorization count, declined authorization count, credit balance, cash balance, approved authorization amount, and declined authorization amount; if the authorization summary is not found, display zeros for all authorization summary fields.

REQ-F-131: [Event-driven] When account details need to be gathered and displayed, the system shall set the account identifier input field length to -1; if the account identifier is not low-values, retrieve the card cross-reference record, account record, customer record, and authorization summary, and if the authorization summary is found, populate the authorization list on the screen; if the account identifier is low-values, skip all data retrieval.

REQ-F-132: [Event-driven] When the card cross-reference record needs to be retrieved by account identifier, the system shall execute a read operation to retrieve the card cross-reference record from the card cross-reference data store using the account identifier as the key; if not found, construct an error message indicating the account was not found in the cross-reference data store and display the screen with the error message; if any other error occurs, construct an error message describing the system error with the response and reason codes and display the screen with the error message.

REQ-F-133: [Event-driven] When the account record needs to be retrieved by account identifier, the system shall execute a read operation to retrieve the account record from the account file using the account identifier as the key; if not found, construct an error message indicating the account was not found in the account data store and display the screen with the error message; if any other error occurs, construct an error message describing the system error with the response and reason codes and display the screen with the error message.

REQ-F-134: [Event-driven] When the customer record needs to be retrieved by customer identifier, the system shall execute a read operation to retrieve the customer record from the customer file using the customer identifier as the key; if not found, construct an error message indicating the customer was not found in the customer data store and display the screen with the error message; if any other error occurs, construct an error message describing the system error with the response and reason codes and display the screen with the error message.

REQ-F-135: [Event-driven] When the authorization summary needs to be retrieved from the pending authorization summary data store, the system shall schedule the program specification block, move the account identifier to the pending authorization summary record, and execute a GU operation; if successful, set the pending authorization summary segment found flag to 'Y'; if the segment is not found, set the flag to 'N'; if any other error occurs, set the error flag, construct an error message with the error code, and display the screen with the error message.

REQ-F-136: [Event-driven] When the next authorization record needs to be retrieved from the pending authorization details data store, the system shall execute a GNP operation; if successful, set the end-of-file flag to 'N'; if the segment is not found or end-of-database is reached, set the end-of-file flag to 'Y'; if any other error occurs, set the error flag, construct an error message with the error code, and display the screen with the error message.

REQ-F-137: [Event-driven] When the IMS program control block needs to be scheduled for database access, the system shall retrieve the DIB status; if the PSB has been scheduled more than once, refresh the status; if the status does not indicate success, set the error flag, construct an error message with the error code, and display the screen with the error message.

REQ-F-138: [Event-driven] When the authorization cursor needs to be repositioned to a saved authorization key, the system shall execute a GNP operation with a WHERE clause to retrieve the authorization record matching that key; if successful, set the end-of-file flag to 'N'; if the segment is not found or end-of-database is reached, set the end-of-file flag to 'Y'; if any other error occurs, set the error flag, construct an error message with the error code, and display the screen with the error message.

REQ-F-139: [Ubiquitous] The system shall extract the authorization amount, reformat the authorization original time from HHMMSS to HH:MM:SS format, reformat the authorization original date from YYMMDD to MM/DD/YY format, and store both in working variables for subsequent screen population.

REQ-F-140: [Ubiquitous] The system shall clear each of the five authorization list rows by setting each row's selection field to protected and blanking all transaction, date, time, type, approval, status, and amount fields before populating with retrieved data.

REQ-F-141: [Event-driven] When the index counter is 1, the system shall populate the first authorization row on the screen with transaction ID, date, time, type, approval status, match status, and amount, and set the selection field to unprotected for user interaction.

REQ-F-142: [Event-driven] When the index counter is 2, the system shall populate the second authorization row on the screen with transaction ID, date, time, type, approval status, match status, and amount, and set the selection field to unprotected for user interaction.

REQ-F-143: [Event-driven] When the index counter is 3, the system shall populate the third authorization row on the screen with transaction ID, date, time, type, approval status, match status, and amount, and set the selection field to unprotected for user interaction.

REQ-F-144: [Event-driven] When the index counter is 4, the system shall populate the fourth authorization row on the screen with transaction ID, date, time, type, approval status, match status, and amount, and set the selection field to unprotected for user interaction.

REQ-F-145: [Event-driven] When the index counter is 5 or any other value, the system shall populate the fifth authorization row on the screen with transaction ID, date, time, type, approval status, match status, and amount, and set the selection field to unprotected for user interaction; take no action when the index counter is outside the range 1–5.

REQ-F-146: [Event-driven] When the user presses a function key on the authorization list screen, the system shall receive the user's input; evaluate the attention identifier to determine which function key was pressed; if ENTER is pressed, process the account identifier and authorization selection; if PF3 is pressed, redisplay the screen; if PF7 is pressed, navigate to the previous page of authorizations; if PF8 is pressed, navigate to the next page of authorizations; if any other key is pressed, display an error message indicating an invalid key was pressed and redisplay the screen.

REQ-F-147: [Ubiquitous] The system shall retrieve the current date and time, reformat the date into MM/DD/YY format and the time into HH:MM:SS format, and populate the screen header fields with the screen titles, transaction identifier, program name, formatted date, and formatted time.


### Open Questions

OQ-001: Rule 9c1c9b5d (COPAUS1C) and rule ef9a89cb (COPAUS0C) both describe PSB scheduling with overlapping logic. Should these be treated as a single shared scheduling service, or are they independent scheduling operations in each program? — Owner: Authorization Management team

OQ-002: Rule fe326c7e describes "the authorization details population paragraph completes" with no observable business action. Is there a business postcondition (e.g., a status flag set, a record written) that should be captured, or is this purely a structural termination point? — Owner: Authorization Management team


---


## 12. Authorization Selection and Summary Navigation (Authorization Summary Screen)
As an authorization management user, I want to select a specific authorization from the summary list so that I can view or act on its details.

### Requirements

REQ-F-148: [Event-driven] When the user selects authorization option 1, the system shall record the selection flag and retrieve the authorization key for option 1.

REQ-F-149: [Event-driven] When the user selects authorization option 2, the system shall record the selection flag and retrieve the authorization key for option 2.

REQ-F-150: [Event-driven] When the user selects authorization option 3, the system shall record the selection flag and retrieve the authorization key for option 3.

REQ-F-151: [Event-driven] When the user selects authorization option 4, the system shall record the selection flag and retrieve the authorization key for option 4.

REQ-F-152: [Event-driven] When the user selects authorization option 5, the system shall record the selection flag and retrieve the authorization key for option 5.


---


## 13. Expired Authorization Record Cleanup
As a batch operations team, I want expired pending authorization records identified and removed daily so that the authorization data store reflects only active, non-expired authorizations and summary totals remain accurate.

**Restart/Recovery:** The job processes authorization summary and detail records sequentially. Deletions are applied record by record; if the job is interrupted, partially processed summaries may remain with adjusted counts but undeleted summary records.

### Requirements

REQ-F-153: [Ubiquitous] The system shall accept the current date in YYDDD format and read the expiry threshold parameter from input; if the parameter is numeric, the system shall use it as the expiry days threshold, otherwise the system shall default to 5 days.

REQ-F-154: [Event-driven] When the system enters the summary-retrieval phase or advances to the next summary record, the system shall retrieve the next pending authorization summary record from the authorization data store; when retrieval succeeds, the system shall clear the end-of-database flag; when the data store is exhausted, the system shall set the end-of-database flag.

REQ-F-155: [Event-driven] When the system enters the detail-retrieval phase or advances to the next detail record under a summary, the system shall retrieve the next authorization detail record associated with the current summary; when retrieval succeeds, the system shall set the more-authorizations flag; when the segment or data store is exhausted, the system shall clear the more-authorizations flag.

REQ-F-156: [Event-driven] When an authorization detail record is evaluated for expiry, the system shall compute the authorization age by subtracting the stored authorization date from 99999 and then calculating the difference between the current date and that computed value; when the difference is greater than or equal to the expiry threshold and the response code is '00' (approved), the system shall decrement the approved authorization count by 1 and subtract the approved amount from the summary's total approved authorization amount; when the difference is greater than or equal to the expiry threshold and the response code is not '00' (declined), the system shall decrement the declined authorization count by 1 and subtract the transaction amount from the summary's total declined authorization amount; when the difference is less than the expiry threshold, the system shall mark the authorization as not qualified for deletion.

REQ-F-157: [Event-driven] When an authorization detail record qualifies for deletion based on expiry evaluation, the system shall delete that authorization detail record from the authorization data store.

REQ-F-158: [Event-driven] When all authorization detail records for a summary have been processed and both the approved authorization count and the declined authorization count are zero or less, the system shall delete the pending authorization summary record from the authorization data store.

REQ-F-159: [State-driven] While authorization summary records remain to be processed and no error has occurred, the system shall retrieve each pending authorization summary, process all associated authorization detail records for expiry evaluation and count adjustment, delete the summary record when both approved and declined counts reach zero or less, and then advance to the next summary record.


### Open Questions

OQ-003: The expiry age calculation inverts the stored authorization date using the formula 99999 minus the stored date before computing the day difference against the current YYDDD date. The intent of this inversion is not explicitly stated in the rules. Confirmation is needed that this arithmetic correctly represents elapsed calendar days for all valid YYDDD values and that no wrap-around edge cases exist near year boundaries. — Owner: Authorization Management domain team

OQ-004: The rule states that a summary record is deleted when both approved and declined counts are "zero or less." It is unclear whether negative counts represent a data integrity anomaly that should be flagged rather than silently treated as a deletion trigger. — Owner: Authorization Management domain team


---


## 14. Authorization Expiration Cleanup
As a batch operations team, I want expired authorization records removed from the authorization data store daily so that pending authorization counts and amounts remain accurate and stale records do not accumulate.

**Restart/Recovery:** The cleanup processes authorization summary and detail records sequentially. Deletions are applied record by record; if the job is interrupted, partially processed summaries may remain with some detail records already deleted.

### Requirements

REQ-F-160: [Ubiquitous] The system shall accept the current date in YYDDD format and read the expiry threshold parameter from input; if the parameter value is numeric, the system shall use it as the expiry days threshold, otherwise the system shall default to 5 days.

REQ-F-161: [State-driven] While authorization records remain to be processed and no error has occurred, the system shall retrieve each pending authorization summary record from the authorization data store and process all associated authorization detail records under that summary.

REQ-F-162: [Event-driven] When the system enters the summary-retrieval phase or advances to the next summary record, the system shall retrieve the next authorization summary record from the authorization data store; when retrieval succeeds, the system shall clear the end-of-database flag; when the database segment is exhausted, the system shall set the end-of-database flag.

REQ-F-163: [Event-driven] When the system enters the detail-retrieval phase or advances to the next detail record under a summary, the system shall retrieve the next authorization detail record under the current summary; when retrieval succeeds, the system shall set the more-authorizations flag; when the segment or database is exhausted, the system shall clear the more-authorizations flag.

REQ-F-164: [Event-driven] When an authorization detail record is evaluated for expiry, the system shall compute the authorization age by subtracting the stored authorization date from 99999 and then calculating the difference between the current date and that computed value; when the day difference is greater than or equal to the expiry threshold, the authorization shall be considered expired.

REQ-F-165: [Event-driven] When an authorization detail record is determined to be expired and its response code is '00' (approved), the system shall decrement the approved authorization count by 1 and subtract the approved amount from the total approved authorization amount in the summary.

REQ-F-166: [Event-driven] When an authorization detail record is determined to be expired and its response code is not '00' (declined), the system shall decrement the declined authorization count by 1 and subtract the transaction amount from the total declined authorization amount in the summary.

REQ-F-167: [Event-driven] When an authorization detail record qualifies for deletion based on expiry evaluation, the system shall delete that authorization detail record from the authorization data store.

REQ-F-168: [Event-driven] When all authorization detail records for a summary have been processed and both the approved authorization count and the declined authorization count are zero or less, the system shall delete the pending authorization summary record from the authorization data store.


---


## 15. Authorization Request Processing and Response Delivery
As a batch operations team, I want pending authorization requests processed from a message queue so that card transactions are approved or declined based on available credit and responses are returned to requestors.

**Restart/Recovery:** Each authorization request is committed individually after processing. If the job is interrupted, previously committed authorizations are not reprocessed. The IMS pending authorization summary is updated in place; partial updates within a single message cycle are rolled back on critical error via the end-routine handler.

### Requirements

REQ-F-169: [Ubiquitous] The system shall retrieve trigger event data to obtain the request queue name and set the message wait interval to 5000 milliseconds before opening the request queue.

REQ-F-170: [Event-driven] When the request queue open operation is initiated, the system shall open the request queue for input-shared access; if successful, set the request-queue-open flag; if unsuccessful, log a critical error with location 'M001' and delegate to the error handler.

REQ-F-171: [State-driven] While messages remain available in the request queue and the message-processed count has not exceeded 500, the system shall extract and parse each incoming authorization request, process the authorization decision, increment the message-processed counter, commit the transaction, reset the IMS PSB-scheduled flag to 'N', and read the next message; when the message-processed count exceeds 500, the system shall set the loop-end flag to stop processing.

REQ-F-172: [Event-driven] When the next authorization request message is requested from the queue, the system shall retrieve the message using no-syncpoint, wait, convert, and fail-if-quiescing options; if successful, extract the correlation ID and reply-to queue name for response routing; if no messages are available, set the no-more-messages flag; if any other failure occurs, log a critical error with location 'M003' and delegate to the error handler.

REQ-F-173: [Event-driven] When IMS PSB scheduling is required for database operations, the system shall check the IMS database status indicator; if status is OK, set the PSB-scheduled flag to 'Y'; if status indicates PSB-scheduled-more-than-once, re-read the status; if the final status is not OK, log a critical error with location 'I001' and delegate to the error handler.

REQ-F-174: [Event-driven] When the card number from the authorization request is looked up in the card cross-reference data store (AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS), the system shall read the cross-reference record using the card number as key; if found, set the card-found flag; if not found, set the card-not-found and account-not-found flags and log a warning with location 'A001'; if any other error occurs, log a critical error with location 'C001' and delegate to the error handler.

REQ-F-175: [Event-driven] When the account ID from the cross-reference record is looked up in the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS), the system shall read the account record using the account ID as key; if found, set the account-found flag; if not found, set the account-not-found flag and log a warning with location 'A002'; if any other error occurs, log a critical error with location 'C002' and delegate to the error handler.

REQ-F-176: [Event-driven] When the customer ID from the cross-reference record is looked up in the customer data store (AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS), the system shall read the customer record using the customer ID as key; if found, set the customer-found flag; if not found, set the customer-not-found flag and log a warning with location 'A003'; if any other error occurs, log a critical error with location 'C003' and delegate to the error handler.

REQ-F-177: [Event-driven] When the pending authorization summary segment is retrieved from the IMS database for the account, the system shall retrieve the segment using the account ID as search criterion; if found, set the summary-found flag; if not found, set the summary-not-found flag; if any other error occurs, log a critical error with location 'I002' and delegate to the error handler.

REQ-F-178: [Event-driven] When the pending authorization summary segment is not found in the IMS database, the system shall initialize the summary record with zero values and populate the account and customer identifiers from the card cross-reference data.

REQ-F-179: [Ubiquitous] The system shall populate the credit limit and cash credit limit in the pending authorization summary from the account record.

REQ-F-180: [Complex] While authorization data is available from either the pending authorization summary or the account record, when the transaction amount is compared against available credit, the system shall calculate available credit as credit limit minus credit balance from the pending authorization summary if the summary exists, or as account credit limit minus current account balance if only the account record is found; if the transaction amount exceeds the calculated available credit, the system shall decline the authorization and set the insufficient-funds indicator; if neither the summary nor the account record is found, the system shall decline the authorization without a specific reason.

REQ-F-181: [Event-driven] When the authorization is declined and a specific decline reason must be mapped to a response reason code, the system shall set the authorization response reason code to '3100' when the card or account is not found; '4100' when funds are insufficient; '4200' when the card is not active; '4300' when the account is closed; '5100' when card fraud is detected; '5200' when merchant fraud is detected; '9000' for any other decline reason; and leave it as '0000' when the authorization is approved.

REQ-F-182: [Event-driven] When the decline flag indicates whether the authorization should be approved or declined, the system shall set the authorization response code to '05' and the approved amount to zero when declined; set the authorization response code to '00' and the approved amount to the transaction amount when approved.

REQ-F-183: [Ubiquitous] The system shall populate the authorization response header with the card number, transaction identifier, and authorization time from the incoming request.

REQ-F-184: [Ubiquitous] The system shall format the approved amount for display and construct the authorization response message by concatenating the card number, transaction identifier, authorization ID code, response code, reason code, and formatted approved amount into the reply buffer.

REQ-F-185: [Event-driven] When the authorization response is ready to be sent to the reply queue, the system shall send the response using MQPUT1 with the saved correlation ID for tracking, message type reply, non-persistent persistence, and message expiry of 50; if the send fails, log a critical error with location 'M004' and delegate to the error handler.

REQ-F-186: [Event-driven] When the authorization response is approved, the system shall increment the approved authorization count, add the approved amount to the approved authorization total and credit balance, and reset the cash balance to zero.

REQ-F-187: [Event-driven] When the authorization response is declined, the system shall increment the declined authorization count and add the transaction amount to the declined authorization total.

REQ-F-188: [Ubiquitous] The system shall replace the existing pending authorization summary segment in the IMS database if found, or insert a new segment if not found.

REQ-F-189: [Unwanted] If the IMS database operation to update the pending authorization summary fails, the system shall populate the error log record with location code 'I003', critical level, IMS subsystem code, the IMS return code, failure message, and card number, then delegate to the error-logging routine.

REQ-F-190: [Event-driven] When the IMS database operation completes successfully, the system shall capture the IMS return status code and continue processing.

REQ-F-191: [Event-driven] When the authorization detail must be recorded in the IMS database, the system shall convert the current system time to compressed numeric format using the inverse calculation (99999 minus year-day-of-year for the date key; 999999999 minus time-with-milliseconds for the time key), populate the detail record with all authorization request fields (card number, authorization type, card expiry date, message type, message source, processing code, transaction amount, merchant information), authorization response fields (authorization ID code, response code, response reason, approved amount), and match status (pending if approved, declined if declined), then insert the detail segment as a child of the pending authorization summary using the account ID as the parent key; if the insert fails, log a critical error with location 'I004' and delegate to the error handler.

REQ-F-192: [Event-driven] When an error condition is detected, the system shall retrieve the current system time formatted as YYMMDD and HHMMSS, populate the error log record with transaction ID, program code, date, time, and error details (location, level, subsystem, codes, message, event key), and write the error log record to the audit queue; if the error is critical, the system shall delegate to the end-routine handler to terminate the program.

REQ-F-193: [Event-driven] When a critical error has been logged and the program must terminate, the system shall invoke the end-routine handler to terminate the program and initiate recovery procedures.

REQ-F-194: [Event-driven] When program termination requires closing the request queue, the system shall close the request queue if the request-queue-open flag is set; if the close fails, log a warning error with location 'M005' and delegate to the error handler.


### Non-Functional Requirements

REQ-N-001: [Event-driven] When each authorization request has been processed and a response sent, the system shall commit the transaction before proceeding to the next message, ensuring each authorization is an atomic unit of work.


---


## 16. Fraud Authorization Record Recording
As a batch operations team, I want flagged card authorizations recorded as fraud events in the authorization fraud data store so that fraud incidents are captured with full transaction, card, and merchant detail for downstream investigation.

### Requirements

REQ-F-195: [Ubiquitous] The system shall retrieve the current system date, format it as an 8-character date in MM/DD/YY format, and record it as the fraud report date on the fraud authorization record before any insert or update operation is attempted.

REQ-F-196: [Event-driven] When a fraud authorization record is submitted for recording, the system shall extract the year, month, and day from the original authorization date by character position; compute the authorization time by subtracting the input authorization time numeric from 999999999; and decompose the resulting 9-digit value into hour, minute, second, and millisecond components to form the formatted authorization timestamp in the pattern YY-MM-DD HH.MI.SS.SSS.

REQ-F-197: [Event-driven] When the authorization timestamp has been derived, the system shall populate all fields of the fraud authorization record from the input parameters, including: card number, authorization timestamp, authorization type, card expiry date, message type, message source, authorization ID code, authorization response code, authorization response reason, processing code, transaction amount, approved amount, merchant category code, acquirer country code, point-of-sale entry mode, merchant identifier, merchant name, merchant city, merchant state, merchant ZIP code, transaction identifier, match status, fraud indicator (set from the fraud action code), account identifier, and customer identifier.

REQ-F-198: [Event-driven] When the fraud authorization record is fully assembled, the system shall insert the complete record into the authorization fraud data store (CARDDEMO.AUTHFRDS), setting the fraud report date to the current system date at time of insertion.

REQ-F-199: [Event-driven] When the fraud record insert operation completes successfully, the system shall set the fraud update status to succeeded ('S') and record the fraud action message as 'ADD SUCCESS'.

REQ-F-200: [Event-driven] When the fraud record insert fails with a duplicate-key error (SQL return code -803), the system shall delegate processing to the fraud update routine to attempt an update of the existing fraud record.

REQ-F-201: [Unwanted] If the fraud record insert fails with an unexpected database error (any error other than a duplicate-key conflict), the system shall set the fraud update status to failed ('F'), capture the SQL return code and SQL state code, and compose a system error message in the fraud action message in the form ' SYSTEM ERROR DB2: CODE: \<code\>, STATE: \<state\>'.


---


## 17. Fraud Authorization Record Update on Duplicate-Key Conflict
As a batch operations team, I want an existing fraud record updated when a duplicate-key conflict is detected during insertion so that the fraud indicator and report date reflect the most current fraud classification without creating duplicate entries.

### Requirements

REQ-F-202: [Ubiquitous] The system shall extract the year, month, and day from the original authorization date; compute the time-of-day by subtracting the input authorization time numeric from 999999999 and decompose it into hour, minute, second, and millisecond components; and populate the fraud record with the card number, the assembled authorization timestamp, and the fraud action code prior to performing an update.

REQ-F-203: [Event-driven] When a duplicate-key conflict is confirmed and the fraud record update is invoked, the system shall update the fraud indicator and set the fraud report date to the current date in the authorization fraud store (`CARDDEMO.AUTHFRDS`) for the row matching the card number and authorization timestamp, where the stored timestamp is parsed using the format 'YY-MM-DD HH24.MI.SSNNNNNN', and the value written to the fraud indicator shall be derived from the fraud action code supplied by the caller.

REQ-F-204: [Event-driven] When the fraud record update operation completes successfully, the system shall set the fraud update status to succeeded ('S') and record the fraud action message as 'UPDT SUCCESS'.

REQ-F-205: [Event-driven] When the fraud record update operation fails, the system shall set the fraud update status to failed ('F'), capture the SQL return code and SQL state code, and compose an update error message in the fraud action message in the form ' UPDT ERROR DB2: CODE: \<code\>, STATE: \<state\>'.


---



## Shared Capability Dependencies

This capability depends on the following shared capabilities.
Do not reimplement their behavior — integrate with the shared service.

- **COMEN01C** (`_shared/COMEN01C/`)
