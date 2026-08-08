# Unclassified — Requirements


## Navigation Context

Users enter the application through the COSGN00C signon screen, where they authenticate with a user ID and password before being routed to either the administrator menu (COADM01C) or the general user menu (COMEN01C) based on their user type. Within the menu programs, users select numbered options to navigate forward to specific functional areas via CICS XCTL transfers, with the communication area carrying transaction and program context between screens. At any point in the menu hierarchy, pressing PF3 returns the user to the signon screen, effectively ending the current session context and requiring re-authentication or re-entry. If a destination program is unavailable or uninstalled, the user remains on the current menu with an appropriate error message rather than being transferred.


## Global Preconditions

- A valid CICS environment must be active and capable of processing XCTL program transfers and BMS screen interactions
- The user security file (AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS) must be accessible and available for read operations prior to any authenticated navigation
- A valid communication area (COMMAREA) must be present and correctly populated with originating transaction ID and program name for all post-signon navigation; an absent or empty COMMAREA causes automatic redirection to the signon screen
- The user must have successfully authenticated through COSGN00C before accessing either COADM01C or COMEN01C; unauthenticated access attempts are redirected to the signon screen
- The user's type classification (administrator vs. general user) must be resolvable from the security record to determine correct menu routing and option authorization
- All target programs referenced by menu options must be registered within the CICS program inventory; unregistered programs result in a "not installed" condition rather than a transfer
- Date and time services must be available to populate screen header fields on each display cycle


## 1. Sign-on Credential Collection and Validation
As an end user, I want to submit my credentials on the sign-on screen so that the system can authenticate me and route me to the appropriate menu.

### Requirements

REQ-F-001: [Ubiquitous] The system shall initialize the error flag to the off state at program entry so that no validation errors are assumed before input is received.

REQ-F-002: [Event-driven] When the sign-on screen is displayed, the system shall populate the screen header with the current date formatted as MM/DD/YY, the current time formatted as HH:MM:SS, the application title lines, the transaction identifier, and the program name, and shall include any pending error message in the screen output area before presenting the screen to the user.

REQ-F-003: [Event-driven] When the user presses the Enter key on the sign-on screen, the system shall route to sign-on credential processing logic.

REQ-F-004: [Event-driven] When the user presses any key other than Enter on the sign-on screen, the system shall display the error message 'Invalid key pressed. Please see below...' and redisplay the sign-on screen.

REQ-F-005: [Event-driven] When the user submits the sign-on form and the user ID field is empty (spaces or low-values), the system shall set the error flag to on, display the message 'Please enter User ID ...', and redisplay the sign-on screen.

REQ-F-006: [Event-driven] When the user submits the sign-on form and the password field is empty (spaces or low-values), the system shall set the error flag to on, display the message 'Please enter Password ...', and redisplay the sign-on screen.

REQ-F-007: [Event-driven] When both the user ID and password fields are non-empty, the system shall convert both values to uppercase and store them before proceeding to credential verification against the user security data store (AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS).


---


## 2. User Authentication Against the Security Data Store
As an end user, I want my credentials verified against the security data store so that only valid users gain access to the application.

### Requirements

REQ-F-008: [Event-driven] When both credentials have been collected and validated as non-empty, the system shall look up the user record in the user security data store (AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS) using the user ID (up to 8 alphanumeric characters) as the key.

REQ-F-009: [Event-driven] When the user record is found and the stored password does not match the entered password, the system shall display the message 'Wrong Password. Try again ...' and redisplay the sign-on screen.

REQ-F-010: [Event-driven] When the user record is not found in the user security data store, the system shall display the message 'User not found. Try again ...' and redisplay the sign-on screen.

REQ-F-011: [Event-driven] When a system error occurs while reading the user security data store, the system shall display the message 'Unable to verify the User ...' and redisplay the sign-on screen.


---


## 3. Authenticated User Session Routing
As an authenticated user, I want to be routed to the correct menu based on my user type so that I access only the functions appropriate to my role.

### Requirements

REQ-F-012: [Event-driven] When the stored password matches the entered password, the system shall populate the session context with the authenticated user's transaction identifier, program name, user ID (up to 8 alphanumeric characters), and user type (1 alphanumeric character) retrieved from the user security data store.

REQ-F-013: [Event-driven] When the authenticated user's user type indicates administrator, the system shall transfer control to the administrator menu, passing the populated session context.

REQ-F-014: [Event-driven] When the authenticated user's user type does not indicate administrator, the system shall transfer control to the general menu, passing the populated session context.


---


## 4. Administrator Menu Initialization and Navigation (COADM01C)
As an administrator, I want the administration menu to initialize correctly and route my input so that I can select and launch administrative functions.

### Requirements

REQ-F-015: [Event-driven] When the administrator menu program is invoked with no prior session context, the system shall transfer control immediately to the sign-on screen program.

REQ-F-016: [Event-driven] When the administrator menu program is invoked with a session context present, the system shall load the session context data into the local record structure.

REQ-F-017: [Event-driven] When the administrator menu program is entered and the program context does not indicate a re-entry, the system shall display the administration menu screen.

REQ-F-018: [Event-driven] When the administrator menu program is re-entered and the user presses Enter, the system shall receive the menu screen input and route to option validation processing.

REQ-F-019: [Event-driven] When the administrator menu program is re-entered and the user presses any key other than Enter or PF3, the system shall display the invalid-key message and redisplay the administration menu screen.

REQ-F-020: [Event-driven] When the user submits a menu option on the administration menu, the system shall trim trailing spaces from the option input, replace internal spaces with zeros, convert the result to a numeric option number, and validate that the value is numeric, greater than zero, and does not exceed the maximum available administrator option count.

REQ-F-021: [Event-driven] When the submitted menu option fails validation (non-numeric, zero, or exceeds the maximum administrator option count), the system shall set the error flag to active, display the message 'Please enter a valid option number...', and redisplay the administration menu screen.

REQ-F-022: [Event-driven] When a program-not-found condition occurs during menu option navigation, the system shall display the message 'This option is not installed ...' and redisplay the administration menu screen.

REQ-F-023: [Event-driven] When the user presses PF3 on the administrator menu, the system shall set the destination program to the sign-on screen program and transfer control to it.

REQ-F-024: [Event-driven] When the destination program name is empty or unset before a control transfer, the system shall default the destination program name to the sign-on screen program.

REQ-F-025: [Ubiquitous] The system shall transfer control to the destination program specified in the session context navigation target.


---



## Shared Capability Dependencies

This capability depends on the following shared capabilities.
Do not reimplement their behavior — integrate with the shared service.

- **COMEN01C** (`_shared/COMEN01C/`)
