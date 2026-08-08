# Interactive Navigation and Screen Control — Requirements


## Navigation Context

Users navigate through the Interactive Navigation and Screen Control function via CICS terminal screens, using mapped function keys (primarily PF3 for exit) to move between programs. Navigation state is maintained and passed between programs through a shared communication area (COCOM01Y), which carries the originating transaction, calling program identity, and destination context so that exit handling can correctly return users to either the prior screen or the main menu. When a user completes an action or presses an exit key, the current program evaluates the communication area to determine the appropriate transfer target and issues an XCTL to route control to the next program in the flow. The administrator menu (COADM01C) acts as a hub for administrative navigation, while account-focused programs (COACTUPC, COACTVWC) handle their own local navigation loops before delegating back to the menu hierarchy.


## Global Preconditions

- A valid and populated CICS communication area (COMMAREA) must be present for all programs except the initial signon entry point; an empty or absent COMMAREA triggers an immediate redirect to the signon screen
- The user must be authenticated and have an active terminal session established within the CardDemo CICS environment before any screen interaction is processed
- The shared communication area copybook (COCOM01Y) must be correctly initialized with the originating transaction ID and calling program name prior to any inter-program XCTL transfer
- All terminal function key mappings (e.g., PF3 mapped to exit) must be resolved against the DFHAID and DFHBMSCA copybooks before navigation routing logic is evaluated
- The CICS transaction associated with the current screen must be active and the terminal must be in a valid input state before function key validation and routing decisions are made
- Access to the required VSAM data stores (ACCTDATA, CUSTDATA, CARDXREF) must be available for any program that retrieves or updates account or customer data as part of its screen lifecycle
- The external date validation service (CSUTLDTC) must be available and callable for any operation that involves date field validation prior to data acceptance or file update


## 1. Account Update — Function Key Mapping
As an account update operator, I want function key presses to be correctly mapped to internal action flags so that the system routes my input to the appropriate processing logic.

### Requirements

REQ-F-001: [Event-driven] When the user presses the Enter key, the system shall set the Enter flag indicator to true.

REQ-F-002: [Event-driven] When the user presses the PF1 key, the system shall set the PF1 key flag to true.

REQ-F-003: [Event-driven] When the user presses the PF4 key, the system shall set the PF4 key flag to true.

REQ-F-004: [Event-driven] When the user presses the PF5 key, the system shall set the PF5 key flag to true.

REQ-F-005: [Event-driven] When the user presses the PF6 key, the system shall set the PF6 key flag to true.

REQ-F-006: [Event-driven] When the user presses the PF7 key, the system shall set the PF7 key flag to true.

REQ-F-007: [Event-driven] When the user presses the PF12 key, the system shall set the PF12 key flag to true.

REQ-F-008: [Event-driven] When the user presses the PF13 key, the system shall set the PF1 key flag to true, treating PF13 as an alias for PF1.

REQ-F-009: [Event-driven] When the user presses the PF15 key, the system shall set the PF3 key flag to true, treating PF15 as an alias for PF3.

REQ-F-010: [Event-driven] When the user presses the PF16 key, the system shall set the PF4 key flag to true, treating PF16 as an alias for PF4.

REQ-F-011: [Event-driven] When the user presses the PF17 key, the system shall set the PF5 key flag to true, treating PF17 as an alias for PF5.

REQ-F-012: [Event-driven] When the user presses the PF18 key, the system shall set the PF6 key flag to true, treating PF18 as an alias for PF6.

REQ-F-013: [Event-driven] When the user presses the PF20 key, the system shall set the PF8 key flag to true, treating PF20 as an alias for PF8.

REQ-F-014: [Event-driven] When the user presses the PF22 key, the system shall set the PF10 key flag to true, treating PF22 as an alias for PF10.

REQ-F-015: [Event-driven] When the user presses the PF23 key, the system shall set the PF11 key flag to true, treating PF23 as an alias for PF11.

REQ-F-016: [Event-driven] When the user presses the PF24 key, the system shall set the PF12 key flag to true, treating PF24 as an alias for PF12.

REQ-F-017: [Ubiquitous] The system shall define and execute a function key mapping dispatch table that evaluates the terminal attention identifier against all supported key codes.

REQ-F-018: [Event-driven] When the user presses a function key, the system shall map the attention identifier to a named function key flag, validate the key against the current program state, and force the input to Enter if the key is invalid for the current state. Valid keys are: Enter, PF3 (exit), PF5 (confirm changes when changes are pending), and PF12 (cancel when details are shown).


---


## 2. Account Update — Program State Routing and Screen Control
As an account update operator, I want the system to route me to the correct next action based on my current state and key press so that the workflow proceeds correctly.

### Requirements

REQ-F-019: [State-driven] While the program is in one of the following states — account details not fetched, details shown, changes made, changes confirmed, or changes completed/failed — the system shall evaluate the current program state and user function key input to route to the appropriate next action: exit on PF3, display the search screen on initial entry or after completion, or process user input for validation and update.

REQ-F-020: [Event-driven] When the program is on first entry or the account identifier is zero, the system shall display the initial screen with all data fields set to low-value sentinels.

REQ-F-021: [Event-driven] When account details have not yet been fetched, the system shall clear any prior error message, retrieve the account record from the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS), and if the associated customer record is found in the customer data store (AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS), transition to the detail-display state.

REQ-F-022: [Event-driven] When account details are displayed and input validation passed and changes were detected, the system shall transition to the confirmation-pending state to request user approval of the changes.

REQ-F-023: [Event-driven] When the state is confirmation-pending but the user did not press PF5, the system shall take no action and continue to the next state evaluation.

REQ-F-024: [Event-driven] When account filter validation indicates an invalid state, the system shall take no action and continue to the next state evaluation.

REQ-F-025: [Event-driven] When account update is complete, the system shall transition to the detail-display state to show the updated account information.

REQ-F-026: [Unwanted] If the program state does not match any defined business scenario, the system shall record abend code 0001 with the program name and message 'UNEXPECTED DATA SCENARIO', then delegate to the abend routine to terminate.


---


## 3. Account Update — Screen Population and Display
As an account update operator, I want the screen to be populated with the correct account and customer data for the current state so that I can review and edit the information accurately.

### Requirements

REQ-F-027: [State-driven] While the program is in one of the following states — first entry, account details shown, or changes made — the system shall populate the screen with account and customer data: display blank fields on first entry, display previously fetched data when details are shown, or display updated values when changes have been made.

REQ-F-028: [Event-driven] When account details are being displayed on the screen, the system shall populate all account and customer fields with the previously fetched values from the account and customer master files.

REQ-F-029: [Ubiquitous] The system shall display the account active status and credit limit, formatting the credit limit as currency when validation succeeds or displaying the raw input value when validation fails.

REQ-F-030: [Ubiquitous] The system shall display the current balance, formatting it as currency when validation succeeds or displaying the raw input value when validation fails.

REQ-F-031: [Ubiquitous] The system shall display the cash credit limit, formatting it as currency when validation succeeds or displaying the raw input value when validation fails.

REQ-F-032: [Ubiquitous] The system shall display the current cycle credit, formatting it as currency when validation succeeds or displaying the raw input value when validation fails.

REQ-F-033: [Ubiquitous] The system shall display the current cycle debit, formatting it as currency when validation succeeds or displaying the raw input value when validation fails.

REQ-F-034: [Ubiquitous] The system shall display the account open date, expiration date, reissue date, and group identifier on the screen, with each date component (year, month, day) displayed individually.

REQ-F-035: [Ubiquitous] The system shall display the customer identifier, social security number (in three parts), FICO credit score, and date of birth (year, month, day components) on the screen.

REQ-F-036: [Ubiquitous] The system shall display the customer name (first, middle, and last), address lines, state code, ZIP code, and country code on the screen.

REQ-F-037: [Ubiquitous] The system shall display the customer EFT account identifier and primary card holder indicator on the screen.

REQ-F-038: [Ubiquitous] The system shall set all input fields to protected status with the modified data tag enabled by default.

REQ-F-039: [State-driven] While the program is preparing to display the screen, the system shall set display attributes for the information message field: if no information message is set, display the field with dark attribute; if an information message is set, display the field with bright and autoskip attributes. Additionally, if changes have been made and not yet confirmed, display the F12 (cancel) key indicator with bright attribute; if changes are pending confirmation, display both F5 (confirm) and F12 (cancel) key indicators with bright attribute.

REQ-F-040: [State-driven] While the program is in one of the following states — first entry, account details shown, changes made, changes confirmed, or changes completed/failed — the system shall set up information and error messages based on the current program state, displaying prompts for search, update, confirmation, or success/failure messages, as well as any validation error messages recorded during input validation.


---


## 4. Account Update — Input Extraction
As an account update operator, I want all submitted field values to be correctly extracted from the screen input so that the update record reflects my intended changes.

### Requirements

REQ-F-041: [Event-driven] When the user submits the account update screen, the system shall extract the account identifier: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered account identifier.

REQ-F-042: [Event-driven] When the user submits the account update screen, the system shall extract the account active status: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered status.

REQ-F-043: [Event-driven] When the user submits the account update screen, the system shall extract the credit limit: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, validate the entered value as numeric and convert it to a signed decimal amount with two decimal places.

REQ-F-044: [Event-driven] When the user submits the account update screen, the system shall extract the cash credit limit: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, validate the entered value as numeric and convert it to a signed decimal amount with two decimal places.

REQ-F-045: [Event-driven] When the user submits the account update screen, the system shall extract the current cycle credit: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, validate the entered value as numeric and convert it to a signed decimal amount with two decimal places.

REQ-F-046: [Event-driven] When the user submits the account update screen, the system shall extract the current cycle debit: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, validate the entered value as numeric and convert it to a signed decimal amount with two decimal places.

REQ-F-047: [Event-driven] When the user submits the account update screen, the system shall extract the account open date components (year, month, day): for each component, if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered value.

REQ-F-048: [Event-driven] When the user submits the account update screen, the system shall extract the account expiration date components (year, month, day): for each component, if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered value.

REQ-F-049: [Event-driven] When the user submits the account update screen, the system shall extract the account reissue date components (year, month, day): for each component, if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered value.

REQ-F-050: [Event-driven] When the user submits the account update screen, the system shall extract the account group identifier: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered identifier.

REQ-F-051: [Event-driven] When the user submits the account update screen, the system shall extract the customer identifier: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered identifier.

REQ-F-052: [Event-driven] When the user submits the account update screen, the system shall extract the customer first name: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered name.

REQ-F-053: [Event-driven] When the user submits the account update screen, the system shall extract the customer middle name: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered name.

REQ-F-054: [Event-driven] When the user submits the account update screen, the system shall extract the customer last name: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered name.

REQ-F-055: [Event-driven] When the user submits the account update screen, the system shall extract customer address line 1: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered address.

REQ-F-056: [Event-driven] When the user submits the account update screen, the system shall extract customer address line 2: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered address.

REQ-F-057: [Event-driven] When the user submits the account update screen, the system shall extract the customer city: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered city into the address line 3 field of the update record.

REQ-F-058: [Event-driven] When the user submits the account update screen, the system shall extract the customer state code: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered state code.

REQ-F-059: [Event-driven] When the user submits the account update screen, the system shall extract the customer ZIP code: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered ZIP code.

REQ-F-060: [Event-driven] When the user submits the account update screen, the system shall extract the customer country code: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered country code.

REQ-F-061: [Event-driven] When the user submits the account update screen, the system shall extract the primary phone number components (area code, exchange code, line number): for each component, if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered value.

REQ-F-062: [Event-driven] When the user submits the account update screen, the system shall extract the secondary phone number components (area code, exchange code, line number): for each component, if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered value.

REQ-F-063: [Event-driven] When the user submits the account update screen, the system shall extract the social security number components (part 1, part 2, part 3): for each component, if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered value.

REQ-F-064: [Event-driven] When the user submits the account update screen, the system shall extract the customer government-issued identifier: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered identifier.

REQ-F-065: [Event-driven] When the user submits the account update screen, the system shall extract the customer date of birth components (year, month, day): for each component, if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered value.

REQ-F-066: [Event-driven] When the user submits the account update screen, the system shall extract the customer EFT account identifier: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered EFT account identifier.

REQ-F-067: [Event-driven] When the user submits the account update screen, the system shall extract the primary card holder indicator: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered indicator.

REQ-F-068: [Event-driven] When the user submits the account update screen, the system shall extract the customer FICO credit score: if the field is marked with an asterisk or is blank, store a low-value sentinel; otherwise, store the entered score.


---


## 5. Account Update — Input Validation
As an account update operator, I want all submitted field values to be validated before any update is applied so that only correct data is written to the account and customer data stores.

### Requirements

REQ-F-069: [Event-driven] When the account identifier search key is being validated, the system shall validate that the account identifier is supplied, numeric, and non-zero; set error flags and record error messages for blank, non-numeric, or zero values.

REQ-F-070: [Event-driven] When the account status field is being validated, the system shall validate that the account status field is supplied and is either Y or N; set error flags and record error messages for blank or invalid values.

REQ-F-071: [Event-driven] When a yes/no field is being validated, the system shall validate that the field is supplied and is either Y or N; set error flags and record error messages for blank or invalid values.

REQ-F-072: [Event-driven] When a mandatory alphanumeric field is being validated, the system shall validate that the field is supplied and has a non-zero trimmed length; set error flags and record error messages for blank or zero-length values.

REQ-F-073: [Event-driven] When a required alphabetic field is being validated, the system shall validate that the field is supplied, has non-zero trimmed length, and contains only alphabetic characters; set error flags and record error messages for blank, zero-length, or non-alphabetic values.

REQ-F-074: [Event-driven] When an optional alphabetic field is being validated, the system shall validate that the field, if supplied, contains only alphabetic characters; set error flags and record error messages for non-alphabetic values.

REQ-F-075: [Event-driven] When the credit limit field is being validated, the system shall validate that the credit limit field, if supplied, is a valid signed numeric value with up to 2 decimal places; set error flags and record error messages for invalid values.

REQ-F-076: [Event-driven] When the cash credit limit field is being validated, the system shall validate that the cash credit limit field, if supplied, is a valid signed numeric value with up to 2 decimal places; set error flags and record error messages for invalid values.

REQ-F-077: [Event-driven] When the current cycle credit field is being validated, the system shall validate that the current cycle credit field, if supplied, is a valid signed numeric value with up to 2 decimal places; set error flags and record error messages for invalid values.

REQ-F-078: [Event-driven] When the current cycle debit field is being validated, the system shall validate that the current cycle debit field, if supplied, is a valid signed numeric value with up to 2 decimal places; set error flags and record error messages for invalid values.

REQ-F-079: [Event-driven] When the field is supplied but contains non-numeric characters, the system shall set the input error flag, mark the validation flag as not-ok, and record an error message stating the field must be all numeric.

REQ-F-080: [Event-driven] When the field contains only numeric characters but the numeric value equals zero, the system shall set the input error flag, mark the validation flag as not-ok, and record an error message stating the field must not be zero.

REQ-F-081: [Event-driven] When a US state code is being validated, the system shall validate that the state code is a valid 2-character US state code (AL, AK, AZ, AR, CA, CO, CT, DE, FL, GA, HI, ID, IL, IN, IA, KS, KY, LA, ME, MD, MA, MI, MN, MS, MO, MT, NE, NV, NH, NJ, NM, NY, NC, ND, OH, OK, OR, PA, RI, SC, SD, TN, TX, UT, VT, VA, WA, WV, WI, WY, DC, AS, GU, MP, PR, VI); set error flags and record error messages for invalid state codes.

REQ-F-082: [Event-driven] When a US state code and ZIP code combination is being validated, the system shall validate that the state code and first 2 digits of the ZIP code form a valid combination based on USPS data; set error flags and record error messages for invalid combinations.

REQ-F-083: [Event-driven] When the customer phone number fields are being validated, the system shall validate that each phone number part (area code, exchange code, line number) is supplied, numeric, and non-zero; validate that the area code is a valid North American general-purpose area code; set error flags and record error messages for blank, non-numeric, zero, or invalid area code values.

REQ-F-084: [Event-driven] When the area code field is blank or contains low-values, the system shall set the input error flag and record an error message stating 'Area code must be supplied'.

REQ-F-085: [Event-driven] When the area code numeric value equals zero, the system shall set the input error flag and record an error message stating 'Area code cannot be zero'.

REQ-F-086: [Event-driven] When the area code does not match any valid North American general-purpose area code, the system shall set the input error flag and record an error message stating 'Not valid North America general purpose area code'.

REQ-F-087: [Event-driven] When a US phone number prefix code is submitted for validation, the system shall verify that the prefix code is supplied and non-blank; if blank, set input error and record 'Prefix code must be supplied'; verify that the prefix code contains only numeric digits; if non-numeric, set input error and record 'Prefix code must be A 3 digit number'; verify that the numeric prefix code is not zero; if zero, set input error and record 'Prefix code cannot be zero'.

REQ-F-088: [Event-driven] When a US phone number line number is submitted for validation, the system shall verify that the line number is supplied and non-blank; if blank, set input error and record 'Line number code must be supplied'; verify that the line number contains only numeric digits; if non-numeric, set input error and record 'Line number code must be A 4 digit number'; verify that the numeric line number is not zero; if zero, set input error and record 'Line number code cannot be zero'.

REQ-F-089: [Event-driven] When the customer EFT account identifier and primary card holder indicator fields are being validated, the system shall validate that the EFT account identifier, if supplied, is numeric; validate that the primary card holder indicator is supplied and is either Y or N; set error flags and record error messages for invalid values.


---


## 6. Account Update — Date Validation
As an account update operator, I want all date fields to be validated for correct format and calendar accuracy so that only valid dates are stored.

### Requirements

REQ-F-090: [Event-driven] When the open date field is being validated, the system shall validate the open date year, month, and day components for presence, numeric format, and valid ranges; delegate to the date validation service to verify the date is valid; set error flags and record error messages for invalid dates.

REQ-F-091: [Event-driven] When the reissue date field is being validated, the system shall validate the reissue date year, month, and day components for presence, numeric format, and valid ranges; delegate to the date validation service to verify the date is valid; set error flags and record error messages for invalid dates.

REQ-F-092: [Event-driven] When the date of birth field is being validated, the system shall validate the date of birth year, month, and day components for presence, numeric format, and valid ranges; delegate to the date validation service to verify the date is valid; verify that the date of birth is not in the future; set error flags and record error messages for invalid dates.

REQ-F-093: [Event-driven] When the date of birth is being validated, the system shall validate that the date of birth is not in the future by comparing the current date with the date of birth; set error flags and record error messages if the date is in the future.

REQ-F-094: [Event-driven] When the year component is blank or contains only spaces, the system shall set the input error flag, mark the year as blank, and record a diagnostic message requiring the year to be supplied.

REQ-F-095: [Event-driven] When the year component is not numeric, the system shall set the input error flag, mark the year as invalid, and record a diagnostic message requiring the year to be a 4-digit number.

REQ-F-096: [Event-driven] When the century component is not 19 or 20, the system shall set the input error flag, mark the year as invalid, and record a diagnostic message stating that the century is not valid. When the century is valid, mark the year as valid.

REQ-F-097: [Event-driven] When the month component is blank or contains only spaces, the system shall set the input error flag, mark the month as blank, and record a diagnostic message requiring the month to be supplied.

REQ-F-098: [Event-driven] When the month component is not in the range 1–12 or cannot be converted to a number, the system shall set the input error flag, mark the month as invalid, and record a diagnostic message stating that the month must be a number between 1 and 12. When the month is valid, convert it to numeric form and mark the month as valid.

REQ-F-099: [Ubiquitous] The system shall exit the month validation section and proceed to day validation.

REQ-F-100: [Event-driven] When the day component is blank or contains only spaces, the system shall set the input error flag, mark the day as blank, and record a diagnostic message requiring the day to be supplied.

REQ-F-101: [Event-driven] When the day component is not numeric or is not in the range 1–31, the system shall convert the day to numeric form if it is numeric; if the day is not numeric or is not in the range 1–31, set the input error flag, mark the day as invalid, and record a diagnostic message. When the day is valid, mark the day as valid.

REQ-F-102: [Ubiquitous] The system shall exit the day-month-year combination validation section and proceed to day component validation.

REQ-F-103: [Event-driven] When the day is 31 but the month does not have 31 days, the system shall set the input error flag, mark the day and month as invalid, and record a diagnostic message stating that the month cannot have 31 days.

REQ-F-104: [Event-driven] When the day is 30 in February, the system shall set the input error flag, mark the day and month as invalid, and record a diagnostic message stating that February cannot have 30 days.

REQ-F-105: [Event-driven] When the day is 29 in February but the year is not a leap year, the system shall determine whether the year is a leap year by dividing the full year by 400 (if the year ends in 00) or by 4 (otherwise); if the remainder is zero, the year is a leap year and validation passes; if the remainder is not zero, set the input error flag, mark the day, month, and year as invalid, and record a diagnostic message stating that the year is not a leap year.

REQ-F-106: [Ubiquitous] The system shall exit the day-month-year validation section and proceed to final date validation.

REQ-F-107: [Event-driven] When the date validation service returns a non-zero severity code, the system shall set the input error flag, mark the day, month, and year as invalid, and record a diagnostic message with the severity and message codes returned by the service.

REQ-F-108: [Ubiquitous] The system shall mark the date as valid upon successful completion of all validation checks.

REQ-F-109: [Ubiquitous] The system shall mark the day as valid and proceed to validate the day component at the start of day component validation.


---


## 7. Account Update — Data Retrieval
As an account update operator, I want the system to retrieve the correct account and customer records so that I can review and update accurate data.

### Requirements

REQ-F-110: [Event-driven] When the user has entered a valid account identifier for search, the system shall retrieve the card cross-reference record to obtain the customer identifier, retrieve the account master record from the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS), retrieve the customer master record from the customer data store (AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS), and store the retrieved data in the old details area.

REQ-F-111: [Event-driven] When the card cross-reference record is being retrieved by account identifier, the system shall retrieve the record from the card cross-reference data store (AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH) by account identifier; extract the customer identifier if found; set error flags and record error messages for not-found or other errors.

REQ-F-112: [Event-driven] When the account master record is being retrieved by account identifier, the system shall retrieve the record from the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) by account identifier; set the account-found flag if found; set error flags and record error messages for not-found or other errors.

REQ-F-113: [Event-driven] When the customer master record is being retrieved by customer identifier, the system shall retrieve the record from the customer data store (AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS) by customer identifier; set the customer-found flag if found; record error messages for not-found or other errors.

REQ-F-114: [Ubiquitous] The system shall store the retrieved account and customer data in the old details area by extracting and moving all account fields (active status, current balance, credit limit, cash credit limit, current cycle credit, current cycle debit, open date components, expiration date components, reissue date components, group identifier) and all customer fields (customer identifier, social security number, date of birth components, FICO credit score, name fields, address fields, phone numbers, government-issued identifier, EFT account identifier, primary card holder indicator).


---


## 8. Account Update — Change Detection and Confirmation
As an account update operator, I want the system to detect whether I have made any changes and to require my confirmation before applying updates so that accidental modifications are prevented.

### Requirements

REQ-F-115: [Event-driven] When the user has submitted the account update screen with input, the system shall compare the user's updated input with the previously fetched account and customer data; set the no-changes flag if all values match, or set the change-detected flag if any value differs.

REQ-F-116: [Event-driven] When the account record is retrieved from the data store for comparison against the session's baseline copy, the system shall compare all account fields (active status, current balance, credit limits, cycle amounts, open date, expiration date, reissue date, group identifier) between the current store version and the session baseline; if all fields match, allow processing to continue; if any field differs, set the concurrent-change indicator to true.

REQ-F-117: [Event-driven] When the customer record is retrieved from the data store for comparison against the session's baseline copy, the system shall compare all customer fields (name, address, phone, social security number, government-issued identifier, date of birth, EFT account identifier, primary card holder indicator, FICO credit score) between the current store version and the session baseline using case-insensitive matching for text fields; if all fields match, allow processing to continue; if any field differs, set the concurrent-change indicator to true.


---


## 9. Account Update — Record Update Processing
As an account update operator, I want confirmed changes to be written to both the account and customer data stores so that the records reflect the approved updates.

### Requirements

REQ-F-118: [Event-driven] When the user has confirmed changes and the program is preparing to update the account master record, the system shall lock and read the account master record from the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) for update; set error flags if the lock fails.

REQ-F-119: [Event-driven] When the user has confirmed changes and the program is preparing to update the customer master record, the system shall lock and read the customer master record from the customer data store (AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS) for update; set error flags if the lock fails.

REQ-F-120: [Event-driven] When the account and customer master records have been locked and no concurrent changes were detected, the system shall assemble the updated account master record from the user's input and rewrite the account master record in the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS); set error flags if the rewrite fails.

REQ-F-121: [Event-driven] When the account update record is ready to be written to the data store, the system shall assemble the account update record by copying the modified account identifier, active status, current balance, credit limit, cash credit limit, current cycle credit, current cycle debit, and group identifier; reconstruct the open date, expiration date, and reissue date in YYYY-MM-DD format; write the assembled record to the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS); if the write fails, set the update-failed flag to true.

REQ-F-122: [Event-driven] When the account master record has been successfully updated, the system shall assemble the updated customer master record from the user's input and rewrite the customer master record in the customer data store (AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS); set error flags and perform a rollback if the rewrite fails.

REQ-F-123: [Event-driven] When the customer update record is ready to be written to the data store, the system shall assemble the customer update record by copying the modified customer identifier, name fields, address fields, phone numbers, social security number, government-issued identifier, EFT account identifier, primary card holder indicator, and FICO credit score; reconstruct phone numbers in (AAA)EEE-LLLL format and date of birth in YYYY-MM-DD format; write the assembled record to the customer data store (AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS); if the write fails, set the update-failed flag to true and issue a rollback to undo the account update.


### Non-Functional Requirements

REQ-N-001: [Event-driven] When the customer record write fails after the account record has been successfully written, the system shall issue a rollback to undo the account update, ensuring that the account data store and customer data store are not left in an inconsistent state.


---


## 10. Account Update — Cursor Positioning on Validation Failure
As an account update operator, I want the cursor to be positioned on the first invalid field when validation fails so that I can quickly locate and correct errors.

### Requirements

REQ-F-124: [Event-driven] When account status validation fails or the field is blank, the system shall position the cursor on the account status field.

REQ-F-125: [Event-driven] When current balance validation fails or the field is blank, the system shall position the cursor on the current balance field.

REQ-F-126: [Event-driven] When credit limit validation fails or the field is blank, the system shall position the cursor on the credit limit field.

REQ-F-127: [Event-driven] When current cycle credit validation fails or the field is blank, the system shall position the cursor on the current cycle credit field.

REQ-F-128: [Event-driven] When current cycle debit validation fails or the field is blank, the system shall position the cursor on the current cycle debit field.

REQ-F-129: [Event-driven] When open month validation fails or the field is blank, the system shall position the cursor on the open month field.

REQ-F-130: [Event-driven] When open day validation fails or the field is blank, the system shall position the cursor on the open day field.

REQ-F-131: [Event-driven] When expiration year validation fails or the field is blank, the system shall position the cursor on the expiration year field.

REQ-F-132: [Event-driven] When expiration month validation fails or the field is blank, the system shall position the cursor on the expiration month field.

REQ-F-133: [Event-driven] When reissue year validation fails or the field is blank, the system shall position the cursor on the reissue year field.

REQ-F-134: [Event-driven] When reissue month validation fails or the field is blank, the system shall position the cursor on the reissue month field.

REQ-F-135: [Event-driven] When reissue day validation fails or the field is blank, the system shall position the cursor on the reissue day field.

REQ-F-136: [Event-driven] When last name validation fails or the field is blank, the system shall position the cursor on the last name field.

REQ-F-137: [Event-driven] When middle name validation fails, the system shall position the cursor on the middle name field.

REQ-F-138: [Event-driven] When address line 1 validation fails or the field is blank, the system shall position the cursor on the address line 1 field.

REQ-F-139: [Event-driven] When city validation fails or the field is blank, the system shall position the cursor on the city field.

REQ-F-140: [Event-driven] When ZIP code validation fails or the field is blank, the system shall position the cursor on the ZIP code field.

REQ-F-141: [Event-driven] When country code validation fails or the field is blank, the system shall position the cursor on the country field.

REQ-F-142: [Event-driven] When phone 1 prefix validation fails or the field is blank, the system shall position the cursor on the phone 1 prefix field.

REQ-F-143: [Event-driven] When phone 1 line number validation fails or the field is blank, the system shall position the cursor on the phone 1 line number field.

REQ-F-144: [Event-driven] When phone 2 area code validation fails or the field is blank, the system shall position the cursor on the phone 2 area code field.

REQ-F-145: [Event-driven] When phone 2 prefix validation fails or the field is blank, the system shall position the cursor on the phone 2 prefix field.

REQ-F-146: [Event-driven] When SSN part 1 validation fails or the field is blank, the system shall position the cursor on the SSN part 1 field.

REQ-F-147: [Event-driven] When SSN part 3 validation fails or the field is blank, the system shall position the cursor on the SSN part 3 field.

REQ-F-148: [Event-driven] When FICO score validation fails or the field is blank, the system shall position the cursor on the FICO score field.

REQ-F-149: [Event-driven] When date of birth month validation fails or the field is blank, the system shall position the cursor on the date of birth month field.

REQ-F-150: [Event-driven] When date of birth day validation fails or the field is blank, the system shall position the cursor on the date of birth day field.

REQ-F-151: [Event-driven] When account details are found or no changes are detected, the system shall position the cursor on the account status field.


### Open Questions

OQ-001: Rule 041b299e states that only century values 19 or 20 are valid. Should the modernized system support century values beyond 20 (e.g., for dates in the 2100s)? — Owner: business/product owner

OQ-002: Rule 7b0abee0 references state-ZIP code combination validation based on USPS data. Is the full USPS state-ZIP prefix mapping table available and current for the modernized system? — Owner: data/compliance team

OQ-003: Rule 84bec40b references a list of valid North American general-purpose area codes. Is the authoritative list of valid area codes available and maintained for the modernized system? — Owner: data/compliance team


---


## 11. Account and Customer Record Update Processing — Field Validation and Input Extraction
As an account operations user, I want the system to validate all account and customer fields submitted on the account update screen so that only well-formed, consistent data is accepted for update.


**Function Key Mapping**

### Requirements

REQ-F-152: [Event-driven] When the attention identifier equals the PF3 key code, the system shall set the PF3 key flag to TRUE.

REQ-F-153: [Event-driven] When the attention identifier equals the PF8 key code, the system shall set the PF8 key flag to TRUE.

REQ-F-154: [Event-driven] When the attention identifier equals the PF9 key code, the system shall set the PF9 key flag to TRUE.

REQ-F-155: [Event-driven] When the attention identifier equals the PF10 key code, the system shall set the PF10 key flag to TRUE.

REQ-F-156: [Event-driven] When the attention identifier equals the PF11 key code, the system shall set the PF11 key flag to TRUE.

REQ-F-157: [Event-driven] When the attention identifier equals the PA1 key code, the system shall set the PA1 key flag to TRUE.

REQ-F-158: [Event-driven] When the attention identifier equals the PA2 key code, the system shall set the PA2 key flag to TRUE.

REQ-F-159: [Event-driven] When the attention identifier equals the CLEAR key code, the system shall set the CLEAR key flag to TRUE.

REQ-F-160: [Event-driven] When the attention identifier equals the PF2 key code, the system shall set the PF2 key flag to TRUE.

REQ-F-161: [Event-driven] When the attention identifier equals the PF14 key code, the system shall set the PF2 key flag to TRUE, treating PF14 as an alias for PF2.

REQ-F-162: [Event-driven] When the attention identifier equals the PF19 key code, the system shall set the PF7 key flag to TRUE, treating PF19 as an alias for PF7.

REQ-F-163: [Event-driven] When the attention identifier equals the PF21 key code, the system shall set the PF9 key flag to TRUE, treating PF21 as an alias for PF9.

REQ-F-164: [Event-driven] When a yes/no field is being validated, the system shall validate that the field is supplied and is either Y or N; if the field is blank or not Y or N, the system shall set error flags and record an error message.

REQ-F-165: [Event-driven] When the current balance field is being validated, the system shall validate that the field, if supplied, is a valid signed numeric value with up to 2 decimal places; if the value is not valid, the system shall set error flags and record an error message.

REQ-F-166: [Event-driven] When a signed numeric field with 2 decimal places is being validated, the system shall validate that the field, if supplied, is a valid signed numeric value with up to 2 decimal places; if the value is not valid, the system shall set error flags and record an error message.

REQ-F-167: [Event-driven] When the FICO credit score field is being validated, the system shall validate that the field is supplied, numeric, and between 300 and 850 inclusive; if the field is blank, non-numeric, or outside the valid range, the system shall set error flags and record an error message.

REQ-F-168: [Event-driven] When the customer name fields are being validated, the system shall validate that first name and last name are supplied and contain only alphabetic characters; if middle name is supplied, the system shall validate that it contains only alphabetic characters; if any required name field is blank or contains non-alphabetic characters, the system shall set error flags and record an error message.

REQ-F-169: [Event-driven] When the customer address fields are being validated, the system shall validate that address line 1, state code, ZIP code, city, and country code are supplied and valid; the state code must be a valid 2-character US state code (AL, AK, AZ, AR, CA, CO, CT, DE, FL, GA, HI, ID, IL, IN, IA, KS, KY, LA, ME, MD, MA, MI, MN, MS, MO, MT, NE, NV, NH, NJ, NM, NY, NC, ND, OH, OK, OR, PA, RI, SC, SD, TN, TX, UT, VT, VA, WA, WV, WI, WY, DC, AS, GU, MP, PR, VI); the ZIP code must be numeric; city and country code must contain only alphabetic characters; the state and ZIP code combination must be valid; if any required field is blank or invalid, the system shall set error flags and record an error message.

REQ-F-170: [Event-driven] When the Social Security Number field is being validated, the system shall validate that each SSN part is supplied, numeric, and non-zero; SSN part 1 must not be 000, 666, or between 900 and 999; if any part is invalid, the system shall set error flags and record an error message.

REQ-F-171: [Event-driven] When the expiration date field is being validated, the system shall validate the expiration date year, month, and day components for presence, numeric format, and valid ranges; delegate to the date validation service to verify the date is valid; if any component is invalid, the system shall set error flags and record an error message.

REQ-F-172: [Event-driven] When a required numeric field is submitted for validation, the system shall check whether the field contains data; if the field is blank, low-values, or contains only spaces, the system shall set the input error flag, mark the validation flag as blank, and record an error message stating the field must be supplied.

REQ-F-173: [Event-driven] When the field is supplied but contains non-numeric characters, the system shall set the input error flag, mark the validation flag as not-OK, and record an error message stating the field must be all numeric.

REQ-F-174: [Event-driven] When the numeric field value equals zero, the system shall set the input error flag, mark the field as not-OK, and record a message stating the field must not be zero; when the field value is non-zero, the system shall mark the field as valid.

REQ-F-175: [Event-driven] When the area code field is blank or contains low-values, the system shall set the input error flag and record an error message stating 'Area code must be supplied'.

REQ-F-176: [Event-driven] When the area code field contains non-numeric characters, the system shall set the input error flag and record an error message stating 'Area code must be a 3 digit number'.

REQ-F-177: [Event-driven] When the area code numeric value equals zero, the system shall set the input error flag and record an error message stating 'Area code cannot be zero'.

REQ-F-178: [Event-driven] When the area code does not match any valid North American general-purpose area code, the system shall set the input error flag and record an error message stating 'Not valid North America general purpose area code'.

REQ-F-179: [Event-driven] When the account update screen input is received, the system shall extract the account reissue date components (year, month, day); for each component, if the field is marked with an asterisk or is blank, the system shall store a low-value sentinel; otherwise, the system shall store the entered value.

REQ-F-180: [Event-driven] When primary card holder indicator validation fails or the field is blank, the system shall position the cursor on the primary card holder field; for any other validation state, the system shall position the cursor on the account ID field.

REQ-F-181: [Event-driven] When expiration day validation fails or the field is blank, the system shall position the cursor on the expiration day field.

REQ-F-182: [Event-driven] When SSN part 2 validation fails or the field is blank, the system shall position the cursor on the SSN part 2 field.

REQ-F-183: [Event-driven] When cash credit limit validation fails or the field is blank, the system shall position the cursor on the cash credit limit field.

REQ-F-184: [Event-driven] When date of birth year validation fails or the field is blank, the system shall position the cursor on the date of birth year field.

REQ-F-185: [Event-driven] When first name validation fails or the field is blank, the system shall position the cursor on the first name field.

REQ-F-186: [Event-driven] When phone 1 area code validation fails or the field is blank, the system shall position the cursor on the phone 1 area code field.

REQ-F-187: [Event-driven] When open year validation fails or the field is blank, the system shall position the cursor on the open year field.

REQ-F-188: [Event-driven] When state code validation fails or the field is blank, the system shall position the cursor on the state field.

REQ-F-189: [Event-driven] When phone 2 line number validation fails or the field is blank, the system shall position the cursor on the phone 2 line number field.

REQ-F-190: [Event-driven] When EFT account ID validation fails or the field is blank, the system shall position the cursor on the EFT account ID field.

REQ-F-191: [Ubiquitous] The system shall display the customer primary and secondary phone numbers (by area code, exchange code, and line number components) and government-issued ID.

REQ-F-192: [Event-driven] When the account update screen input is received, the system shall extract the current balance; if the field is marked with an asterisk or is blank, the system shall store a low-value sentinel; otherwise, the system shall validate the entered value as numeric and, if valid, convert it to a signed decimal amount with two decimal places and store it in the new account details record.


---


## 12. Account Update Screen Input Validation and Date Editing
As an account operations user, I want the system to validate all date, numeric, and customer information fields submitted on the account update screen so that only correctly formatted and logically consistent data is accepted.


**Function Key Mapping**

### Requirements

REQ-F-193: [Event-driven] When the attention identifier equals the PF9 key code, the system shall set the PF9 key flag to TRUE.

REQ-F-194: [Event-driven] When the attention identifier equals the PF22 key code, the system shall set the PF10 key flag to TRUE, treating PF22 as an alias for PF10.

REQ-F-195: [Event-driven] When the attention identifier equals the PF4 key code, the system shall set the PF4 key flag to TRUE.

REQ-F-196: [Event-driven] When the attention identifier equals the PF11 key code, the system shall set the PF11 key flag to TRUE.

REQ-F-197: [Event-driven] When the attention identifier equals the PF18 key code, the system shall set the PF6 key flag to TRUE, treating PF18 as an alias for PF6.

REQ-F-198: [Event-driven] When the user presses the Enter key, the system shall set the Enter flag indicator to TRUE.

REQ-F-199: [Event-driven] When the attention identifier equals the PF6 key code, the system shall set the PF6 key flag to TRUE.

REQ-F-200: [Event-driven] When the attention identifier equals the PF17 key code, the system shall set the PF5 key flag to TRUE, treating PF17 as an alias for PF5.

REQ-F-201: [Event-driven] When the attention identifier equals the PF12 key code, the system shall set the PF12 key flag to TRUE.

REQ-F-202: [Event-driven] When the attention identifier equals the PF13 key code, the system shall set the PF1 key flag to TRUE, treating PF13 as an alias for PF1.

REQ-F-203: [Event-driven] When the attention identifier equals the PF5 key code, the system shall set the PF5 key flag to TRUE.

REQ-F-204: [Event-driven] When the attention identifier equals the PF10 key code, the system shall set the PF10 key flag to TRUE.

REQ-F-205: [Event-driven] When the attention identifier equals the PF14 key code, the system shall set the PF2 key flag to TRUE, treating PF14 as an alias for PF2.

REQ-F-206: [Event-driven] When the attention identifier equals the PF19 key code, the system shall set the PF7 key flag to TRUE, treating PF19 as an alias for PF7.

REQ-F-207: [Event-driven] When the attention identifier equals the PF21 key code, the system shall set the PF9 key flag to TRUE, treating PF21 as an alias for PF9.

REQ-F-208: [Event-driven] When the attention identifier equals the PA1 key code, the system shall set the PA1 key flag to TRUE.

REQ-F-209: [Event-driven] When the attention identifier equals the PA2 key code, the system shall set the PA2 key flag to TRUE.

REQ-F-210: [Event-driven] When the attention identifier equals the PF2 key code, the system shall set the PF2 key flag to TRUE.

REQ-F-211: [Event-driven] When the attention identifier equals the PF7 key code, the system shall set the PF7 key flag to TRUE.

REQ-F-212: [Event-driven] When the attention identifier equals the PF16 key code, the system shall set the PF4 key flag to TRUE, treating PF16 as an alias for PF4.

REQ-F-213: [Event-driven] When the attention identifier equals the PF20 key code, the system shall set the PF8 key flag to TRUE, treating PF20 as an alias for PF8.

REQ-F-214: [Event-driven] When the attention identifier equals the PF15 key code, the system shall set the PF3 key flag to TRUE, treating PF15 as an alias for PF3.

REQ-F-215: [Event-driven] When the attention identifier equals the PF3 key code, the system shall set the PF3 key flag to TRUE.

REQ-F-216: [Event-driven] When the attention identifier equals the PF8 key code, the system shall set the PF8 key flag to TRUE.

REQ-F-217: [Event-driven] When the attention identifier equals the PF1 key code, the system shall set the PF1 key flag to TRUE.

REQ-F-218: [Event-driven] When the attention identifier equals the PF23 key code, the system shall set the PF11 key flag to TRUE, treating PF23 as an alias for PF11.

REQ-F-219: [Event-driven] When the attention identifier equals the PF24 key code, the system shall set the PF12 key flag to TRUE, treating PF24 as an alias for PF12.

REQ-F-220: [Event-driven] When the attention identifier equals the CLEAR key code, the system shall set the CLEAR key flag to TRUE.

REQ-F-221: [Event-driven] When a function key is pressed, the system shall validate the key against the current account state; if the key is ENTER, PF3, PF5 (when changes are pending), or PF12 (when account details have been fetched), the system shall mark it valid; otherwise, the system shall force the key to ENTER.

REQ-F-222: [Event-driven] When the function key has been validated and the program is ready to dispatch, the system shall route to the appropriate handler: exit on PF3, fetch account details when not yet fetched and on first entry, return to menu when called from menu on non-re-entry, handle completion when changes are confirmed, handle failure when changes have failed, or process input for all other cases.

REQ-F-223: [Ubiquitous] The system shall define and execute the function key mapping dispatch table evaluating the terminal attention identifier against all supported key codes.

REQ-F-224: [Event-driven] When the account update screen input is received, the system shall extract the account identifier; if the field contains '*' or spaces, the system shall clear it to low-values in both the credit card work area and the new account details record; otherwise, the system shall copy the submitted value to both storage locations.

REQ-F-225: [Event-driven] When the account update screen input is received, the system shall extract the account active status; if the field contains '*' or spaces, the system shall clear it to low-values in the new account details record; otherwise, the system shall copy the submitted value to the new account details record.

REQ-F-226: [Event-driven] When the account update screen input is received, the system shall extract the account group identifier; if the field contains '*' or spaces, the system shall clear it to low-values in the new account details record; otherwise, the system shall copy the submitted value to the new account details record.

REQ-F-227: [Event-driven] When the account update screen input is received, the system shall extract the cash credit limit; if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value, validate it as numeric, and if valid, convert and store the numeric equivalent in the new account details record.

REQ-F-228: [Event-driven] When the account update screen input is received, the system shall extract the current balance; if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value, validate it as numeric, and if valid, convert and store the numeric equivalent in the new account details record.

REQ-F-229: [Event-driven] When the account update screen input is received, the system shall extract the credit limit; if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value, validate it as numeric, and if valid, convert and store the numeric equivalent in the new account details record.

REQ-F-230: [Event-driven] When the account update screen input is received, the system shall extract the current cycle debit; if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value, validate it as numeric, and if valid, convert and store the numeric equivalent in the new account details record.

REQ-F-231: [Event-driven] When the account update screen input is received, the system shall extract the current cycle credit; if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value, validate it as numeric, and if valid, convert and store the numeric equivalent in the new account details record.

REQ-F-232: [Event-driven] When the account update screen input is received, the system shall extract the account open date components (year, month, day); for each component, if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value to the corresponding date component field.

REQ-F-233: [Event-driven] When the account update screen input is received, the system shall extract the account expiration date components (year, month, day); for each component, if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value to the corresponding expiration date component field.

REQ-F-234: [Event-driven] When the account update screen input is received, the system shall extract the account reissue date components (year, month, day); for each component, if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value to the corresponding reissue date component field.

REQ-F-235: [Event-driven] When the account update screen input is received, the system shall extract the customer date of birth components (year, month, day); for each component, if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value to the corresponding date of birth component field.

REQ-F-236: [Event-driven] When the account update screen input is received, the system shall extract the customer name components (first name, middle name, last name); for each component, if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value to the corresponding name field.

REQ-F-237: [Event-driven] When the account update screen input is received, the system shall extract the Social Security Number components (part 1, part 2, part 3); for each component, if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value to the corresponding SSN component field.

REQ-F-238: [Event-driven] When the account update screen input is received, the system shall extract the customer phone number components (primary and secondary phone area codes, exchange codes, and line numbers); for each component, if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value to the corresponding phone component field.

REQ-F-239: [Event-driven] When the account update screen input is received, the system shall extract the customer EFT account identifier; if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value to the new account details record.

REQ-F-240: [Event-driven] When the account update screen input is received, the system shall extract the customer FICO score; if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value to the new account details record.

REQ-F-241: [Event-driven] When the account update screen input is received, the system shall extract the customer government-issued identification; if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value to the new account details record.

REQ-F-242: [Event-driven] When the account update screen input is received, the system shall extract the customer primary holder indicator; if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value to the new account details record.

REQ-F-243: [Event-driven] When the account update screen input is received, the system shall extract the customer identifier; if the field contains '*' or spaces, the system shall clear it to low-values; otherwise, the system shall copy the submitted value to the new account details record.

REQ-F-244: [Event-driven] When address line 1 input is received, the system shall, if the input is a deletion marker ('*') or blank, clear the address line 1 field to low-values; otherwise, the system shall store the input value in the address line 1 field.

REQ-F-245: [Event-driven] When the account update screen is submitted, the system shall extract the customer address line 2; if the field is marked with an asterisk or is blank, the system shall store a low-value sentinel; otherwise, the system shall store the entered address.

REQ-F-246: [Event-driven] When the account update screen is submitted, the system shall extract the customer state code; if the field is marked with an asterisk or is blank, the system shall store a low-value sentinel; otherwise, the system shall store the entered state code.

REQ-F-247: [Event-driven] When the account update screen is submitted, the system shall extract the customer ZIP code; if the field is marked with an asterisk or is blank, the system shall store a low-value sentinel; otherwise, the system shall store the entered ZIP code.

REQ-F-248: [Event-driven] When the account update screen is submitted, the system shall extract the customer city; if the field is marked with an asterisk or is blank, the system shall store a low-value sentinel; otherwise, the system shall store the entered city.

REQ-F-249: [Event-driven] When the account update screen is submitted, the system shall extract the customer country code; if the field is marked with an asterisk or is blank, the system shall store a low-value sentinel; otherwise, the system shall store the entered country code.

REQ-F-250: [Event-driven] When account details have not yet been fetched, the system shall validate the account identifier is supplied, numeric, 11 digits, and non-zero; if blank, the system shall set an error and display 'Account number not provided'; if non-numeric or zero, the system shall set an error and display 'Account Number if supplied must be a 11 digit Non-Zero Number'; if valid, the system shall mark it as valid and clear old account data.

REQ-F-251: [Event-driven] When the account ID search key is being validated, the system shall validate that the account ID is supplied, numeric, and non-zero; if blank, non-numeric, or zero, the system shall set error flags and record an error message.

REQ-F-252: [Event-driven] When a mandatory alphanumeric field is being validated, the system shall validate that the field is supplied and has a non-zero trimmed length; if blank or zero-length, the system shall set error flags and record an error message.

REQ-F-253: [Event-driven] When a required alphabetic field is being validated, the system shall validate that the field is supplied, has non-zero trimmed length, and contains only alphabetic characters; if blank, zero-length, or non-alphabetic, the system shall set error flags and record an error message.

REQ-F-254: [Event-driven] When an optional alphabetic field is being validated, the system shall validate that the field, if supplied, contains only alphabetic characters; if supplied and non-alphabetic, the system shall set error flags and record an error message.

REQ-F-255: [Event-driven] When a numeric field is blank, contains only spaces, or has zero length, the system shall set the input error flag, mark the field as blank, and generate a 'must be supplied' message.

REQ-F-256: [Event-driven] When a US state code is being validated, the system shall validate that the state code is a valid 2-character US state code (AL, AK, AZ, AR, CA, CO, CT, DE, FL, GA, HI, ID, IL, IN, IA, KS, KY, LA, ME, MD, MA, MI, MN, MS, MO, MT, NE, NV, NH, NJ, NM, NY, NC, ND, OH, OK, OR, PA, RI, SC, SD, TN, TX, UT, VT, VA, WA, WV, WI, WY, DC, AS, GU, MP, PR, VI); if invalid, the system shall set error flags and record an error message.

REQ-F-257: [Ubiquitous] The system shall validate the state-ZIP code combination by constructing a key from the state code and the first two digits of the ZIP code and checking it against the valid state-ZIP code list; if not valid, the system shall set an error and display 'Invalid zip code for state'.

REQ-F-258: [Ubiquitous] The system shall validate the FICO score is between 300 and 850 inclusive; if outside range, the system shall set an error and display the message that the field should be between 300 and 850.

REQ-F-259: [Ubiquitous] The system shall validate a US Social Security Number: SSN part 1 (3 digits) must not be 000, 666, or 900–999; SSN part 2 (2 digits) must be 01–99; SSN part 3 (4 digits) must be 0001–9999; if part 1 is 000, 666, or 900–999, the system shall display 'SSN: First 3 chars: should not be 000, 666, or between 900 and 999'; if any part fails numeric or zero validation, the system shall set an error.

REQ-F-260: [Ubiquitous] The system shall validate a signed numeric field with two decimal places: if the field is blank or low-values, the system shall display '<field-name> must be supplied'; if the field is supplied but not a valid signed number, the system shall display '<field-name> is not valid'.

REQ-F-261: [Event-driven] When a US phone number prefix code is submitted for validation, the system shall verify the prefix code is supplied and non-blank; if blank, the system shall set input error and record 'Prefix code must be supplied'; if non-numeric, the system shall set input error and record 'Prefix code must be A 3 digit number'; if zero, the system shall set input error and record 'Prefix code cannot be zero'.

REQ-F-262: [Event-driven] When a US phone number line number is submitted for validation, the system shall verify the line number is supplied and non-blank; if blank, the system shall set input error and record 'Line number code must be supplied'; if non-numeric, the system shall set input error and record 'Line number code must be A 4 digit number'; if zero, the system shall set input error and record 'Line number code cannot be zero'.

REQ-F-263: [Event-driven] When the area code field contains non-numeric characters, the system shall set the input error flag and record an error message stating 'Area code must be a 3 digit number'.

REQ-F-264: [Event-driven] When the area code numeric value equals zero, the system shall set the input error flag and record an error message stating 'Area code cannot be zero'.

REQ-F-265: [Event-driven] When the area code does not match any valid North American general-purpose area code, the system shall set the input error flag and record an error message stating 'Not valid North America general purpose area code'.

REQ-F-266: [Event-driven] When the area code field is blank or contains low-values, the system shall set the input error flag and record an error message stating 'Area code must be supplied'.

REQ-F-267: [Event-driven] When the year component is blank or contains only spaces, the system shall set the input error flag, mark the year as blank, and record a diagnostic message requiring the year to be supplied.

REQ-F-268: [Event-driven] When the year component is not numeric, the system shall set the input error flag, mark the year as invalid, and record a diagnostic message requiring the year to be a 4-digit number.

REQ-F-269: [Event-driven] When the century component is not 19 or 20, the system shall set the input error flag, mark the year as invalid, and record a diagnostic message stating that the century is not valid; when the century is valid, the system shall mark the year as valid.

REQ-F-270: [Event-driven] When the month component is blank or contains only spaces, the system shall set the input error flag, mark the month as blank, and record a diagnostic message requiring the month to be supplied.

REQ-F-271: [Event-driven] When the month component is not in the range 1–12 or cannot be converted to a number, the system shall set the input error flag, mark the month as invalid, and record a diagnostic message stating that the month must be a number between 1 and 12; when the month is valid, the system shall convert it to numeric form and mark the month as valid.

REQ-F-272: [Event-driven] When the day component is blank or contains only spaces, the system shall set the input error flag, mark the day as blank, and record a diagnostic message requiring the day to be supplied.

REQ-F-273: [Event-driven] When the day component contains non-numeric characters, the system shall set the input error flag, mark the day as invalid, and record a diagnostic message that the day must be a number between 1 and 31; otherwise, the system shall convert the day to numeric.

REQ-F-274: [Event-driven] When the day component is not between 1 and 31, the system shall set the input error flag, mark the day as invalid, and record a diagnostic message that the day must be a number between 1 and 31; otherwise, the system shall mark the day as valid.

REQ-F-275: [Event-driven] When the day is 31 but the month does not have 31 days, the system shall set the input error flag, mark the day and month as invalid, and record a diagnostic message stating that the month cannot have 31 days.

REQ-F-276: [Event-driven] When the day is 30 in February, the system shall set the input error flag, mark the day and month as invalid, and record a diagnostic message stating that February cannot have 30 days.

REQ-F-277: [Event-driven] When the day is 29 in February and the year is not a leap year, the system shall determine whether the year is a leap year by dividing the full year by 400 (if the year ends in 00) or by 4 (otherwise); if the remainder is not zero, the system shall set the input error flag, mark the day, month, and year as invalid, and record a diagnostic message stating that the year is not a leap year.

REQ-F-278: [Event-driven] When the external date validation service returns a non-zero severity code, the system shall set the input error flag, mark all date components (day, month, year) as invalid, and record a diagnostic message containing the severity and message codes.

REQ-F-279: [Event-driven] When no input errors were detected during date validation, the system shall mark the day validation flag as valid and set the overall date validation status to valid.

REQ-F-280: [Event-driven] When the date of birth is being validated, the system shall validate that the date of birth is not in the future by comparing the current date with the date of birth; if the date of birth is in the future, the system shall set error flags and record an error message.

REQ-F-281: [Ubiquitous] The system shall compare all account and customer fields (account ID, active status, current balance, credit limit, cash credit limit, open date, expiration date, reissue date, current cycle credit, current cycle debit, group ID, customer ID, first name, middle name, last name, address lines 1–3, state code, country code, ZIP code, phone numbers, SSN, government-issued ID, date of birth, EFT account ID, primary holder indicator, and FICO score) between new and old values; string fields shall be compared case-insensitively with leading/trailing spaces trimmed; if all fields match, the system shall set a no-changes flag; if any field differs, the system shall set a changes-occurred flag.

REQ-F-282: [Ubiquitous] The system shall validate all account and customer input fields (account identifier when first fetch, account status, dates, amounts, names, addresses, phone numbers, SSN, FICO score, EFT account ID, and primary holder indicator) and perform cross-field validation (state-ZIP code combination); if all validations pass, the system shall set the changes-pending-confirmation flag; if any validation fails, the system shall set the changes-not-OK flag.


---


## 13. Account Update Screen Navigation and Exit Handling
As an interactive user, I want function keys captured and mapped to internal codes, validated against the current screen state, and routed to the correct destination so that navigation from the account update screen behaves consistently regardless of which physical key is pressed.

### Requirements

REQ-F-283: [Ubiquitous] The system shall invoke the function key capture and mapping routine to process the attention identifier received from the user.

REQ-F-284: [Event-driven] When the attention identifier equals the ENTER key code, the system shall set the ENTER key flag to TRUE.

REQ-F-285: [Event-driven] When the attention identifier equals the CLEAR key code, the system shall set the CLEAR key flag to TRUE.

REQ-F-286: [Event-driven] When the attention identifier equals the PA1 key code, the system shall set the PA1 key flag to TRUE.

REQ-F-287: [Event-driven] When the attention identifier equals the PA2 key code, the system shall set the PA2 key flag to TRUE.

REQ-F-288: [Event-driven] When the attention identifier equals the PF1 key code, the system shall set the PF1 key flag to TRUE.

REQ-F-289: [Event-driven] When the attention identifier equals the PF13 key code, the system shall set the PF1 key flag to TRUE, treating PF13 as an alias for PF1.

REQ-F-290: [Event-driven] When the attention identifier equals the PF2 key code, the system shall set the PF2 key flag to TRUE.

REQ-F-291: [Event-driven] When the attention identifier equals the PF14 key code, the system shall set the PF2 key flag to TRUE, treating PF14 as an alias for PF2.

REQ-F-292: [Event-driven] When the attention identifier equals the PF3 key code, the system shall set the PF3 key flag to TRUE.

REQ-F-293: [Event-driven] When the attention identifier equals the PF15 key code, the system shall set the PF3 key flag to TRUE, treating PF15 as an alias for PF3.

REQ-F-294: [Event-driven] When the attention identifier equals the PF4 key code, the system shall set the PF4 key flag to TRUE.

REQ-F-295: [Event-driven] When the attention identifier equals the PF16 key code, the system shall set the PF4 key flag to TRUE, treating PF16 as an alias for PF4.

REQ-F-296: [Event-driven] When the attention identifier equals the PF5 key code, the system shall set the PF5 key flag to TRUE.

REQ-F-297: [Event-driven] When the attention identifier equals the PF17 key code, the system shall set the PF5 key flag to TRUE, treating PF17 as an alias for PF5.

REQ-F-298: [Event-driven] When the attention identifier equals the PF6 key code, the system shall set the PF6 key flag to TRUE.

REQ-F-299: [Event-driven] When the attention identifier equals the PF18 key code, the system shall set the PF6 key flag to TRUE, treating PF18 as an alias for PF6.

REQ-F-300: [Event-driven] When the attention identifier equals the PF7 key code, the system shall set the PF7 key flag to TRUE.

REQ-F-301: [Event-driven] When the attention identifier equals the PF19 key code, the system shall set the PF7 key flag to TRUE, treating PF19 as an alias for PF7.

REQ-F-302: [Event-driven] When the attention identifier equals the PF8 key code, the system shall set the PF8 key flag to TRUE.

REQ-F-303: [Event-driven] When the attention identifier equals the PF20 key code, the system shall set the PF8 key flag to TRUE, treating PF20 as an alias for PF8.

REQ-F-304: [Event-driven] When the attention identifier equals the PF9 key code, the system shall set the PF9 key flag to TRUE.

REQ-F-305: [Event-driven] When the attention identifier equals the PF21 key code, the system shall set the PF9 key flag to TRUE, treating PF21 as an alias for PF9.

REQ-F-306: [Event-driven] When the attention identifier equals the PF10 key code, the system shall set the PF10 key flag to TRUE.

REQ-F-307: [Event-driven] When the attention identifier equals the PF22 key code, the system shall set the PF10 key flag to TRUE, treating PF22 as an alias for PF10.

REQ-F-308: [Event-driven] When the attention identifier equals the PF11 key code, the system shall set the PF11 key flag to TRUE.

REQ-F-309: [Event-driven] When the attention identifier equals the PF23 key code, the system shall set the PF11 key flag to TRUE, treating PF23 as an alias for PF11.

REQ-F-310: [Event-driven] When the attention identifier equals the PF12 key code, the system shall set the PF12 key flag to TRUE.

REQ-F-311: [Event-driven] When the attention identifier equals the PF24 key code, the system shall set the PF12 key flag to TRUE, treating PF24 as an alias for PF12.

REQ-F-312: [Event-driven] When a function key is captured from the terminal, the system shall determine whether the pressed key is valid for the current screen state: the key is valid if it is ENTER, PF3, PF5 with account changes pending confirmation, or PF12 with account details already fetched; if none of these conditions are met, the system shall override the key to ENTER and mark it as invalid.

REQ-F-313: [Event-driven] When the user presses PF3 to exit the account update screen, the system shall determine the destination program and transaction from the session context: if the caller's transaction identifier is empty or spaces, the system shall route to the main menu (transaction CM00, program COMEN01C); otherwise, the system shall route to the caller's program and transaction.

REQ-F-314: [Event-driven] When the user presses PF3 to exit the account update screen, the system shall update the session context to record the current program (COACTUPC) and transaction (CAUP) as the originating context, set the user type to user, mark this as an initial entry (not a re-entry), record the current mapset (COACTUP) and map (CACTUPA) as last accessed, and transfer control to the destination program.

REQ-F-315: [Event-driven] When a valid function key is pressed and the PF3 exit case does not apply, the system shall evaluate the current screen state and function key to dispatch to the appropriate processing branch: initial fetch when account details are not yet loaded, menu return when re-entering from the menu program, completion handling when account changes are confirmed, or error handling when account changes processing has failed.


---


## 14. Account Update — Screen Field Error Highlighting on Re-entry
As an account update operator, I want invalid fields highlighted with a visual indicator on re-entry so that I can immediately identify which fields require correction.

### Requirements

REQ-F-316: [Complex] While the program is re-entering after processing, when the account status field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the account status field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-317: [Complex] While the program is re-entering after processing, when the credit limit field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the credit limit field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-318: [Complex] While the program is re-entering after processing, when the cash credit limit field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the cash credit limit field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-319: [Complex] While the program is re-entering after processing, when the current balance field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the current balance field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-320: [Complex] While the program is re-entering after processing, when the current cycle credit field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the current cycle credit field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-321: [Complex] While the program is re-entering after processing, when the current cycle debit field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the current cycle debit field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-322: [Complex] While the program is re-entering after processing, when the open year field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the open year field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-323: [Complex] While the program is re-entering after processing, when the open month field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the open month field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-324: [Complex] While the program is re-entering after processing, when the open day field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the open day field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-325: [Complex] While the program is re-entering after processing, when the expiration year field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the expiration year field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-326: [Complex] While the program is re-entering after processing, when the expiration month field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the expiration month field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-327: [Complex] While the program is re-entering after processing, when the expiration day field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the expiration day field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-328: [Complex] While the program is re-entering after processing, when the reissue year field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the reissue year field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-329: [Complex] While the program is re-entering after processing, when the reissue month field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the reissue month field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-330: [Complex] While the program is re-entering after processing, when the reissue day field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the reissue day field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-331: [Complex] While the program is re-entering after processing, when the FICO score field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the FICO score field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-332: [Complex] While the program is re-entering after processing, when the first name field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the first name field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-333: [Complex] While the program is re-entering after processing, when the middle name field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the middle name field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-334: [Complex] While the program is re-entering after processing, when the last name field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the last name field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-335: [Complex] While the program is re-entering after processing, when the primary card holder field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the primary card holder field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-336: [Complex] While the program is re-entering after processing, when the address line 1 field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the address line 1 field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-337: [Complex] While the program is re-entering after processing, when the address line 2 field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the address line 2 field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-338: [Complex] While the program is re-entering after processing, when the city field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the city field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-339: [Complex] While the program is re-entering after processing, when the address state code field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the address state code field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-340: [Complex] While the program is re-entering after processing, when the state field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the state field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-341: [Complex] While the program is re-entering after processing, when the country field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the country field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-342: [Complex] While the program is re-entering after processing, when the phone 1 area code field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the phone 1 area code field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-343: [Complex] While the program is re-entering after processing, when the phone 1 prefix field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the phone 1 prefix field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-344: [Complex] While the program is re-entering after processing, when the phone 1 line number field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the phone 1 line number field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-345: [Complex] While the program is re-entering after processing, when the phone 2 area code field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the phone 2 area code field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-346: [Complex] While the program is re-entering after processing, when the phone 2 prefix field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the phone 2 prefix field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-347: [Complex] While the program is re-entering after processing, when the phone 2 line number field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the phone 2 line number field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-348: [Complex] While the program is re-entering after processing, when the SSN part 1 field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the SSN part 1 field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-349: [Complex] While the program is re-entering after processing, when the SSN part 2 field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the SSN part 2 field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-350: [Complex] While the program is re-entering after processing, when the SSN part 3 field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the SSN part 3 field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-351: [Complex] While the program is re-entering after processing, when the date of birth year field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the date of birth year field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-352: [Complex] While the program is re-entering after processing, when the date of birth month field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the date of birth month field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-353: [Complex] While the program is re-entering after processing, when the date of birth day field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the date of birth day field; if the field is blank, the system shall place an asterisk in the field value.

REQ-F-354: [Complex] While the program is re-entering after processing, when the EFT account ID field validation flag indicates an error or blank condition, the system shall apply a red color indicator to the EFT account ID field; if the field is blank, the system shall place an asterisk in the field value.


---


## 15. Account Update — Screen Field Protection Setup
As an account update operator, I want editable fields unprotected and non-editable fields protected so that I can modify only the fields appropriate to the current update state.

### Requirements

REQ-F-355: [Ubiquitous] The system shall protect all fields with MDT, then unprotect account status, credit limits, dates, customer name, address, phone, and EFT fields, while keeping customer ID and country fields protected.

REQ-F-356: [Event-driven] When the change action state is ACUP-DETAILS-NOT-FETCHED, the system shall unprotect the account ID field to allow user entry.

REQ-F-357: [Event-driven] When the change action state is ACUP-CHANGES-NOT-OK, the system shall delegate to the field unprotection helper to enable editing of account and customer fields.

REQ-F-358: [Event-driven] When the change action state is ACUP-CHANGES-OK-NOT-CONFIRMED, ACUP-CHANGES-OKAYED-AND-DONE, or any other value not explicitly handled, the system shall leave field protection unchanged for confirmed and completed states and unprotect the account ID field for all other states.


---


## 16. Account Update — Account ID Field Color Control
As an account update operator, I want the account ID field color to reflect its validation state so that I can identify when a required account identifier is missing or invalid.

### Requirements

REQ-F-359: [Complex] While the program is re-entering after processing, when the account ID filter is blank, the system shall set the account ID field color to red and display an asterisk to mark the field as required but blank.

REQ-F-360: [Event-driven] When account ID filter validation fails, the system shall set the account ID field color to red.

REQ-F-361: [Event-driven] When the last accessed mapset is the card list mapset, the system shall set the account ID field color to default.


---


## 17. Account Update — Program Initialization
As an account update operator, I want the program to correctly initialize its state on first entry and restore context on re-entry so that the update workflow operates with accurate data throughout.

### Requirements

REQ-F-362: [Ubiquitous] The system shall initialize error handling, clear error messages, and restore or initialize the communication area and program context based on whether this is a first entry or re-entry.

REQ-F-363: [Ubiquitous] The system shall initialize working storage and communication areas; when no caller context is present or the caller is the menu program on first entry, the system shall clear all data and reset the account details state to not-fetched; otherwise, the system shall restore the caller's communication area and account update context from the input parameters.

REQ-F-364: [Ubiquitous] The system shall initialize the screen output buffer to LOW-VALUES and populate fixed header fields with the transaction ID, program name, current date, and current time.

REQ-F-365: [Ubiquitous] The system shall initialize the screen output buffer, populate screen fields with current data, set up information and error messages, configure field protection attributes, position the cursor, and send the screen to the user.


---


## 18. Account Update — Screen Input Receipt
As an account update operator, I want the system to receive and capture my screen input so that submitted data is available for validation and processing.

### Requirements

REQ-F-366: [Event-driven] When the account update screen is submitted, the system shall receive the screen input from the terminal, capture the response and reason codes, and initialize the new account details storage area.


---


## 19. Account Update — Completion and Error State Handling
As an account update operator, I want the system to correctly evaluate the outcome of a confirmed update and set the appropriate state so that I receive accurate feedback on whether the update succeeded or failed.

### Requirements

REQ-F-367: [Complex] While in confirmation-pending state, when the user confirms changes by pressing PF5, the system shall delegate to the write-processing operation to update the account master store and customer master store, then set state to lock-error if account lock failed, set state to update-failed if update failed after lock, return to detail-display if the record was changed externally, or set state to update-complete on success.


---


## 20. Account Update — Date Validation Initialization
As an account update operator, I want date fields validated through a consistent initialization and component-check sequence so that invalid dates are reliably detected.

### Requirements

REQ-F-368: [Ubiquitous] The system shall initialize the date validation state to invalid, then proceed through year, month, and day component checks.

REQ-F-369: [Ubiquitous] The system shall initialize the date validation result, set the date format to 'YYYYMMDD', and delegate to the date validation service to verify the date; if the severity returned is 0, the system shall mark the date as valid; if the severity is non-zero, the system shall set an error and display a message containing the field name, severity code, and message number.


---


## 21. Account Update — Numeric Field Validation Initialization
As an account update operator, I want numeric field validation to begin from a known failure state so that fields are only accepted when all checks explicitly pass.

### Requirements

REQ-F-370: [Ubiquitous] The system shall initialize the numeric field validation flag to failure state before performing numeric field checks.


---


## 22. Account Activity Screen — Function Key Mapping
As an interactive user, I want my key presses recognized and mapped to named actions so that the application can route my request correctly.

### Requirements

REQ-F-371: [Ubiquitous] The system shall evaluate the terminal attention identifier and map it to the corresponding function key indicator by comparing against all supported key codes (ENTER, CLEAR, PA1, PA2, PF1–PF12, and extended PF13–PF24).

REQ-F-372: [Event-driven] When the attention identifier matches the ENTER key code, the system shall set the ENTER function key indicator to active.

REQ-F-373: [Event-driven] When the attention identifier matches the CLEAR key code, the system shall set the CLEAR function key indicator to active.

REQ-F-374: [Event-driven] When the attention identifier matches the PA1 key code, the system shall set the PA1 function key indicator to active.

REQ-F-375: [Event-driven] When the attention identifier matches the PA2 key code, the system shall set the PA2 function key indicator to active.

REQ-F-376: [Event-driven] When the attention identifier matches the PF1 key code, the system shall set the PF1 function key indicator to active.

REQ-F-377: [Event-driven] When the attention identifier matches the PF2 key code, the system shall set the PF2 function key indicator to active.

REQ-F-378: [Event-driven] When the attention identifier matches the PF3 key code, the system shall set the PF3 function key indicator to active.

REQ-F-379: [Event-driven] When the attention identifier matches the PF4 key code, the system shall set the PF4 function key indicator to active.

REQ-F-380: [Event-driven] When the attention identifier matches the PF5 key code, the system shall set the PF5 function key indicator to active.

REQ-F-381: [Event-driven] When the attention identifier matches the PF6 key code, the system shall set the PF6 function key indicator to active.

REQ-F-382: [Event-driven] When the attention identifier matches the PF7 key code, the system shall set the PF7 function key indicator to active.

REQ-F-383: [Event-driven] When the attention identifier matches the PF8 key code, the system shall set the PF8 function key indicator to active.

REQ-F-384: [Event-driven] When the attention identifier matches the PF9 key code, the system shall set the PF9 function key indicator to active.

REQ-F-385: [Event-driven] When the attention identifier matches the PF10 key code, the system shall set the PF10 function key indicator to active.

REQ-F-386: [Event-driven] When the attention identifier matches the PF11 key code, the system shall set the PF11 function key indicator to active.

REQ-F-387: [Event-driven] When the attention identifier matches the PF12 key code, the system shall set the PF12 function key indicator to active.

REQ-F-388: [Event-driven] When the attention identifier matches the PF13 key code, the system shall set the PF1 function key indicator to active (aliasing PF13 to PF1).

REQ-F-389: [Event-driven] When the attention identifier matches the PF14 key code, the system shall set the PF2 function key indicator to active (aliasing PF14 to PF2).

REQ-F-390: [Event-driven] When the attention identifier matches the PF15 key code, the system shall set the PF3 function key indicator to active (aliasing PF15 to PF3).

REQ-F-391: [Event-driven] When the attention identifier matches the PF16 key code, the system shall set the PF4 function key indicator to active (aliasing PF16 to PF4).

REQ-F-392: [Event-driven] When the attention identifier matches the PF17 key code, the system shall set the PF5 function key indicator to active (aliasing PF17 to PF5).

REQ-F-393: [Event-driven] When the attention identifier matches the PF18 key code, the system shall set the PF6 function key indicator to active (aliasing PF18 to PF6).

REQ-F-394: [Event-driven] When the attention identifier matches the PF19 key code, the system shall set the PF7 function key indicator to active (aliasing PF19 to PF7).

REQ-F-395: [Event-driven] When the attention identifier matches the PF20 key code, the system shall set the PF8 function key indicator to active (aliasing PF20 to PF8).

REQ-F-396: [Event-driven] When the attention identifier matches the PF21 key code, the system shall set the PF9 function key indicator to active (aliasing PF21 to PF9).

REQ-F-397: [Event-driven] When the attention identifier matches the PF22 key code, the system shall set the PF10 function key indicator to active (aliasing PF22 to PF10).

REQ-F-398: [Event-driven] When the attention identifier matches the PF23 key code, the system shall set the PF11 function key indicator to active (aliasing PF23 to PF11).

REQ-F-399: [Event-driven] When the attention identifier matches the PF24 key code, the system shall set the PF12 function key indicator to active (aliasing PF24 to PF12).


---


## 23. Account Activity Screen — Function Key Validation and Program Flow Dispatch
As an interactive user, I want the system to accept only valid key presses for the current screen and route me to the correct destination so that I can navigate the application predictably.

### Requirements

REQ-F-400: [Event-driven] When a key press is received, the system shall mark all keys as invalid initially; if the user pressed ENTER or PF3, the system shall mark the key as valid; if the key remains invalid, the system shall override the input and set the key to ENTER.

REQ-F-401: [Complex] While the program is active and awaiting user input or re-entry, when a function key is pressed or the program is re-entered with user input, the system shall route to the menu program when PF3 is pressed, display the account activity search screen on first entry, process and validate user input on re-entry and display the populated account screen on success, or redisplay the screen with error messages on validation failure.

REQ-F-402: [Unwanted] If an input error flag remains set after the main dispatch, the system shall redisplay the account activity screen to present the error message to the user.


---


## 24. Account Activity Screen — Exit Navigation
As an interactive user, I want pressing PF3 to return me to the appropriate prior context so that I can exit the account activity screen without losing my place in the application.

### Requirements

REQ-F-403: [Event-driven] When the user presses PF3 (exit) and the originating transaction identifier is empty or spaces, the system shall route to the menu program and menu transaction.

REQ-F-404: [Event-driven] When the user presses PF3 (exit) and the originating transaction identifier is not empty, the system shall route to the calling program and its transaction.

REQ-F-405: [Event-driven] When the exit destination has been determined, the system shall populate the navigation context with the current program name, current transaction identifier, user type (user), program context (initial entry), and current screen and screen-set names, then transfer control to the destination program via the shared communication area.


---


## 25. Account Activity Screen — User Input Receipt and Account Identifier Validation
As an interactive user, I want the system to validate the account identifier I enter so that only well-formed, non-zero 11-digit account numbers are accepted for data retrieval.

### Requirements

REQ-F-406: [Event-driven] When the user submits input from the screen, the system shall receive the screen input, normalize the account identifier by replacing a wildcard ('*') or spaces with low-values, and then validate the account identifier field.

REQ-F-407: [Unwanted] If the account identifier field is blank after editing, the system shall set an error flag and record the message 'No input received'.

REQ-F-408: [Event-driven] When the account identifier field is validated and the field is blank or low-values, the system shall reject the account identifier, clear the account identifier in the session context, and record the message 'Account number not provided'.

REQ-F-409: [Event-driven] When the account identifier field is validated and the field is not numeric or equals zero, the system shall reject the account identifier, clear the account identifier in the session context, and record the message 'Account Filter must be a non-zero 11 digit number'.

REQ-F-410: [Event-driven] When the account identifier field is validated and the field is numeric and non-zero, the system shall accept the account identifier, copy it to the session context, and mark it as valid.


---


## 26. Account Activity Screen — Screen Preparation
As an interactive user, I want the screen to display appropriate guidance before I enter search criteria so that I know what data to provide.

### Requirements

REQ-F-411: [Event-driven] When the screen is about to be displayed and the session context is empty (first entry) or no information message is currently set, the system shall set the information message to prompt the user to enter or update the account identifier.


---


## 27. Account Activity Screen — Account and Customer Data Retrieval
As an interactive user, I want the system to retrieve account and customer data after a valid account identifier is entered so that the populated account activity screen can be displayed.

### Requirements

REQ-F-412: [Event-driven] When the account identifier has been validated and accepted, the system shall clear the information message, convert the account identifier to alphanumeric format, retrieve the card cross-reference record from the card cross-reference data store (AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH) using the account identifier, retrieve the account master record from the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) using the account identifier, convert the retrieved customer identifier to alphanumeric format, and retrieve the customer master record from the customer data store (AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS) using the customer identifier.

REQ-F-413: [Event-driven] When the card cross-reference record is retrieved successfully, the system shall extract the customer identifier (9-digit numeric) from the cross-reference record and store it in the session context for subsequent use.

REQ-F-414: [Unwanted] If the card cross-reference record is not found, the system shall mark the account filter as invalid and, if no error message has been set, record the message 'Account: \<account-id\> not found in Cross ref file. Resp: \<resp-code\> Reas: \<reason-code\>'.

REQ-F-415: [Unwanted] If an error other than not-found occurs when retrieving the card cross-reference record, the system shall mark the account filter as invalid and record the operation name (READ), the data store name, the response code, and the reason code as the error message.

REQ-F-416: [Unwanted] If the account master record is not found, the system shall mark the account filter as invalid and, if no error message has been set, record the message 'Account: \<account-id\> not found in Acct Master file. Resp: \<resp-code\> Reas: \<reason-code\>'.

REQ-F-417: [Unwanted] If an error other than not-found occurs when retrieving the account master record, the system shall mark the account filter as invalid and record the operation name (READ), the data store name, the response code, and the reason code as the error message.

REQ-F-418: [Event-driven] When the customer identifier is available, the system shall retrieve the customer master record from the customer data store using the customer identifier as the key.


---


## 28. Administration Menu Screen Display and Navigation
As an administrator, I want the administration menu to display available options and respond correctly to my key presses so that I can navigate to the appropriate administrative function.

### Requirements

REQ-F-419: [Event-driven] When the administration menu program is invoked with no prior session context, the system shall display the administration menu screen immediately with a cleared output area.

REQ-F-420: [Complex] While the program is re-entered and the session context indicates this is not yet a re-entry, when the administration menu program is invoked, the system shall clear the output area and display the administration menu screen.

REQ-F-421: [Complex] While the program is re-entered in re-entry mode, when the user presses Enter on the administration menu screen, the system shall receive the screen input and route to option validation and processing.

REQ-F-422: [Complex] While the program is re-entered in re-entry mode, when the user presses any key other than Enter or PF3 on the administration menu screen, the system shall place the standard invalid-key message into the message area and re-display the administration menu screen.

REQ-F-423: [Event-driven] When the user presses Enter on the administration menu screen, the system shall trim trailing spaces from the option input, normalize internal spaces to zeros, convert the result to a numeric option number, and validate that the option is numeric, within the range 1–6, and not zero.

REQ-F-424: [Unwanted] If the option input is non-numeric, exceeds the maximum available option count (6), or equals zero, the system shall set the error flag to active and display the message 'Please enter a valid option number...' and re-display the administration menu screen.

REQ-F-425: [Unwanted] If a program-not-found condition occurs during menu option navigation, the system shall display the message 'This option is not installed ...' and re-display the administration menu screen.

REQ-F-426: [Ubiquitous] The system shall display the administration menu screen with the current message text placed in the error message display area.


---


## 29. Administrator Menu Option Selection and Navigation
As an administrator, I want my validated menu selection to transfer control to the corresponding administrative function so that I can perform the chosen administrative task.

### Requirements

REQ-F-427: [Complex] While the program is re-entered in re-entry mode, when the user presses Enter on the administration menu screen, the system shall receive the administrator menu screen input and process the selected option.

REQ-F-428: [Event-driven] When the user submits a menu option selection, the system shall extract and trim the option input, normalize spaces to zeros, convert to numeric, and validate that the option is numeric, within range 1–6, and not zero; if any validation fails, the system shall set the error flag to active.


---


## 30. Signon Screen Navigation Control
As a user, I want the application to route me to the signon screen when no session context exists or when I press PF3, so that I can authenticate or re-authenticate as needed.

### Requirements

REQ-F-429: [Event-driven] When the program is invoked with no communication area, the system shall transfer control to the signon screen program.

REQ-F-430: [Event-driven] When the program is invoked with a communication area present, the system shall load the communication area data into the local session context.

REQ-F-431: [Event-driven] When the user presses PF3, the system shall set the destination program to the signon screen program and transfer control to it.

REQ-F-432: [Event-driven] When the destination program name is empty or unset (contains low-values or spaces), the system shall default the destination program name to the signon screen program before transferring control.

REQ-F-433: [Ubiquitous] The system shall transfer control to the destination program specified in the session context.


---


## 31. Signon Screen Display and Credential Validation (Signon Program)
As a user, I want to enter my credentials on the signon screen and receive clear feedback so that I can authenticate and be routed to the correct menu.

### Requirements

REQ-F-434: [Event-driven] When the user submits the signon form by pressing Enter, the system shall validate that both the user ID and password fields are non-empty; if the user ID is empty, the system shall display the message 'Please enter User ID ...' and re-display the signon screen; if the password is empty, the system shall display the message 'Please enter Password ...' and re-display the signon screen.

REQ-F-435: [Event-driven] When both user ID and password fields are populated, the system shall convert both to uppercase, store them, and proceed to credential verification against the user security file.

REQ-F-436: [Event-driven] When the user submits valid non-empty credentials, the system shall read the user security file using the user ID as key; if the stored password matches the entered password, the system shall populate the session context with the user's transaction ID, program name, user ID, and user type and transfer control to the administrator menu program if the user type is administrator, or to the general menu program otherwise.

REQ-F-437: [Unwanted] If the password does not match the stored password, the system shall display the message 'Wrong Password. Try again ...' and re-display the signon screen.

REQ-F-438: [Unwanted] If the user ID is not found in the user security file (not-found response), the system shall display the message 'User not found. Try again ...' and re-display the signon screen.

REQ-F-439: [Unwanted] If a system error occurs during the user security file read, the system shall display the message 'Unable to verify the User ...' and re-display the signon screen.

REQ-F-440: [Event-driven] When the user presses Enter on the signon screen, the system shall invoke the enter-key credential processing logic.

REQ-F-441: [Unwanted] If any key other than Enter is pressed on the signon screen, the system shall display the message 'Invalid key pressed. Please see below...' and re-display the signon screen.

REQ-F-442: [Ubiquitous] The system shall display the signon screen with the current error message placed in the screen output area.


---


## 32. Bill Payment Transaction Processing
As an interactive user, I want to submit a bill payment against my account so that my account balance is reduced and a transaction record is created confirming the payment.

### Requirements

REQ-F-443: [Event-driven] When the account ID input field is empty or contains only spaces, the system shall set the error flag, place the message 'Acct ID can NOT be empty...' in the message buffer, reposition the cursor to the account ID input field, and redisplay the bill payment screen.

REQ-F-444: [Complex] While the account ID is valid and not empty, when the user enters a confirmation value of 'Y' or 'y', the system shall set the confirmation flag to confirmed and retrieve the account record from the account data store (AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS).

REQ-F-445: [Complex] While the account ID is valid and not empty, when the user enters a confirmation value of 'N' or 'n', the system shall clear the screen and set the error flag.

REQ-F-446: [Complex] While the account ID is valid and not empty, when the confirmation field is empty, the system shall retrieve the account record from the account data store without setting the confirmation flag.

REQ-F-447: [Complex] While the account ID is valid and not empty, when the confirmation field contains any value other than 'Y', 'y', 'N', 'n', or empty, the system shall set the error flag, place the message 'Invalid value. Valid values are (Y/N)...' in the message buffer, reposition the cursor to the confirmation field, and redisplay the bill payment screen.

REQ-F-448: [Event-driven] When the system needs to retrieve account information for the entered account ID, the system shall read the account record from the account data store using the account ID as the key with an update lock; if the account is not found, the system shall set the error flag and display the message 'Account ID NOT found...'; if any other error occurs, the system shall set the error flag and display the message 'Unable to lookup Account...'.

REQ-F-449: [Event-driven] When account ID and confirmation input validation succeeds, the system shall retrieve the current account balance from the account record and display it on the bill payment screen.

REQ-F-450: [Event-driven] When the account balance is zero or negative and the account ID input field contains data, the system shall set the error flag, display the message 'You have nothing to pay...', reposition the cursor to the account ID input field, and display the bill payment screen.

REQ-F-451: [Event-driven] When the user confirms the bill payment (confirmation flag is 'Y') and no error flag is set, the system shall retrieve the card number from the card cross-reference data store (AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH) using the account ID as the key; if the account ID is not found, the system shall display the message 'Account ID NOT found...'; if any other error occurs, the system shall display the message 'Unable to lookup XREF AIX file...'.

REQ-F-452: [Event-driven] When the system needs to determine the highest existing transaction ID, the system shall start a backward browse of the transaction data store (AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS) positioned at the highest transaction ID (high-values); if no transaction is found, the system shall display the message 'Transaction ID NOT found...'; if any other error occurs, the system shall display the message 'Unable to lookup Transaction...'.

REQ-F-453: [Event-driven] When the system reads the previous transaction record to find the last transaction ID, the system shall use the retrieved transaction ID as the basis for generating the next ID; if end-of-file is reached indicating no prior transactions exist, the system shall set the transaction ID to zeros; if any other error occurs, the system shall display the message 'Unable to lookup Transaction...'.

REQ-F-454: [Ubiquitous] The system shall end the browse of the transaction data store after reading the previous transaction record to release the cursor and any associated locks.

REQ-F-455: [Event-driven] When the user confirms the bill payment and no error flag is set, the system shall increment the highest existing transaction ID by 1 and populate a new transaction record in the transaction data store with: the incremented transaction ID (alphanumeric, 16 characters), transaction type code '02', transaction category code 2, transaction source 'POS TERM', transaction description 'BILL PAYMENT - ONLINE', transaction amount equal to the account current balance, card number from the card cross-reference record, merchant ID 999999999, merchant name 'BILL PAYMENT', merchant city 'N/A', merchant zip 'N/A', and the current timestamp in both the original and processing timestamp fields.

REQ-F-456: [Event-driven] When a bill payment transaction record is ready to be written, the system shall write the transaction record to the transaction data store; if a duplicate transaction ID is detected, the system shall display the message 'Tran ID already exist...'; if any other error occurs, the system shall display the message 'Unable to Add Bill pay Transaction...'; on success, the system shall initialize all screen fields and display the success message 'Payment successful. Your Transaction ID is <transaction-id>.'.

REQ-F-457: [Event-driven] When the transaction record has been successfully written, the system shall compute the new account balance by subtracting the transaction amount from the current balance and rewrite the account record to the account data store; if the account is not found, the system shall display the message 'Account ID NOT found...'; if any other error occurs, the system shall display the message 'Unable to Update Account...'.

REQ-F-458: [Event-driven] When the user has not confirmed the bill payment (confirmation flag is not 'Y') and no error flag is set, the system shall display the message 'Confirm to make a bill payment...' and reposition the cursor to the confirmation input field.

REQ-F-459: [Ubiquitous] The system shall receive the user's account ID and confirmation inputs from the bill payment screen input buffer, capturing the response and reason codes for subsequent error handling.

REQ-F-460: [Ubiquitous] The system shall set the account ID input field cursor position to -1 and clear the account ID input, current balance input, confirmation input, and message text fields to spaces when resetting all screen input fields to their initial state.

REQ-F-461: [Ubiquitous] The system shall retrieve the current system date and time, populate the bill payment screen header with the titles 'AWS Mainframe Modernization' and 'CardDemo', the transaction identifier code 'CB00', the program name 'COBIL00C', the current date formatted as MM/DD/YY, and the current time formatted as HH:MM:SS.

REQ-F-462: [Ubiquitous] The system shall display the bill payment screen with current account information, messages, and status after all validation and processing logic completes.


### Open Questions

OQ-004: Design decision — the original code does not enforce atomicity for this operation (was REQ-N). Determine whether the modernized system requires transactional guarantees: When a confirmed bill payment is processed, the system shall write the transaction record to the transaction data sto...


---


## 33. Interactive Navigation and Screen Control
As an interactive user, I want the application to route me to the correct screen based on my key presses and navigation context so that I can move through the card demonstration application workflows.

### Requirements

REQ-F-463: [Event-driven] When the program is invoked with no communication area, the system shall set the destination program to the signon screen and transfer control to it.

REQ-F-464: [Event-driven] When the program is invoked with a communication area, the system shall copy the incoming communication area into working storage to preserve the navigation context and application state.

REQ-F-465: [Event-driven] When the user presses the ENTER key, the system shall evaluate the attention identifier and route based on ENTER key input.

REQ-F-466: [Event-driven] When the user presses the PF3 key, the system shall set the destination to the originating program name if known, or default to the menu screen ('COMEN01C') if the originating program name is empty or contains low-values, then transfer control to the destination program.

REQ-F-467: [Event-driven] When the user presses the PF4 key, the system shall route based on PF4 key input.

REQ-F-468: [Event-driven] When the user presses an unrecognized key (any key other than ENTER, PF3, or PF4), the system shall handle the unrecognized key input.

REQ-F-469: [Ubiquitous] The system shall validate the destination program name and default it to the signon screen ('COSGN00C') if it is empty or contains low-values, set the originating transaction identifier to 'CB00', set the originating program name to 'COBIL00C', and reset the program context indicator to zero before transferring control.

REQ-F-470: [Ubiquitous] The system shall transfer control to the destination program with the updated communication area.


### Open Questions

OQ-005: Rules for ENTER key routing (REQ-F-023) and PF4 key routing (REQ-F-025) state that routing logic is handled elsewhere; the specific destination programs for these key presses are not defined in the provided rules. — Owner: application navigation team

OQ-006: Rule for unrecognized key handling (REQ-F-026) states the handling logic is not shown; the specific behavior (error message, default navigation, or no-op) is not defined in the provided rules. — Owner: application navigation team

OQ-007: REQ-N-001 covers atomicity of the transaction write and account balance update. No rule item explicitly describes rollback behavior if the account rewrite fails after the transaction record is successfully written. Should a compensating action (e.g., deletion of the written transaction record) be performed? — Owner: payments domain team


---


## 34. Card Listing and Pagination Interface
As a user, I want to browse a paginated list of payment cards filtered by account and card number so that I can locate and select a card for viewing or updating.


#### Function Key Mapping

### Requirements

REQ-F-471: [Ubiquitous] The system shall evaluate the terminal attention identifier and dispatch to the appropriate action mapping, setting the corresponding function key flag (ENTER, CLEAR, PA1, PA2, or PF1 through PF24) to TRUE.

REQ-F-472: [Event-driven] When the user presses the Enter key, the system shall set the action indicator to the Enter state.

REQ-F-473: [Event-driven] When the attention identifier matches the CLEAR key code, the system shall set the CLEAR function key indicator to TRUE.

REQ-F-474: [Event-driven] When the attention identifier matches the PA1 key code, the system shall set the PA1 function key indicator to TRUE.

REQ-F-475: [Event-driven] When the attention identifier matches the PA2 key code, the system shall set the PA2 function key indicator to TRUE.

REQ-F-476: [Event-driven] When the attention identifier matches the PF1 key code, the system shall set the PF1 function key indicator to TRUE.

REQ-F-477: [Event-driven] When the attention identifier matches the PF2 key code, the system shall set the PF2 function key indicator to TRUE.

REQ-F-478: [Event-driven] When the attention identifier matches the PF3 key code, the system shall set the PF3 function key indicator to TRUE.

REQ-F-479: [Event-driven] When the attention identifier matches the PF4 key code, the system shall set the PF4 function key indicator to TRUE.

REQ-F-480: [Event-driven] When the attention identifier matches the PF5 key code, the system shall set the PF5 function key indicator to TRUE.

REQ-F-481: [Event-driven] When the attention identifier matches the PF6 key code, the system shall set the PF6 function key indicator to TRUE.

REQ-F-482: [Event-driven] When the attention identifier matches the PF7 key code, the system shall set the PF7 function key indicator to TRUE.

REQ-F-483: [Event-driven] When the attention identifier matches the PF8 key code, the system shall set the PF8 function key indicator to TRUE.

REQ-F-484: [Event-driven] When the attention identifier matches the PF9 key code, the system shall set the PF9 function key indicator to TRUE.

REQ-F-485: [Event-driven] When the attention identifier matches the PF10 key code, the system shall set the PF10 function key indicator to TRUE.

REQ-F-486: [Event-driven] When the attention identifier matches the PF11 key code, the system shall set the PF11 function key indicator to TRUE.

REQ-F-487: [Event-driven] When the attention identifier matches the PF12 key code, the system shall set the PF12 function key indicator to TRUE.

REQ-F-488: [Event-driven] When the attention identifier matches the PF13 key code, the system shall set the PF1 function key indicator to TRUE.

REQ-F-489: [Event-driven] When the attention identifier matches the PF14 key code, the system shall set the PF2 function key indicator to TRUE.

REQ-F-490: [Event-driven] When the attention identifier matches the PF15 key code, the system shall set the PF3 function key indicator to TRUE.

REQ-F-491: [Event-driven] When the attention identifier matches the PF16 key code, the system shall set the PF4 function key indicator to TRUE.

REQ-F-492: [Event-driven] When the attention identifier matches the PF17 key code, the system shall set the PF5 function key indicator to TRUE.

REQ-F-493: [Event-driven] When the attention identifier matches the PF18 key code, the system shall set the PF6 function key indicator to TRUE.

REQ-F-494: [Event-driven] When the attention identifier matches the PF19 key code, the system shall set the PF7 function key indicator to TRUE.

REQ-F-495: [Event-driven] When the attention identifier matches the PF20 key code, the system shall set the PF8 function key indicator to TRUE.

REQ-F-496: [Event-driven] When the attention identifier matches the PF21 key code, the system shall set the PF9 function key indicator to TRUE.

REQ-F-497: [Event-driven] When the attention identifier matches the PF22 key code, the system shall set the PF10 function key indicator to TRUE.

REQ-F-498: [Event-driven] When the attention identifier matches the PF23 key code, the system shall set the PF11 function key indicator to TRUE.

REQ-F-499: [Event-driven] When the user presses the PF24 key, the system shall set the action indicator to PF12.

REQ-F-500: [Event-driven] When a function key has been mapped from the user's keyboard input, the system shall check if the function key is one of the valid keys (ENTER, PF3, PF7, or PF8); if valid, mark the key as valid; if invalid, reset the key to ENTER.

REQ-F-501: [Ubiquitous] The system shall receive the card list screen input and extract the account identifier, card number, and seven card selection indicators (one for each row on the display) into the work area.

REQ-F-502: [Event-driven] When user submits the screen form, the system shall receive the screen input map, extract the account identifier, card number, and selection flags, and validate each field against business rules.

REQ-F-503: [Event-driven] When the account identifier field is received from the screen, the system shall check if the account identifier is empty, contains only zeros, or is non-numeric; if any of these conditions is true, set the account identifier to zero; otherwise, store the numeric account identifier value.

REQ-F-504: [Event-driven] When the card number field is received from the screen, the system shall check if the card number is empty, contains only zeros, or is non-numeric; if any of these conditions is true, set the card number to zero; otherwise, store the numeric card number value.

REQ-F-505: [Event-driven] When card selection flags are submitted, the system shall count the number of 'S' and 'U' selections; if more than one selection is made, mark input as invalid and set error message 'PLEASE SELECT ONLY ONE RECORD TO VIEW OR UPDATE'; iterate through each row and validate the selection flag; for valid selections, store the row index; for invalid selections, mark the row with an error and set error message 'INVALID ACTION CODE' (if no prior error).

REQ-F-506: [Event-driven] When the program is entered fresh or re-entered from the menu, the system shall display the account ID filter on the screen — from the stored filter value when validation has occurred, or from the incoming context when no prior validation exists — and mark the field with the edit indicator when a value is present; if the incoming account ID is zero, the screen field shall be cleared.

REQ-F-507: [Event-driven] When the program is entered fresh or re-entered from the menu, the system shall display the card ID filter on the screen — from the stored card ID when validation has occurred, or from the incoming context when no prior validation exists — and mark the field with the edit indicator when a value is present; if the incoming card ID is zero, the screen field shall be cleared.

REQ-F-508: [Event-driven] When the user presses PF3 and the originating program is the current program, the system shall exit the transaction and return to the main menu.

REQ-F-509: [Event-driven] When the user presses PF7 and the current page is not the first page, the system shall retrieve the previous page of cards starting from the stored first card number, decrement the screen page number, and display the updated card list.

REQ-F-510: [Event-driven] When the user presses PF7 while on the first page, the system shall recognize the invalid page-up attempt and take no action.

REQ-F-511: [Event-driven] When the user presses PF7 while on the first page, the system shall retrieve the first page of cards starting from the stored first card number and display the card list.

REQ-F-512: [Event-driven] When the user presses PF8 and a next page exists, the system shall retrieve the next page of cards starting from the stored last card number, increment the screen page number, and display the updated card list.

REQ-F-513: [Event-driven] When the user does not press PF8, the system shall reset the last-page indicator to indicate the last page has not been shown.

REQ-F-514: [Event-driven] When the user presses Enter and has selected a card for viewing (marked with 'S') and the originating program is the current program, the system shall transfer control to the card detail view.

REQ-F-515: [Event-driven] When the user presses Enter and has selected a card for updating (marked with 'U') and the originating program is the current program, the system shall transfer control to the card update view.

REQ-F-516: [Event-driven] When the user performs an unrecognized action, the system shall retrieve the first page of cards starting from the stored first card number and display the card list.

REQ-F-517: [Event-driven] When input validation fails, the system shall set the originating program to the current program; if both account and card filters are valid, retrieve the first page of matching cards; display the card list.

REQ-F-518: [Event-driven] When all input validation has passed, the system shall position the cursor at the account ID input field.

REQ-F-519: [Event-driven] When a card record is read from the card data store (AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS), the system shall mark the record as included; if the account filter is valid and the record's account identifier does not match the filter, mark the record as excluded; if the card filter is valid and the record's card number does not match the filter, mark the record as excluded.

REQ-F-520: [Event-driven] When the backward browse operation is initiated on the card data store, the system shall, if the response code is NORMAL or DUPREC, decrement the screen counter by one; if the response code is any other value, record the operation name as 'READ', the file name, and the response codes in the error message structure.

REQ-F-521: [State-driven] While records remain to be read and the screen buffer is not full, the system shall read the next card record in reverse order; when the response code is NORMAL or DUPREC, invoke the filter routine; if the record passes the filter, copy the card number, account identifier, and active status to the screen row, decrement the screen counter, and exit the loop when the counter reaches 0; when the response code is any other value, exit the loop and record the error details (operation name 'READ', file name, and response codes).

REQ-F-522: [Ubiquitous] The system shall terminate the backward browse operation on the card data store to release the browse cursor.

REQ-F-523: [State-driven] While the read loop is active and the screen has not yet reached the maximum row capacity of 7 rows, the system shall retrieve the next card record from the browse, apply filtering logic, and when the record passes the filter, add it to the screen display by incrementing the screen counter and populating the row with the card number, account identifier, and active status; when the first record is added, increment the screen page number if it is zero.

REQ-F-524: [Ubiquitous] The system shall close the browse cursor on the card data store after the read loop completes.

REQ-F-525: [Event-driven] When end-of-file is reached during the main record read loop, the system shall exit the read loop, set the next-page indicator to false, display 'NO MORE RECORDS TO SHOW' if no error message is already set, and if this is the first page with no records retrieved, set the no-records-found flag.

REQ-F-526: [Unwanted] If a read error occurs during the main record read loop, the system shall exit the read loop, record the operation name as 'READ', capture the card data store name and response codes in the error message structure, and display the error message.

REQ-F-527: [Complex] While the screen row counter has reached the maximum of 7 displayable rows, when the screen becomes full after staging a card record, the system shall exit the read loop, perform a lookahead read to check for next-page availability, and set the next-page indicator to: exists if the response code is NORMAL or DUPREC; does not exist if the response code is ENDFILE (with 'NO MORE RECORDS TO SHOW' message if no error message is already set); or error if any other response code (recording operation name 'READ', file name, and both response and reason codes in the error message buffer).

REQ-F-528: [Event-driven] When the first card detail row in the buffer contains data, the system shall transfer the card selection status, account identifier, card number, and active status to the first row of the screen output; if the row is empty, skip the transfer.

REQ-F-529: [Event-driven] When the second card detail row in the buffer contains data, the system shall transfer the card selection status, account identifier, card number, and active status to the second row of the screen output; if the row is empty, skip the transfer.

REQ-F-530: [Event-driven] When the third card detail row in the buffer contains data, the system shall transfer the card selection status, account identifier, card number, and active status to the third row of the screen output; if the row is empty, skip the transfer.

REQ-F-531: [Event-driven] When the fourth card detail row in the buffer contains data, the system shall transfer the card selection status, account identifier, card number, and active status to the fourth row of the screen output; if the row is empty, skip the transfer.

REQ-F-532: [Event-driven] When the fifth card detail row in the buffer contains data, the system shall transfer the card selection status, account identifier, card number, and active status to the fifth row of the screen output; if the row is empty, skip the transfer.

REQ-F-533: [Event-driven] When the sixth card detail row in the buffer contains data, the system shall transfer the card selection status, account identifier, card number, and active status to the sixth row of the screen output; if the row is empty, skip the transfer.

REQ-F-534: [Event-driven] When the seventh card detail row in the buffer contains data, the system shall transfer the card selection status, account identifier, card number, and active status to the seventh row of the screen output; if the row is empty, skip the transfer.

REQ-F-535: [Event-driven] When row 1 of the card selection display is being prepared for output, the system shall apply protected-with-MDT-set attributes when the row is empty or protected; apply error highlighting and set cursor position when the row contains data, selection is not protected, and an error flag is set (displaying an asterisk marker when the selection is blank); otherwise apply standard MDT-set attributes.

REQ-F-536: [Event-driven] When row 2 of the card selection display is being prepared for output, the system shall apply protected attributes when the row is empty or protected; apply error highlighting and set cursor position when the row contains data, selection is not protected, and an error flag is set; otherwise apply standard MDT-set attributes.

REQ-F-537: [Event-driven] When row 3 of the card selection display is being prepared for output, the system shall apply protected attributes when the row is empty or protected; apply error highlighting and set cursor position when the row contains data, selection is not protected, and an error flag is set; otherwise apply standard MDT-set attributes.

REQ-F-538: [Event-driven] When row 4 of the card selection display is being prepared for output, the system shall apply protected attributes when the row is empty or protected; apply error highlighting and set cursor position when the row contains data, selection is not protected, and an error flag is set; otherwise apply standard MDT-set attributes.

REQ-F-539: [Event-driven] When row 5 of the card selection display is being prepared for output, the system shall apply protected attributes when the row is empty or protected; apply error highlighting and set cursor position when the row contains data, selection is not protected, and an error flag is set; otherwise apply standard MDT-set attributes.

REQ-F-540: [Event-driven] When row 6 of the card selection display is being prepared for output, the system shall apply protected attributes when the row is empty or protected; apply error highlighting and set cursor position when the row contains data, selection is not protected, and an error flag is set; otherwise apply standard MDT-set attributes.

REQ-F-541: [Event-driven] When row 7 of the card selection display is being prepared for output, the system shall apply protected attributes when the row is empty or protected; apply error highlighting and set cursor position when the row contains data, selection is not protected, and an error flag is set; otherwise apply standard MDT-set attributes.


---


## 35. Credit Card Inquiry Screen Navigation and Input Handling
As a user, I want to submit account and card number criteria on the card inquiry screen so that I can navigate to the main menu or proceed with card lookup.

### Requirements

REQ-F-542: [Event-driven] When the user presses a key on the terminal, the system shall evaluate the attention identifier and set the corresponding function key flag (ENTER, CLEAR, PA1, PA2, or PF1 through PF24) to TRUE.

REQ-F-543: [Event-driven] When a function key has been mapped from the user's keyboard input, the system shall check if the function key is one of the valid keys (ENTER, PF3, PF7, or PF8); if valid, mark the key as valid; if invalid, reset the key to ENTER.

REQ-F-544: [Event-driven] When the user submits the card inquiry screen and the originating program is the current program, the system shall receive the account identifier and card number from the screen input, validate that both fields are numeric and non-empty, and store valid values in the communication area.

REQ-F-545: [Event-driven] When the user submits the card inquiry screen, the system shall receive the card inquiry screen input, extract the account identifier and card number fields into working storage, and capture the response code.


---


## 36. Credit Card List Navigation and Detail Transfer
As a user, I want to navigate the card list and select a card for viewing or updating so that I can access card detail or update functions.

### Requirements

REQ-F-546: [Ubiquitous] The system shall validate the account identifier is numeric; validate the card number is either empty or numeric; when the account identifier is not numeric, set input validation to error; when the card number is not numeric, set input validation to error and clear the card number; when the card number is numeric, copy it to the communication area.

REQ-F-547: [Ubiquitous] The system shall translate the terminal attention identifier to a function key indicator by comparing against known key constants and setting the matching function key flag.

REQ-F-548: [Ubiquitous] The system shall check if the function key is valid (ENTER, PF3, PF7, or PF8); if invalid, set the function key flag to invalid and default the action to ENTER.

REQ-F-549: [Event-driven] When a communication area is present and the originating program is the current program, the system shall receive the screen input and validate the user's entries.

REQ-F-550: [Event-driven] When the program context indicates initial entry and the originating program is not the current program, the system shall clear the program communication area, mark the entry as initial, and reset the screen position to the first page.

REQ-F-551: [Ubiquitous] The system shall evaluate the input validation status and function key selections to determine the next action: reject invalid input, handle page-up requests when not on the first page, handle page-down requests when a next page exists, handle exit requests, or handle re-entry from a different program.

REQ-F-552: [Event-driven] When the user presses PF3 and the originating program is the current program, the system shall exit the transaction and return to the main menu.

REQ-F-553: [Ubiquitous] The system shall count the number of selected rows in the selection array; if more than one row is selected, set input validation to error; iterate through the selection array to identify the selected row index; when a row is selected (flag is 'S' or 'U'), store the row index; when a row is blank, take no action; when a row has an invalid flag value, set input validation to error.


### Open Questions

OQ-008: Rules 925ef823 and caf44a79 both describe behavior when PF7 is pressed on the first page — one states no action is taken, the other states the first page is retrieved and displayed. These appear contradictory. Which behavior is correct? — Owner: product owner / business analyst


---


## 37. Credit Card Update Delegation and Navigation
As an interactive user, I want to select a card from the list and be routed to the card update program so that I can update the selected card's details.

### Requirements

REQ-F-554: [Ubiquitous] The system shall translate the terminal attention identifier to a function key indicator by comparing against known key constants (ENTER, CLEAR, PA1, PA2, PF1 through PF12, and extended PF13 through PF24) and setting the matching function key flag.

REQ-F-555: [Event-driven] When the user presses the ENTER key, the system shall set the ENTER action flag to active.

REQ-F-556: [Event-driven] When the user presses the CLEAR key, the system shall set the CLEAR action flag to active.

REQ-F-557: [Event-driven] When the user presses a primary function key (PF1 through PF12), the system shall set the corresponding action flag (PFK01 through PFK12) to active.

REQ-F-558: [Event-driven] When the user presses an extended function key (PF13 through PF24), the system shall set the action flag corresponding to the primary function key equivalent (PFK01 through PFK12) to active, mapping PF13→PFK01 through PF24→PFK12.

REQ-F-559: [Ubiquitous] The system shall validate that the function key pressed is one of ENTER, PF3, PF7, or PF8; if the key is not one of these, the system shall set the function key flag to invalid and default the action to ENTER.

REQ-F-560: [Ubiquitous] The system shall receive card list screen input and extract the account identifier, card number, and seven card selection indicators into the work area.

REQ-F-561: [Event-driven] When the account identifier is received from the screen, the system shall validate that it is numeric; if not numeric, the system shall set the input validation error flag.

REQ-F-562: [Event-driven] When the card number is received from the screen, the system shall validate the card number: if empty (low-values, spaces, or numeric zero), the system shall clear the card number in the communication area; if not numeric, the system shall set the input validation error flag and clear the card number; if numeric, the system shall store the card number in the communication area.

REQ-F-563: [Ubiquitous] The system shall count the number of selected rows in the seven-row selection array; if more than one row is selected, the system shall set the input validation flag to error.

REQ-F-564: [Ubiquitous] The system shall iterate through the seven selection array elements to identify the selected row: if the selection flag is 'S' or 'U', the system shall store the row index; if the selection flag is blank or low-values, the system shall take no action; if the selection flag is any other value, the system shall set the input validation flag to error.

REQ-F-565: [Event-driven] When the program context indicates initial entry and the originating program is not the current program, the system shall clear the program communication area, mark the entry as initial, and reset the screen position to the first page.

REQ-F-566: [Event-driven] When a communication area is present and the originating program is the current program, the system shall receive the screen input and validate the user's entries.

REQ-F-567: [Ubiquitous] The system shall evaluate the input validation status and function key selections to determine the next action: reject invalid input, handle page-up requests when not on the first page, handle page-down requests when a next page exists, handle exit requests, or handle re-entry from a different program.

REQ-F-568: [Event-driven] When the user presses PF3 and the originating program is the current program, the system shall exit the transaction and return to the main menu.

REQ-F-569: [Event-driven] When the user presses PF7 (page up) and the current page is not the first page, the system shall retreat to the previous page of card records and display it.

REQ-F-570: [Event-driven] When the user presses PF7 (page up) while already on the first page, the system shall reject the page-up navigation request and display an error message.

REQ-F-571: [Event-driven] When the user presses PF8 (page down) and a next page of card records exists, the system shall advance to the next page of card records and display it.

REQ-F-572: [Event-driven] When the user presses PF3 (exit), the system shall exit the current transaction and return control to the calling program.

REQ-F-573: [Event-driven] When the program is re-entered from a different program, the system shall handle the cross-program re-entry and determine the appropriate next action.

REQ-F-574: [Event-driven] When user input or function key does not match any recognized condition, the system shall handle the unrecognized input and display an appropriate error or informational message.

REQ-F-575: [Event-driven] When the user presses ENTER with a card selected for view (marked 'S') and the originating program is the current program, the system shall display the detailed view of the selected card.

REQ-F-576: [Event-driven] When the communication area has been populated with the selected card's account number, card number, and navigation context, the system shall transfer control to the card update program with the populated communication area.


---


## 38. Card Inquiry and Display Screen
As an interactive user, I want to search for a card by account and card number and view its details so that I can review cardholder information.

### Requirements

REQ-F-577: [Event-driven] When the user presses the Enter key, the system shall set the Enter function indicator to active.

REQ-F-578: [Event-driven] When the user presses the Clear key, the system shall set the Clear function indicator to active.

REQ-F-579: [Event-driven] When the user presses the PA1 key, the system shall set the PA1 function indicator to active.

REQ-F-580: [Event-driven] When the user presses the PA2 key, the system shall set the PA2 function indicator to active.

REQ-F-581: [Event-driven] When the user presses PF1, the system shall set the PF1 function indicator to active.

REQ-F-582: [Event-driven] When the user presses PF2, the system shall set the PF2 function indicator to active.

REQ-F-583: [Event-driven] When the user presses PF3, the system shall set the PF3 function indicator to active.

REQ-F-584: [Event-driven] When the user presses PF4, the system shall set the PF4 function indicator to active.

REQ-F-585: [Event-driven] When the user presses PF5, the system shall set the PF5 function indicator to active.

REQ-F-586: [Event-driven] When the user presses PF6, the system shall set the PF6 function indicator to active.

REQ-F-587: [Event-driven] When the user presses PF7, the system shall set the PF7 function indicator to active.

REQ-F-588: [Event-driven] When the user presses PF8, the system shall set the PF8 function indicator to active.

REQ-F-589: [Event-driven] When the user presses PF9, the system shall set the PF9 function indicator to active.

REQ-F-590: [Event-driven] When the user presses PF10, the system shall set the PF10 function indicator to active.

REQ-F-591: [Event-driven] When the user presses PF11, the system shall set the PF11 function indicator to active.

REQ-F-592: [Event-driven] When the user presses PF12, the system shall set the PF12 function indicator to active.

REQ-F-593: [Event-driven] When the user presses the extended PF13 key, the system shall set the PF1 function indicator to active.

REQ-F-594: [Event-driven] When the user presses the extended PF14 key, the system shall set the PF2 function indicator to active.

REQ-F-595: [Event-driven] When the user presses the extended PF15 key, the system shall set the PF3 function indicator to active.

REQ-F-596: [Event-driven] When the user presses the extended PF16 key, the system shall set the PF4 function indicator to active.

REQ-F-597: [Event-driven] When the user presses the extended PF17 key, the system shall set the PF5 function indicator to active.

REQ-F-598: [Event-driven] When the user presses the extended PF18 key, the system shall set the PF6 function indicator to active.

REQ-F-599: [Event-driven] When the user presses the extended PF19 key, the system shall set the PF7 function indicator to active.

REQ-F-600: [Event-driven] When the user presses the extended PF20 key, the system shall set the PF8 function indicator to active.

REQ-F-601: [Event-driven] When the user presses the extended PF21 key, the system shall set the PF9 function indicator to active.

REQ-F-602: [Event-driven] When the user presses the extended PF22 key, the system shall set the PF10 function indicator to active.

REQ-F-603: [Event-driven] When the user presses the extended PF23 key, the system shall set the PF11 function indicator to active.

REQ-F-604: [Event-driven] When the user presses the extended PF24 key, the system shall set the PF12 function indicator to active.

REQ-F-605: [Ubiquitous] The system shall evaluate the terminal attention identifier and set the corresponding function key indicator flag, completing the evaluation after all possible key values have been tested.

REQ-F-606: [Event-driven] When a user presses a function key on the terminal, the system shall map the key to a standard identifier, validate that only Enter or PF3 are permitted, and force any invalid key to be treated as Enter.

REQ-F-607: [Event-driven] When the account number field is being validated and the account number is blank (low-values, spaces, or zero), the system shall set the input validation error flag and, if no error message is already set, display the message 'Account number not provided', and clear the account number in the communication area.

REQ-F-608: [Event-driven] When the account number field is being validated and the account number is not numeric, the system shall set the input validation error flag and, if no error message is already set, display the message 'ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER', and clear the account number in the communication area.

REQ-F-609: [Event-driven] When the account number field is being validated and the account number is numeric, the system shall copy it to the communication area.

REQ-F-610: [Event-driven] When the card number field is being validated and the card number is blank (low-values, spaces, or zero), the system shall set the input validation error flag and, if no error message is already set, display the message 'Card number not provided', and clear the card number in the communication area.

REQ-F-611: [Event-driven] When the card number field is being validated and the card number is not numeric, the system shall set the input validation error flag and, if no error message is already set, display the message 'CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER', and clear the card number in the communication area.

REQ-F-612: [Event-driven] When the card number field is being validated and the card number is numeric, the system shall copy it to the communication area.

REQ-F-613: [Event-driven] When the program requests a card record by card number, the system shall read the card record from the card data store (CARDDAT) using the card number (16-character alphanumeric) as the key; on success, the system shall mark cards as found; on not-found, the system shall mark both the account and card fields as invalid and, if no error message is already set, display 'Did not find cards for this search condition'; on any other error, the system shall mark the account field as invalid, log the operation name, file name, response code, and reason code, and display the error message.

REQ-F-614: [Event-driven] When the screen is being prepared for display, the system shall populate search criteria (account and card numbers) from the work area, display card details (embossed name, expiration month and year extracted from the expiration date, and active status) when a card record was found, and display information and error messages based on the current transaction state.

REQ-F-615: [Complex] While the program is active and awaiting user action, when the user presses Enter, PF3, or another key, or the program re-enters after processing, the system shall dispatch to: exit on PF3; retrieve and display the card record when entering from the card list with pre-populated account and card numbers; display a blank search screen on first entry from another context; validate input and retrieve and display the card record on successful re-entry; redisplay the screen with error messages on validation failure; or send an error message on an unexpected data scenario.


---


## 39. Credit Card Transaction Routing and Function Key Handling
As an interactive user, I want my key presses to be validated and routed to the correct destination so that only permitted actions are executed and I can exit to the appropriate program.

### Requirements

REQ-F-616: [Event-driven] When a keyboard input is received, the system shall assume the input is invalid; if the user pressed ENTER or PF3, the system shall mark the input as valid; if the input remains invalid, the system shall override it to ENTER to ensure a valid action proceeds.

REQ-F-617: [Event-driven] When a user presses a keyboard key, the system shall evaluate the attention identifier against known key codes (ENTER, CLEAR, PA1, PA2, PF1–PF24) and set the corresponding action flag; PF13 through PF24 shall be mapped to PF1 through PF12 respectively; if the attention identifier does not match any known key code, no action flag shall be set.

REQ-F-618: [Event-driven] When the user presses PF3 (exit), the system shall determine the destination transaction and program from the communication area (using the originating transaction identifier and program name if present, otherwise defaulting to the menu transaction identifier and menu program name), record the current program and transaction as the origin, set the user type to regular user, set the program context to initial entry, record the current mapset and map as last accessed, and transfer control to the destination program with the updated communication area.


---


## 40. Card Update Program Navigation and Control Transfer
As an interactive user, I want my key presses mapped to named actions and the system to route me to the correct program so that card update operations are handled appropriately.

### Requirements

REQ-F-619: [Ubiquitous] The system shall translate the terminal attention identifier into a named function-key indicator by matching against key-code constants.

REQ-F-620: [Event-driven] When the user presses the ENTER key, the system shall set the ENTER indicator to TRUE.

REQ-F-621: [Event-driven] When the user presses the CLEAR key, the system shall set the CLEAR indicator to TRUE.

REQ-F-622: [Event-driven] When the user presses the PA1 key, the system shall set the PA1 indicator to TRUE.

REQ-F-623: [Event-driven] When the user presses the PA2 key, the system shall set the PA2 indicator to TRUE.

REQ-F-624: [Event-driven] When the user presses function key 1, the system shall set the function key 1 indicator to TRUE.

REQ-F-625: [Event-driven] When the user presses function key 2, the system shall set the function key 2 indicator to TRUE.

REQ-F-626: [Event-driven] When the user presses function key 3, the system shall set the function key 3 indicator to TRUE.

REQ-F-627: [Event-driven] When the user presses function key 4, the system shall set the function key 4 indicator to TRUE.

REQ-F-628: [Event-driven] When the user presses function key 5, the system shall set the function key 5 indicator to TRUE.

REQ-F-629: [Event-driven] When the user presses function key 6, the system shall set the function key 6 indicator to TRUE.

REQ-F-630: [Event-driven] When the user presses function key 7, the system shall set the function key 7 indicator to TRUE.

REQ-F-631: [Event-driven] When the user presses function key 8, the system shall set the function key 8 indicator to TRUE.

REQ-F-632: [Event-driven] When the user presses function key 9, the system shall set the function key 9 indicator to TRUE.

REQ-F-633: [Event-driven] When the user presses function key 10, the system shall set the function key 10 indicator to TRUE.

REQ-F-634: [Event-driven] When the user presses function key 11, the system shall set the function key 11 indicator to TRUE.

REQ-F-635: [Event-driven] When the user presses function key 12, the system shall set the function key 12 indicator to TRUE.

REQ-F-636: [Event-driven] When the user presses function key 13, the system shall set the function key 1 indicator to TRUE.

REQ-F-637: [Event-driven] When the user presses function key 14, the system shall set the function key 2 indicator to TRUE.

REQ-F-638: [Event-driven] When the user presses function key 15, the system shall set the function key 3 indicator to TRUE.

REQ-F-639: [Event-driven] When the user presses function key 16, the system shall set the function key 4 indicator to TRUE.

REQ-F-640: [Event-driven] When the user presses function key 17, the system shall set the function key 5 indicator to TRUE.

REQ-F-641: [Event-driven] When the user presses function key 18, the system shall set the function key 6 indicator to TRUE.

REQ-F-642: [Event-driven] When the user presses function key 19, the system shall set the function key 7 indicator to TRUE.

REQ-F-643: [Event-driven] When the user presses function key 20, the system shall set the function key 8 indicator to TRUE.

REQ-F-644: [Event-driven] When the user presses function key 21, the system shall set the function key 9 indicator to TRUE.

REQ-F-645: [Event-driven] When the user presses function key 22, the system shall set the function key 10 indicator to TRUE.

REQ-F-646: [Event-driven] When the user presses function key 23, the system shall set the function key 11 indicator to TRUE.

REQ-F-647: [Event-driven] When the user presses function key 24, the system shall set the function key 12 indicator to TRUE.

REQ-F-648: [Ubiquitous] The system shall complete the key mapping evaluation after all terminal attention identifiers have been tested and mapped to their corresponding function-key indicators.

REQ-F-649: [Event-driven] When a function key is pressed, the system shall validate the key against the current program state; the key is valid if the user pressed ENTER, PF3 (exit), PF5 (confirm changes) when changes are pending confirmation, or PF12 (submit) when card details have already been fetched; if the key is invalid, the system shall override it to ENTER.

REQ-F-650: [Event-driven] When the user presses PF3, or when changes have been confirmed and the last accessed mapset was the credit card list, or when changes have failed and the last accessed mapset was the credit card list, the system shall determine the destination program and transaction from the originating context (defaulting to the menu transaction identifier and menu program name if the originating values are empty or spaces), record the current program and transaction as the source, clear account and card numbers if the last mapset was the credit card list, set the user type to regular user, set the program context to initial entry, record the current mapset and map names, and transfer control to the destination program with the updated communication area.


---


## 41. Credit Card Update Transaction Processing
As an authenticated user, I want to view and update credit card details through a validated screen workflow so that card information is accurately maintained and concurrent changes are safely detected.


**Function Key Mapping**

### Requirements

REQ-F-651: [Event-driven] When the user presses function key 1, the system shall set the function key 1 indicator to TRUE.

REQ-F-652: [Event-driven] When the user presses function key 2, the system shall set the function key 2 indicator to TRUE.

REQ-F-653: [Event-driven] When the user presses function key 3, the system shall set the function key 3 indicator to TRUE.

REQ-F-654: [Event-driven] When the user presses function key 4, the system shall set the function key 4 indicator to TRUE.

REQ-F-655: [Event-driven] When the user presses function key 5, the system shall set the function key 5 indicator to TRUE.

REQ-F-656: [Event-driven] When the user presses function key 6, the system shall set the function key 6 indicator to TRUE.

REQ-F-657: [Event-driven] When the user presses function key 7, the system shall set the function key 7 indicator to TRUE.

REQ-F-658: [Event-driven] When the user presses function key 8, the system shall set the function key 8 indicator to TRUE.

REQ-F-659: [Event-driven] When the user presses function key 9, the system shall set the function key 9 indicator to TRUE.

REQ-F-660: [Event-driven] When the user presses function key 10, the system shall set the function key 10 indicator to TRUE.

REQ-F-661: [Event-driven] When the user presses function key 11, the system shall set the function key 11 indicator to TRUE.

REQ-F-662: [Event-driven] When the user presses function key 12, the system shall set the function key 12 indicator to TRUE.

REQ-F-663: [Event-driven] When the user presses function key 13, the system shall set the function key 1 indicator to TRUE, mapping extended key 13 to primary key 1.

REQ-F-664: [Event-driven] When the user presses function key 14, the system shall set the function key 2 indicator to TRUE, mapping extended key 14 to primary key 2.

REQ-F-665: [Event-driven] When the user presses function key 15, the system shall set the function key 3 indicator to TRUE, mapping extended key 15 to primary key 3.

REQ-F-666: [Event-driven] When the user presses function key 16, the system shall set the function key 4 indicator to TRUE, mapping extended key 16 to primary key 4.

REQ-F-667: [Event-driven] When the user presses function key 17, the system shall set the function key 5 indicator to TRUE, mapping extended key 17 to primary key 5.

REQ-F-668: [Event-driven] When the user presses function key 18, the system shall set the function key 6 indicator to TRUE, mapping extended key 18 to primary key 6.

REQ-F-669: [Event-driven] When the user presses function key 19, the system shall set the function key 7 indicator to TRUE, mapping extended key 19 to primary key 7.

REQ-F-670: [Event-driven] When the user presses function key 20, the system shall set the function key 8 indicator to TRUE, mapping extended key 20 to primary key 8.

REQ-F-671: [Event-driven] When the user presses function key 21, the system shall set the function key 9 indicator to TRUE, mapping extended key 21 to primary key 9.

REQ-F-672: [Event-driven] When the user presses function key 22, the system shall set the function key 10 indicator to TRUE, mapping extended key 22 to primary key 10.

REQ-F-673: [Event-driven] When the user presses function key 23, the system shall set the function key 11 indicator to TRUE, mapping extended key 23 to primary key 11.

REQ-F-674: [Event-driven] When the user presses function key 24, the system shall set the function key 12 indicator to TRUE, mapping extended key 24 to primary key 12.

REQ-F-675: [Event-driven] When the user presses the ENTER key, the system shall set the action flag to ENTER to signal form submission.

REQ-F-676: [Event-driven] When the user presses the CLEAR key, the system shall set the CLEAR indicator to TRUE.

REQ-F-677: [Event-driven] When the user presses the PA1 key, the system shall set the PA1 indicator to TRUE.

REQ-F-678: [Event-driven] When the user presses the PA2 key, the system shall set the PA2 indicator to TRUE.

REQ-F-679: [Ubiquitous] The system shall complete the key mapping evaluation after all attention identifiers have been tested and mapped to their corresponding function key indicators.

REQ-F-680: [Event-driven] When a function key is pressed, the system shall validate the key against the current transaction state; valid keys are ENTER, F3 (exit), F5 (confirm changes when changes are pending), and F12 (cancel changes when details are already fetched); if the pressed key is not valid for the current state, the system shall override it to ENTER to redisplay the current screen.

REQ-F-681: [Complex] While the program is active and awaiting user input, when a function key is pressed, the system shall route to: exit when F3 is pressed; reset and redisplay the search screen when changes have been confirmed and saved or when changes have failed; retrieve and display card details when first entered from the card list or when F12 is pressed to cancel changes; display the search screen when first entered from the menu or when details have not yet been fetched; or process and validate user input and redisplay the screen for all other cases.

REQ-F-682: [Event-driven] When the transaction state is changes-okayed-and-done, the system shall set the transaction state to show-details to allow the user to review the updated card details.

REQ-F-683: [Event-driven] When the transaction state is changes-ok-not-confirmed and the user does not press F5, the system shall take no action and remain in the current state.

REQ-F-684: [Event-driven] When the transaction state is changes-not-ok, the system shall take no action and remain in the current state.

REQ-F-685: [Unwanted] If the transaction state is not one of the expected values (details-not-fetched, show-details, changes-not-ok, changes-ok-not-confirmed, or changes-okayed-and-done), the system shall record the abnormal termination with code '0001' and message 'UNEXPECTED DATA SCENARIO' and terminate the program.

REQ-F-686: [Ubiquitous] The system shall receive the card update screen input from the terminal into the input buffer, capturing the response code and reason code.

REQ-F-687: [Event-driven] When the account identifier field contains an asterisk or is blank, the system shall clear the account identifier to low-values; otherwise, the system shall copy the user's account identifier input to the internal account identifier fields.

REQ-F-688: [Event-driven] When the card number field contains an asterisk or is blank, the system shall clear the card number to low-values; otherwise, the system shall copy the user's card number input to the internal card number fields.

REQ-F-689: [Event-driven] When the card name field contains an asterisk or is blank, the system shall clear the new card name to low-values; otherwise, the system shall copy the user's card name input to the new card name field.

REQ-F-690: [Event-driven] When the card status code field contains an asterisk or is blank, the system shall clear the new card status code to low-values; otherwise, the system shall copy the user's card status code input to the new card status code field.

REQ-F-691: [Event-driven] When the expiration month field contains an asterisk or is blank, the system shall clear the new expiration month to low-values; otherwise, the system shall copy the user's expiration month input to the new expiration month field.

REQ-F-692: [Event-driven] When the expiration year field contains an asterisk or is blank, the system shall clear the new expiration year to low-values; otherwise, the system shall copy the user's expiration year input to the new expiration year field.

REQ-F-693: [Ubiquitous] The system shall unconditionally copy the expiration day from the screen input to the new expiration day field.

REQ-F-694: [Event-driven] When the account identifier field is validated, the system shall: if blank or zero, set an input error flag and display a prompt asking for the account number; if non-numeric, set an input error flag and display an error message stating that the account filter must be an 11-digit number; if numeric, mark the field as valid.

REQ-F-695: [Event-driven] When the card identifier field is validated, the system shall: if blank or zero, set an input error flag, display a prompt asking for the card number, and clear the card identifier; if non-numeric, set an input error flag, display an error message stating that the card ID filter must be a 16-digit number, and clear the card identifier; if numeric, store it and mark it as valid.

REQ-F-696: [Event-driven] When the card name field is validated, the system shall: if blank, set an input error flag and display a prompt asking for the card name; if it contains non-alphabetic characters other than spaces, set an input error flag and display an error message stating that the card name can only contain alphabets and spaces; if valid, mark it as valid.

REQ-F-697: [Event-driven] When the card status code field is validated, the system shall: if blank, set an input error flag and display the message 'Card Active Status must be Y or N'; if not 'Y' or 'N', set an input error flag and display the message 'Card Active Status must be Y or N'; if 'Y' or 'N', mark it as valid.

REQ-F-698: [Event-driven] When the expiration month field is validated, the system shall: if blank or zero, set an input error flag and display the message 'Card expiry month must be between 1 and 12'; if not between 1 and 12, set an input error flag and display the message 'Card expiry month must be between 1 and 12'; if between 1 and 12, mark it as valid.

REQ-F-699: [Event-driven] When the expiration year field is validated, the system shall: if blank or zero, set an input error flag and display the message 'Invalid card expiry year'; if not between 1950 and 2099, set an input error flag and display the message 'Invalid card expiry year'; if between 1950 and 2099, mark it as valid.

REQ-F-700: [Event-driven] When the user enters search criteria for the first time (card details not yet fetched), the system shall validate the account identifier and card number; if both are blank, the system shall set the information message to 'No input received'; otherwise, the system shall clear any previously stored card data and prepare for the lookup.

REQ-F-701: [Event-driven] When the user is editing card details that have already been fetched, the system shall compare the new card data (name, status, expiration date) against the old card data using case-insensitive comparison; if identical, the system shall set the information message to 'No change detected with respect to values fetched' and mark all card field validations as passed.

REQ-F-702: [Event-driven] When the program has completed the change-detection phase and is ready to validate individual card fields, the system shall validate the card name, card status code, expiration month, and expiration year in sequence; if any validation fails, the system shall stop processing; if all validations pass, the system shall mark the change action as 'changes made but not yet confirmed'.

REQ-F-703: [Event-driven] When card details are displayed and the user has entered input, the system shall validate the input and detect changes; if input is valid and changes are detected, the system shall set the transaction state to changes-ok-not-confirmed; otherwise, the system shall leave the state unchanged.

REQ-F-704: [Event-driven] When the user presses F12 with both the account identifier and card identifier validation flags set to valid, the system shall retrieve the card record from the card data store (AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS); if found, the system shall set the transaction state to show-details.

REQ-F-705: [Unwanted] If the card record read operation returns a not-found response, the system shall set error flags for both account and card filters and display the message 'Did not find cards for this search condition'.

REQ-F-706: [Unwanted] If the card record read operation returns a technical error response, the system shall set an error flag for the account filter, log the operation name, file name, response code, and reason code to the return message, and display the error message to the user.

REQ-F-707: [Event-driven] When the user confirms changes and presses F5 to save, the system shall read the card record from the card data store (AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS) with an update lock; if the lock fails, the system shall set an error flag with the message 'Could not lock record for update'.

REQ-F-708: [Event-driven] When a card record is locked for update, the system shall compare the current record values (card verification value code, cardholder name, expiration year, expiration month, expiration day, and card active status) against the old values displayed to the user; if all match, the system shall allow the update to proceed; if any differ, the system shall set a concurrent-change flag and update the old-details area with the current values from the locked record for user review.

REQ-F-709: [Event-driven] When no concurrent changes are detected in the locked record, the system shall assemble the updated card record from the new user-entered values (card number [16 alphanumeric], account identifier [11 numeric], CVV code [3 numeric], cardholder name [50 alphanumeric], expiration date [10 alphanumeric, formatted as YYYY-MM-DD], active status [1 alphanumeric]) and write the record to the card data store (AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS); if the write fails, the system shall set an error flag with the message 'Update of record failed'.

REQ-F-710: [Ubiquitous] The system shall clear the screen output buffer, populate the fixed header fields (titles, transaction identifier, and program name), retrieve the current date and time, format the date as MM/DD/YY and the time as HH:MM:SS, and move these formatted values to the screen output buffer.

REQ-F-711: [Event-driven] When re-entering the screen after prior processing, the system shall: if the account identifier is zero, clear the account identifier output field; otherwise, display the stored account identifier.

REQ-F-712: [Event-driven] When re-entering the screen after prior processing, the system shall: if the card identifier is zero, clear the card identifier output field; otherwise, display the stored card identifier.

REQ-F-713: [Event-driven] When the transaction state indicates that card details should be shown, the system shall populate the screen with the previously stored card name, card status code, expiration day, expiration month, and expiration year from the old-details area.

REQ-F-714: [Event-driven] When the transaction state indicates that card details have not yet been fetched, the system shall clear all card detail fields (card name, card status code, expiration day, expiration month, and expiration year) to low-values on the screen.

REQ-F-715: [Event-driven] When the change-action flag indicates that changes have been made to the card data, the system shall populate the screen with the new card name, new card status code, new expiration month, and new expiration year from the new-details area, and populate the expiration day from the old expiration day.

REQ-F-716: [Unwanted] If the change-action flag contains an unrecognized value, the system shall default to displaying the previously stored card name, card status code, expiration day, expiration month, and expiration year from the old-details area.

REQ-F-717: [State-driven] While the screen display buffer is being prepared, the system shall set the information message based on the current transaction state: prompt for account and card number when details are not yet fetched; indicate that details of the selected card are shown when in show-details state; prompt to update card details when changes have been made but not confirmed; prompt to press F5 to save when changes are validated but not yet confirmed; indicate that changes have been committed when changes are confirmed and saved; indicate that changes were unsuccessful when a lock error or update error has occurred.

REQ-F-718: [Event-driven] When the user returns from the credit card list, the system shall reset the account identifier and card identifier field colors to the default color.

REQ-F-719: [Event-driven] When account identifier validation fails or the field is blank, the system shall position the cursor to the account identifier field.

REQ-F-720: [Event-driven] When card identifier validation fails or the field is blank, the system shall position the cursor to the card identifier field.

REQ-F-721: [Event-driven] When card name validation fails or the field is blank, the system shall position the cursor to the card name field.

REQ-F-722: [Event-driven] When card status code validation fails or the field is blank, the system shall position the cursor to the card status code field.

REQ-F-723: [Event-driven] When expiration month validation fails or the field is blank, the system shall position the cursor to the expiration month field.

REQ-F-724: [Event-driven] When expiration year validation fails or the field is blank, the system shall position the cursor to the expiration year field.

REQ-F-725: [Event-driven] When cards are found for the account or no changes are detected, the system shall position the cursor to the card name field.

REQ-F-726: [Event-driven] When the cursor positioning scenario is not explicitly handled, the system shall position the cursor to the account identifier field as the default.


### Open Questions

OQ-009: Rule `b7f1f210_91f8_4fb2_8a42_d94b80220123` specifies abnormal termination code '0001' with message 'UNEXPECTED DATA SCENARIO'. Should the modernized system preserve this exact error code and message for operational monitoring, or should it be mapped to a platform-neutral error classification? — Owner: operations/modernization team

OQ-010: Rule `3cb585de_b7ed_420a_85f7_a443c3593be4` specifies that concurrent change detection compares the card verification value code, cardholder name, expiration date components, and card active status. It is not stated whether the card number itself is included in the comparison. — Owner: business/data team


---


## 42. Card Filter Validation
As a card list user, I want account and card number filter inputs validated before the list is retrieved so that only well-formed filter criteria are applied to the search.

### Requirements

REQ-F-727: [Event-driven] When the account ID filter is submitted and the account ID is blank or zero, the system shall mark the account filter as blank and clear the account ID in the communication area.

REQ-F-728: [Event-driven] When the account ID filter is submitted and the account ID is not numeric, the system shall mark the account filter as invalid, protect the selection rows, set the error message to 'ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER' if no prior error has been set, and clear the account ID in the communication area.

REQ-F-729: [Event-driven] When the account ID filter is submitted and the account ID is numeric, the system shall mark the account filter as valid and move the account ID to the communication area.

REQ-F-730: [Event-driven] When the card number filter is submitted and the card number is blank or zero, the system shall mark the card filter as blank and clear the card number in the communication area.

REQ-F-731: [Event-driven] When the card number filter is submitted and the card number is not numeric, the system shall mark the card filter as invalid, protect the selection rows, set the error message to 'CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER' if no prior error has been set, and clear the card number in the communication area.

REQ-F-732: [Event-driven] When the card number filter is submitted and the card number is numeric, the system shall mark the card filter as valid and move the card number to the communication area.

REQ-F-733: [Event-driven] When the account filter validation status indicates invalid input, the system shall highlight the account ID field in red and position the cursor at the account ID input field.

REQ-F-734: [Event-driven] When the card filter validation status indicates invalid input, the system shall highlight the card ID field in red and position the cursor at the card ID input field.


---


## 43. Card List Program Initialization and Re-entry
As a card list user, I want the program to correctly initialize its state on first entry and restore or reset state on re-entry so that navigation context is consistent across program transitions.

### Requirements

REQ-F-735: [Ubiquitous] The system shall initialize working storage, communication areas, and pagination state on program entry, and set the program context to entry mode when first invoked or when re-entering from a different program.

REQ-F-736: [Event-driven] When PF3 is pressed or the program is re-entered from a different program, the system shall reinitialize communication areas, set the originating program to the current program, mark the program context as entry, set the screen to the first page, reset the last-page indicator, retrieve the first page of cards, and display the card list screen.

REQ-F-737: [Ubiquitous] The system shall initialize the credit card work area, communication context, and program state; when no prior context exists, the system shall set the originating program to the current program and mark the entry as initial; when prior context exists, the system shall restore the communication area and program communication area from the passed buffer.

REQ-F-738: [Ubiquitous] The system shall initialize working storage and communication context; when no communication area is passed, the system shall set the originating program to the current program; when a communication area is passed, the system shall extract it into the local structure.


---


## 44. Card File Browse and Screen Staging
As a card list user, I want card records retrieved from the card data store and staged for paginated display so that the correct records appear on each page.

### Requirements

REQ-F-739: [Ubiquitous] The system shall clear the screen data buffer, initiate a forward browse of the card data store (LIT-CARD-FILE) positioned at the specified card number, initialize the screen row counter to zero, and set the next-page and read-loop flags to indicate records are available.

REQ-F-740: [Ubiquitous] The system shall initialize the screen buffer to empty, set the screen row counter to 8 (one more than the maximum displayable rows), mark the next page as existing, and set the read-loop flag to continue when initializing a backward browse of the card data store (LIT-CARD-FILE).

REQ-F-741: [Event-driven] When a card record is successfully retrieved and passes the filter criteria, the system shall increment the screen row counter, populate the current row with the card's number, account identifier, and status, and initialize the screen page number to 1 if this is the first record on the page.


---


## 45. Card List Screen Header Preparation
As a card list user, I want the screen header populated with current context information so that I can identify the transaction, program, date, time, and page being viewed.

### Requirements

REQ-F-742: [Ubiquitous] The system shall populate the screen title lines, transaction ID, program name, current date, current time, and page number, and clear the information message area when preparing the card list screen for display.


---


## 46. Function Key Mapping
As an interactive user, I want my keyboard input (function keys and control keys) mapped to recognized action indicators so that the system can route my request to the correct operation.

### Requirements

REQ-F-743: [Ubiquitous] The system shall evaluate the terminal attention identifier and set the corresponding function key indicator flag for all standard and extended key inputs.

REQ-F-744: [Event-driven] When the user presses the Enter key, the system shall set the Enter function indicator to active.

REQ-F-745: [Event-driven] When the user presses the Clear key, the system shall set the Clear function indicator to active.

REQ-F-746: [Event-driven] When the user presses the PA1 key, the system shall set the PA1 function indicator to active.

REQ-F-747: [Event-driven] When the user presses the PA2 key, the system shall set the PA2 function indicator to active.

REQ-F-748: [Event-driven] When the user presses the PF1 key, the system shall set the PF1 function indicator to active.

REQ-F-749: [Event-driven] When the user presses the PF2 key, the system shall set the PF2 function indicator to active.

REQ-F-750: [Event-driven] When the user presses the PF3 key, the system shall set the PF3 function indicator to active.

REQ-F-751: [Event-driven] When the user presses the PF4 key, the system shall set the PF4 function indicator to active.

REQ-F-752: [Event-driven] When the user presses the PF5 key, the system shall set the PF5 function indicator to active.

REQ-F-753: [Event-driven] When the user presses the PF6 key, the system shall set the PF6 function indicator to active.

REQ-F-754: [Event-driven] When the user presses the PF7 key, the system shall set the PF7 function indicator to active.

REQ-F-755: [Event-driven] When the user presses the PF8 key, the system shall set the PF8 function indicator to active.

REQ-F-756: [Event-driven] When the user presses the PF9 key, the system shall set the PF9 function indicator to active.

REQ-F-757: [Event-driven] When the user presses the PF10 key, the system shall set the PF10 function indicator to active.

REQ-F-758: [Event-driven] When the user presses the PF11 key, the system shall set the PF11 function indicator to active.

REQ-F-759: [Event-driven] When the user presses the PF12 key, the system shall set the PF12 function indicator to active.

REQ-F-760: [Event-driven] When the user presses the extended PF13 key, the system shall remap it and set the PF1 function indicator to active.

REQ-F-761: [Event-driven] When the user presses the extended PF14 key, the system shall remap it and set the PF2 function indicator to active.

REQ-F-762: [Event-driven] When the user presses the extended PF15 key, the system shall remap it and set the PF3 function indicator to active.

REQ-F-763: [Event-driven] When the user presses the extended PF16 key, the system shall remap it and set the PF4 function indicator to active.

REQ-F-764: [Event-driven] When the user presses the extended PF17 key, the system shall remap it and set the PF5 function indicator to active.

REQ-F-765: [Event-driven] When the user presses the extended PF18 key, the system shall remap it and set the PF6 function indicator to active.

REQ-F-766: [Event-driven] When the user presses the extended PF19 key, the system shall remap it and set the PF7 function indicator to active.

REQ-F-767: [Event-driven] When the user presses the extended PF20 key, the system shall remap it and set the PF8 function indicator to active.

REQ-F-768: [Event-driven] When the user presses the extended PF21 key, the system shall remap it and set the PF9 function indicator to active.

REQ-F-769: [Event-driven] When the user presses the extended PF22 key, the system shall remap it and set the PF10 function indicator to active.

REQ-F-770: [Event-driven] When the user presses the extended PF23 key, the system shall remap it and set the PF11 function indicator to active.

REQ-F-771: [Event-driven] When the user presses the extended PF24 key, the system shall remap it and set the PF12 function indicator to active.


---


## 47. Input Validation and Action Routing
As an interactive user, I want my key presses validated and routed to the correct action so that only permitted operations proceed and invalid inputs are handled gracefully.

### Requirements

REQ-F-772: [Event-driven] When keyboard input is received, the system shall assume the input is invalid; if the user pressed Enter or PF3, the system shall mark the input as valid; if the input remains invalid, the system shall override it to Enter so that a valid action proceeds.

REQ-F-773: [Event-driven] When the user presses PF3 (exit), the system shall determine the destination transaction and program from the session context (using the originating transaction identifier and program name if present, otherwise defaulting to the menu transaction identifier and menu program name), update the session context to record the current program and transaction as the origin, set the user type to regular user, set the program context to initial entry, record the current mapset and map as the last accessed, and transfer control to the destination program with the updated session context.


---


## 48. Card Inquiry Transaction Routing
As an interactive user, I want the card inquiry screen to route my actions correctly based on entry context and key pressed so that I can search for and view card details or exit to the appropriate destination.

### Requirements

REQ-F-774: [Complex] While the card inquiry is active and awaiting user action, when the user presses Enter after first entry from the card list program with pre-populated account and card numbers, the system shall retrieve the card record from the card data store (AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS) by card number and display the card details.

REQ-F-775: [Complex] While the card inquiry is active and awaiting user action, when the program is entered for the first time from a context other than the card list, the system shall display a blank search screen prompting the user to enter account and card numbers.

REQ-F-776: [Complex] While the card inquiry is active and the user has re-entered after submitting input, when validation succeeds, the system shall retrieve the card record from the card data store by card number and display the card details.

REQ-F-777: [Complex] While the card inquiry is active and the user has re-entered after submitting input, when validation fails, the system shall redisplay the screen with error messages.

REQ-F-778: [Event-driven] When the account number field is validated and the value is blank or zero, the system shall set an input error flag, clear the account number from the session context, and set the error message 'Account number not provided' if no error message is already set.

REQ-F-779: [Event-driven] When the account number field is validated and the value is non-numeric, the system shall set an input error flag, clear the account number from the session context, and set the error message 'ACCOUNT FILTER,IF SUPPLIED MUST BE A 11 DIGIT NUMBER' if no error message is already set.

REQ-F-780: [Event-driven] When the account number field is validated and the value is numeric, the system shall copy the account number to the session context and mark the field as valid.

REQ-F-781: [Event-driven] When the card number field is validated and the value is blank or zero, the system shall set an input error flag, clear the card number from the session context, and set the error message 'Card number not provided' if no error message is already set.

REQ-F-782: [Event-driven] When the card number field is validated and the value is non-numeric, the system shall set an input error flag, clear the card number from the session context, and set the error message 'CARD ID FILTER,IF SUPPLIED MUST BE A 16 DIGIT NUMBER' if no error message is already set.

REQ-F-783: [Event-driven] When the card number field is validated and the value is numeric, the system shall copy the card number (16-character alphanumeric) to the session context and mark the field as valid.

REQ-F-784: [Event-driven] When the system requests a card record by card number and the read from the card data store succeeds, the system shall mark cards as found for the account.

REQ-F-785: [Event-driven] When the system requests a card record by card number and the card is not found in the card data store, the system shall mark both the account and card fields as invalid and set the error message 'Did not find cards for this search condition' if no error message is already set.

REQ-F-786: [Unwanted] If the read from the card data store fails with an error other than not-found, the system shall mark the account field as invalid, log the operation name, file name, response code, and reason code to the error message field, and display the error message if no error message is already set.


---


## 49. Card Inquiry Screen Content
As an interactive user, I want the card inquiry screen populated with current search criteria and card details so that I can review cardholder information and understand any validation errors.

### Requirements

REQ-F-787: [Event-driven] When the screen is being prepared for display and no session context is present, the system shall prompt the user to enter account and card numbers.

REQ-F-788: [Event-driven] When the screen is being prepared for display and a session context is present, the system shall display the account number and card number from the session context (or blank values if they are zero).

REQ-F-789: [Event-driven] When a card record has been found, the system shall display the cardholder embossed name, expiration month and year (extracted from the card expiration date field), and card active status on the screen.

REQ-F-790: [Event-driven] When the screen is being prepared for display, the system shall display the current date formatted as MM/DD/YY and the current time formatted as HH:MM:SS in the screen header.

REQ-F-791: [Event-driven] When the screen is being prepared for display, the system shall display any information message and any error message from validation or lookup results.


### Open Questions

OQ-011: Rule 61f71f18 describes protecting input fields when returning from the card list and unprotecting them for a new search. The business consequence of field protection (preventing user modification of pre-populated data) may be a functional requirement. Should the modernized system enforce read-only account and card number fields when the entry context is the card list program? — Owner: product/UX team

OQ-012: Rule ceda5c13 describes marking blank required fields with an asterisk. Is the asterisk marker a business convention (indicating a required field to the user) that must be preserved in the modernized system, or is it a purely presentational legacy artifact? — Owner: product/UX team

OQ-013: Rule 850d12e2 describes initializing a fresh session when no communication area is passed or when the caller is the menu program on initial entry, versus copying the passed communication area otherwise. The boundary condition for "initial entry from menu" may affect downstream routing logic. Should this distinction be explicitly modeled as a session-state requirement? — Owner: architecture team


---


## 50. Function Key Mapping
As an interactive user, I want my key presses recognized and mapped to named actions so that the system can route my input to the correct business operation.

### Requirements

REQ-F-792: [Ubiquitous] The system shall translate the terminal attention identifier into a named function-key indicator by matching it against the set of recognized key codes (ENTER, CLEAR, PA1, PA2, PF1 through PF24).

REQ-F-793: [Event-driven] When the user presses the ENTER key, the system shall set the ENTER function-key indicator to TRUE.

REQ-F-794: [Event-driven] When the user presses the CLEAR key, the system shall set the CLEAR function-key indicator to TRUE.

REQ-F-795: [Event-driven] When the user presses the PA1 key, the system shall set the PA1 function-key indicator to TRUE.

REQ-F-796: [Event-driven] When the user presses the PA2 key, the system shall set the PA2 function-key indicator to TRUE.

REQ-F-797: [Event-driven] When the user presses function key 1, the system shall set the function key 1 indicator to TRUE.

REQ-F-798: [Event-driven] When the user presses function key 2, the system shall set the function key 2 indicator to TRUE.

REQ-F-799: [Event-driven] When the user presses function key 3, the system shall set the function key 3 indicator to TRUE.

REQ-F-800: [Event-driven] When the user presses function key 4, the system shall set the function key 4 indicator to TRUE.

REQ-F-801: [Event-driven] When the user presses function key 5, the system shall set the function key 5 indicator to TRUE.

REQ-F-802: [Event-driven] When the user presses function key 6, the system shall set the function key 6 indicator to TRUE.

REQ-F-803: [Event-driven] When the user presses function key 7, the system shall set the function key 7 indicator to TRUE.

REQ-F-804: [Event-driven] When the user presses function key 8, the system shall set the function key 8 indicator to TRUE.

REQ-F-805: [Event-driven] When the user presses function key 9, the system shall set the function key 9 indicator to TRUE.

REQ-F-806: [Event-driven] When the user presses function key 10, the system shall set the function key 10 indicator to TRUE.

REQ-F-807: [Event-driven] When the user presses function key 11, the system shall set the function key 11 indicator to TRUE.

REQ-F-808: [Event-driven] When the user presses function key 12, the system shall set the function key 12 indicator to TRUE.

REQ-F-809: [Event-driven] When the user presses function key 13, the system shall set the function key 1 indicator to TRUE.

REQ-F-810: [Event-driven] When the user presses function key 14, the system shall set the function key 2 indicator to TRUE.

REQ-F-811: [Event-driven] When the user presses function key 15, the system shall set the function key 3 indicator to TRUE.

REQ-F-812: [Event-driven] When the user presses function key 16, the system shall set the function key 4 indicator to TRUE.

REQ-F-813: [Event-driven] When the user presses function key 17, the system shall set the function key 5 indicator to TRUE.

REQ-F-814: [Event-driven] When the user presses function key 18, the system shall set the function key 6 indicator to TRUE.

REQ-F-815: [Event-driven] When the user presses function key 19, the system shall set the function key 7 indicator to TRUE.

REQ-F-816: [Event-driven] When the user presses function key 20, the system shall set the function key 8 indicator to TRUE.

REQ-F-817: [Event-driven] When the user presses function key 21, the system shall set the function key 9 indicator to TRUE.

REQ-F-818: [Event-driven] When the user presses function key 22, the system shall set the function key 10 indicator to TRUE.

REQ-F-819: [Event-driven] When the user presses function key 23, the system shall set the function key 11 indicator to TRUE.

REQ-F-820: [Event-driven] When the user presses function key 24, the system shall set the function key 12 indicator to TRUE.


---


## 51. Function Key Validation and Action Dispatch
As an interactive user, I want only contextually valid key presses to trigger business actions so that I cannot perform invalid operations at each step of the card update workflow.

### Requirements

REQ-F-821: [Event-driven] When a function key is pressed, the system shall validate the key against the current transaction state; valid keys are ENTER, PF3 (exit), PF5 (confirm changes, only when changes are pending confirmation), and PF12 (cancel changes, only when card details have already been fetched). If the pressed key is not valid for the current state, the system shall override it to ENTER.

REQ-F-822: [Event-driven] When the user presses the ENTER key, the system shall set the action flag to ENTER to signal form submission.


---


## 52. Transaction State Routing
As an interactive user, I want the card update workflow to route me to the correct step based on my current state and key press so that the workflow progresses correctly.

### Requirements

REQ-F-823: [Complex] While the program is active and awaiting user input, when a function key is pressed, the system shall route to exit when PF3 is pressed; retrieve and display card details when PF12 is pressed or when re-entering from the card list; display the search screen when first entered from the menu or when card details have not yet been fetched; and process input and redisplay for all other cases.

REQ-F-824: [Event-driven] When the transaction state is changes-okayed-and-done, the system shall set the transaction state to show-details so the user can review the updated card details.

REQ-F-825: [Event-driven] When the transaction state is changes-ok-not-confirmed and the user does not press PF5, the system shall take no action and remain in the current state.

REQ-F-826: [Event-driven] When the transaction state is changes-not-ok, the system shall take no action and remain in the current state.

REQ-F-827: [Unwanted] If the transaction state is not one of the expected values (details-not-fetched, show-details, changes-not-ok, changes-ok-not-confirmed, or changes-okayed-and-done), the system shall record the abnormal termination with code '0001' and message 'UNEXPECTED DATA SCENARIO' and terminate the program.


---


## 53. Navigation and Control Transfer
As an interactive user, I want the system to transfer control to the correct destination program when I exit or complete a card update so that I am returned to the appropriate context.

### Requirements

REQ-F-828: [Event-driven] When the user presses PF3, or when changes have been confirmed and the last accessed screen was the credit card list, or when changes have failed and the last accessed screen was the credit card list, the system shall determine the destination program and transaction from the originating context; if the originating transaction identifier is empty or spaces, the system shall default the destination to the menu transaction (CM00) and menu program (COMEN01C).

REQ-F-829: [Event-driven] When transferring control back to the originating program or menu, the system shall record the current program and transaction as the source, clear the account and card numbers if the last accessed screen was the credit card list, set the user type to regular user, set the program context to initial entry, and transfer control to the destination program with the updated session context carrying account, card, and navigation data.


---


## 54. Card Update Input Processing and Validation
As an interactive user, I want my card update inputs validated against business rules so that only correct data is written to the card data store.

### Requirements

REQ-F-830: [Event-driven] When the card name field is validated, the system shall verify the card name is supplied and contains only alphabetic characters and spaces; if blank, the system shall set an input error flag and display a prompt for the card name; if non-alphabetic characters are present, the system shall set an input error flag and display the message 'Card name can only contain alphabets and spaces'.

REQ-F-831: [Event-driven] When the card status code field is validated, the system shall verify the value is 'Y' or 'N'; if blank or any other value, the system shall set an input error flag and display the message 'Card Active Status must be Y or N'.

REQ-F-832: [Event-driven] When the expiration month field is validated, the system shall verify the value is between 1 and 12; if blank or outside this range, the system shall set an input error flag and display the message 'Card expiry month must be between 1 and 12'.

REQ-F-833: [Event-driven] When the expiration year field is validated, the system shall verify the value is between 1950 and 2099; if blank or outside this range, the system shall set an input error flag and display the message 'Invalid card expiry year'.

REQ-F-834: [Event-driven] When the account identifier field is validated, the system shall verify the value is supplied and numeric; if blank, the system shall set an input error flag and display a prompt for the account number; if non-numeric, the system shall set an input error flag and display the message that the account filter must be an 11-digit number.

REQ-F-835: [Event-driven] When the card identifier field is validated, the system shall verify the value is supplied and numeric; if blank, the system shall set an input error flag, display a prompt for the card number, and clear the card identifier; if non-numeric, the system shall set an input error flag, display the message that the card ID filter must be a 16-digit number, and clear the card identifier.

REQ-F-836: [Event-driven] When the program has completed change detection and is ready to validate individual card fields, the system shall validate the card name, card status code, expiration month, and expiration year; if any validation fails, the system shall stop processing; if all validations pass, the system shall mark the change action as 'changes made but not yet confirmed'.

REQ-F-837: [Event-driven] When the card details have already been fetched and the user is editing them, the system shall compare the new card data (name, status, expiration date) against the old card data using case-insensitive comparison; if identical, the system shall set an information message indicating no changes were detected and mark all card field validations as passed.

REQ-F-838: [Event-driven] When the detail screen is displayed and the user has entered input, the system shall validate the input and detect changes; if input is valid and changes are detected, the system shall set the transaction state to changes-ok-not-confirmed to request user confirmation.

REQ-F-839: [Event-driven] When the user enters search criteria for the first time, the system shall validate the account identifier and card number; if both are blank, the system shall set an information message indicating 'No input received'; otherwise, the system shall clear any previously stored card data and prepare for the lookup.


---


## 55. Card Record Retrieval and Update
As an interactive user, I want the system to retrieve and update card records in the card data store (AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS) so that card details are accurately fetched and saved.

### Requirements

REQ-F-840: [Event-driven] When the user provides a card number to retrieve card details, the system shall read the card record from the card data store by card number and extract the card verification value, cardholder name, expiration date components, and active status into the old-details area.

REQ-F-841: [Unwanted] If the card record read operation returns a not-found response, the system shall set error flags for both account and card filters and display the message 'Did not find cards for this search condition'.

REQ-F-842: [Unwanted] If the card record read operation returns a technical error response, the system shall set an error flag for the account filter, log the operation name, file name, response code, and reason code, and display the error message to the user.

REQ-F-843: [Event-driven] When the card details have not yet been fetched and the user presses PF12 with both the account identifier and card identifier validation flags set to valid, the system shall retrieve the card record from the card data store; if found, the system shall set the transaction state to show-details.

REQ-F-844: [Event-driven] When the user confirms changes and presses PF5 to save, the system shall read the card record from the card data store with an update lock; if the lock fails, the system shall set an error flag with the message 'Could not lock record for update'.

REQ-F-845: [Event-driven] When no concurrent changes are detected in the locked record, the system shall assemble the updated card record from the new user-entered values (card number, account identifier, CVV code, cardholder name, expiration date constructed as year-month-day, and active status) and write the record to the card data store; if the write fails, the system shall set an error flag with the message 'Update of record failed'.

REQ-F-846: [Event-driven] When a card record is locked for update, the system shall compare the current record values (card verification value, cardholder name, expiration year, expiration month, expiration day, and active status) against the old values displayed to the user; if all match, the system shall allow the update to proceed; if any value differs, the system shall set a concurrent-change flag and update the old-details area with the current values from the locked record for user review.

REQ-F-847: [Event-driven] When the user presses PF5 with changes validated and confirmed, the system shall write the updated card record to the card data store and set the transaction state to: changes-okayed-lock-error if the record cannot be locked; changes-okayed-but-failed if locked but the write fails; show-details if the record was changed concurrently; or changes-okayed-and-done if the write succeeds.


---


## 56. Screen Input Normalization
As an interactive user, I want asterisk or blank entries in card update fields treated as no-change signals so that only explicitly entered values are applied to the card record.

### Requirements

REQ-F-848: [Event-driven] When the card name field contains an asterisk or is blank, the system shall clear the new card name to low-values; otherwise, the system shall copy the user's card name input to the new card name field.

REQ-F-849: [Event-driven] When the card status code field contains an asterisk or is blank, the system shall clear the new card status code to low-values; otherwise, the system shall copy the user's card status code input to the new card status code field.

REQ-F-850: [Event-driven] When the expiration month field contains an asterisk or is blank, the system shall clear the new expiration month to low-values; otherwise, the system shall copy the user's expiration month input to the new expiration month field.

REQ-F-851: [Event-driven] When the expiration year field contains an asterisk or is blank, the system shall clear the new expiration year to low-values; otherwise, the system shall copy the user's expiration year input to the new expiration year field.

REQ-F-852: [Ubiquitous] The system shall copy the expiration day from the screen input to the new expiration day field unconditionally.

REQ-F-853: [Event-driven] When the account identifier field contains an asterisk or is blank, the system shall clear the account identifier and new account identifier to low-values; otherwise, the system shall copy the user's account identifier input to both fields.

REQ-F-854: [Event-driven] When the card number field contains an asterisk or is blank, the system shall clear the card number and new card identifier to low-values; otherwise, the system shall copy the user's card number input to both fields.


---


## 57. Screen Display Preparation
As an interactive user, I want the card update screen to display the correct card details and messages based on the current transaction state so that I can review and act on accurate information.

### Requirements

REQ-F-855: [Ubiquitous] The system shall receive the card update screen input from the terminal into the input buffer, capturing the response code and reason code.

REQ-F-856: [Ubiquitous] The system shall clear the output buffer, populate header fields (screen titles, transaction identifier, and program name), retrieve and format the current date as MM/DD/YY and the current time as HH:MM:SS, and move these values to the screen output buffer.

REQ-F-857: [Event-driven] When the program re-enters the screen and the account identifier is zero, the system shall clear the account identifier output field; otherwise, the system shall display the stored account identifier.

REQ-F-858: [Event-driven] When the program re-enters the screen and the card identifier is zero, the system shall clear the card identifier output field; otherwise, the system shall display the stored card identifier.

REQ-F-859: [Event-driven] When the change-action flag indicates that card details have not yet been fetched, the system shall clear all card detail fields (name, status code, expiration day, expiration month, and expiration year) to low-values on the screen.

REQ-F-860: [Event-driven] When the change-action flag indicates that card details should be shown, the system shall populate the screen with the previously stored card name, status code, expiration day, expiration month, and expiration year.

REQ-F-861: [Event-driven] When the change-action flag indicates that changes have been made to the card data, the system shall populate the screen with the new card name, new card status code, new expiration month, and new expiration year, while retaining the old expiration day.

REQ-F-862: [Unwanted] If the change-action flag contains an unrecognized value, the system shall default to displaying the previously stored card name, status code, expiration day, expiration month, and expiration year.

REQ-F-863: [State-driven] While the screen display buffer is being prepared for sending, the system shall set the information message based on the current transaction state (prompt for search, show details, prompt for changes, prompt for confirmation, confirm success, or inform failure) and move the information message and error message to the screen output.

REQ-F-864: [Event-driven] When the user returns from the credit card list, the system shall reset the account identifier and card identifier field colors to default.


---


## 58. Cursor Positioning
As an interactive user, I want the cursor positioned at the relevant field when validation fails so that I can correct errors efficiently.

### Requirements

REQ-F-865: [Event-driven] When account identifier validation fails or the field is blank, the system shall position the cursor to the account identifier field.

REQ-F-866: [Event-driven] When card identifier validation fails or the field is blank, the system shall position the cursor to the card identifier field.

REQ-F-867: [Event-driven] When card name validation fails or the field is blank, the system shall position the cursor to the card name field.

REQ-F-868: [Event-driven] When card status code validation fails or the field is blank, the system shall position the cursor to the card status code field.

REQ-F-869: [Event-driven] When expiration month validation fails or the field is blank, the system shall position the cursor to the expiration month field.

REQ-F-870: [Event-driven] When expiration year validation fails or the field is blank, the system shall position the cursor to the expiration year field.

REQ-F-871: [Event-driven] When cards are found for the account or no changes are detected, the system shall position the cursor to the card name field.

REQ-F-872: [Event-driven] When no other cursor positioning condition is explicitly matched, the system shall position the cursor to the account identifier field.


### Open Questions

OQ-014: Rule `1cfef88c_4932_4537_8166_74be36d2b678` states that the program initializes card-details state based on whether the caller is the menu program on initial entry. The specific business rule governing what state is set when the caller is the menu program versus any other caller is not fully described in the rule text. Clarification is needed on whether this constitutes a distinct routing requirement. — Owner: business analyst / modernization team

OQ-015: Rule `648f38ac_a7bb_487a_ad96_f26bdb2cac20` describes reading the card record and extracting fields into the old-details area. The rule is classified as noise_context (working storage initialization), but it contains a data-access step with business meaning (reading the card data store by card number). Confirm whether a standalone retrieval requirement is needed beyond REQ-F-049. — Owner: modernization team

OQ-016: Rule `bd520277_0362_4100_94dd_6564e1273c30` is classified as noise_context but describes four distinct outcome states for the PF5 save operation (lock error, update failure, concurrent change, success). REQ-F-056 captures these outcomes. Confirm that all four state transitions are required in the modernized system. — Owner: business analyst


---


## 59. Interactive Navigation and Screen Control
As an interactive user, I want the card demonstration application to validate my menu selections, enforce authorization rules, and route me to the correct screen so that I can navigate the application safely and efficiently.


**Communication Area and Entry Handling**

### Requirements

REQ-F-873: [Event-driven] When the program is invoked with no communication area (length is zero), the system shall immediately transfer control to the sign-on screen.

REQ-F-874: [Event-driven] When the program is invoked with a communication area of length greater than zero, the system shall restore the incoming communication area into the local communication area record, preserving the originating transaction identifier, originating program name, and program context.

REQ-F-875: [Event-driven] When the program context indicator indicates first invocation (not re-entry), the system shall display the menu screen with cleared output fields.

REQ-F-876: [Event-driven] When the program context indicator indicates re-entry status, the system shall receive the menu screen input and dispatch based on the attention identifier: process the menu selection when the Enter key is pressed, handle the PF3 key via the sign-on routing path, or display an invalid-key error message for any other key.

REQ-F-877: [Event-driven] When the PF3 function key is pressed during re-entry processing, the system shall set the destination program to the sign-on screen program and transfer control to it.

REQ-F-878: [Event-driven] When the system is about to transfer control and the destination program name is empty or uninitialized, the system shall default the destination program to the sign-on screen program before transferring control.

REQ-F-879: [Ubiquitous] The system shall transfer control to the destination program specified in the communication area.

REQ-F-880: [Ubiquitous] The system shall retrieve the current system date and time, populate the screen title lines with 'AWS Mainframe Modernization' and 'CardDemo', populate the transaction identifier and program name fields, format and display the current date as MM/DD/YY, and format and display the current time as HH:MM:SS.

REQ-F-881: [Event-driven] When assembling menu option display positions 1 through 12, the system shall format each menu option text by concatenating the sequence number, a period and space, and the display name, then assign the formatted text to the corresponding menu option output position.

REQ-F-882: [Event-driven] When the loop counter exceeds the range of defined menu option positions (1 through 12), the system shall take no action and continue to the next loop iteration.

REQ-F-883: [Ubiquitous] The system shall terminate the menu option assembly loop after all menu options have been processed.

REQ-F-884: [Event-driven] When the user submits input from the menu screen, the system shall receive the user's menu option selection and capture the response status.

REQ-F-885: [Event-driven] When the Enter key is pressed on the menu screen, the system shall route to the menu option processing handler.

REQ-F-886: [Ubiquitous] The system shall clear the error flag indicator at the start of menu processing to indicate no error condition.

REQ-F-887: [Ubiquitous] The system shall trim trailing spaces from the menu option input and replace remaining spaces with zeros, then convert the normalized alphanumeric option selection to a two-digit numeric value.

REQ-F-888: [Event-driven] When the menu option is non-numeric, exceeds the available menu options count (11), or equals zero, the system shall set the error flag indicator to 'Y' to reject the invalid selection.

REQ-F-889: [Event-driven] When the user presses Enter and the option fails validation (non-numeric, exceeds count of 11, or is zero), the system shall set the error flag to 'Y', store the error message 'Please enter a valid option number...' in the message text, and display the menu screen with the error.

REQ-F-890: [Event-driven] When the user type is standard user and the selected menu option is restricted to authorized users only (user type authorization code 'A'), the system shall set the error flag indicator to 'Y' to deny access to the restricted menu option.

REQ-F-891: [Event-driven] When a regular user selects a menu option restricted to administrators, the system shall set the error flag to 'Y', display the error message 'No access - Admin Only option... ', and send the menu screen.

REQ-F-892: [Event-driven] When the error flag indicator is not set and the selected menu option program name is 'COPAUS0C', the system shall verify the target program is available; when available, populate the communication area with the originating transaction identifier, originating program name, and reset the program context indicator to zero, then transfer control to the target program; when the program is not available, invoke error handling.

REQ-F-893: [Event-driven] When no error flag is set and the selected menu option program name is not 'COPAUS0C' and does not begin with 'DUMMY', the system shall process the selected option via an alternative path and send the menu screen.

REQ-F-894: [Complex] While no error flag is set, when the selected menu option program name begins with 'DUMMY', the system shall display an informational message indicating the option is coming soon.

REQ-F-895: [Complex] While no error flag is set, when the selected menu option program is 'COPAUS0C' and the program is not found, the system shall display an error message indicating the option is not installed.


### Open Questions

OQ-017: Rule fc55f924 describes error handling when the INQUIRE for 'COPAUS0C' fails but states the alternative branch is "not included in this slice." What specific error message or state transition should occur when the target program is unavailable? — Owner: application team

OQ-018: Rule 83dafae1 states that when any key other than Enter or PF3 is pressed, the system displays an invalid-key error message and sends the menu screen, but does not specify the exact error message text. What is the required error message for an unrecognized key? — Owner: application team


---


## 60. Screen Navigation and Program Transfer
As an interactive user, I want the application to route me to the correct program based on my session context and key press so that I can navigate the application consistently.

### Requirements

REQ-F-896: [Event-driven] When the program is invoked without a communication area, the system shall set the destination program to the signon screen program and transfer control to it.

REQ-F-897: [Complex] While the program is re-entering with a valid communication area and the program context indicator is set to re-entry status, when the PF3 key is pressed, the system shall set the destination program to the menu program and transfer control to it.

REQ-F-898: [Ubiquitous] The system shall validate the destination program name and, if it is unset, default it to the signon screen program; the system shall then populate the communication area with the current program's transaction identifier and program name, reset the program context indicator to zero, and transfer control to the destination program.


---


## 61. Transaction Report Request and Job Submission
As an interactive user, I want to select a report type, provide date parameters, and submit a report job so that the appropriate report is generated.

### Requirements

REQ-F-899: [Event-driven] When the user submits input from the transaction report screen, the system shall receive the user's input into the screen input record and capture the response code and reason code from the receive operation.

REQ-F-900: [Event-driven] When the user presses ENTER without selecting a report type, the system shall display the error message 'Select a report type to print report...' and set the error flag to 'Y'.

REQ-F-901: [Event-driven] When the user selects the monthly report option, the system shall set the report name to 'Monthly', set the start date to the first day of the current month, calculate the end date as the last day of the current month, store both dates in the job data parameters, and invoke the job submission process.

REQ-F-902: [Event-driven] When the user selects the yearly report option, the system shall set the report name to 'Yearly', set the start date to January 1st of the current year, set the end date to December 31st of the current year, store both dates in the job data parameters, and invoke the job submission process.

REQ-F-903: [Event-driven] When the user selects the custom report option and the start date month field is empty, the system shall display the error message 'Start Date - Month can NOT be empty...' and set the error flag to 'Y'.

REQ-F-904: [Event-driven] When the user selects the custom report option and the start date day field is empty, the system shall display the error message 'Start Date - Day can NOT be empty...' and set the error flag to 'Y'.

REQ-F-905: [Event-driven] When the user selects the custom report option and the start date year field is empty, the system shall display the error message 'Start Date - Year can NOT be empty...' and set the error flag to 'Y'.

REQ-F-906: [Event-driven] When the user selects the custom report option and the end date month field is empty, the system shall display the error message 'End Date - Month can NOT be empty...' and set the error flag to 'Y'.

REQ-F-907: [Event-driven] When the user selects the custom report option and the end date day field is empty, the system shall display the error message 'End Date - Day can NOT be empty...' and set the error flag to 'Y'.

REQ-F-908: [Event-driven] When the user selects the custom report option and the end date year field is empty, the system shall display the error message 'End Date - Year can NOT be empty...' and set the error flag to 'Y'.

REQ-F-909: [Ubiquitous] The system shall convert the start date month, day, and year text inputs to numeric values, and convert the end date month, day, and year text inputs to numeric values, in preparation for range validation.

REQ-F-910: [Unwanted] If the start date month is not numeric or exceeds 12, the system shall display the error message 'Start Date - Not a valid Month...' and set the error flag to 'Y'.

REQ-F-911: [Unwanted] If the start date day is not numeric or exceeds 31, the system shall display the error message 'Start Date - Not a valid Day...' and set the error flag to 'Y'.

REQ-F-912: [Unwanted] If the start date year is not numeric, the system shall display the error message 'Start Date - Not a valid Year...' and set the error flag to 'Y'.

REQ-F-913: [Unwanted] If the end date month is not numeric or exceeds 12, the system shall display the error message 'End Date - Not a valid Month...' and set the error flag to 'Y'.

REQ-F-914: [Unwanted] If the end date day is not numeric or exceeds 31, the system shall display the error message 'End Date - Not a valid Day...' and set the error flag to 'Y'.

REQ-F-915: [Unwanted] If the end date year is not numeric, the system shall display the error message 'End Date - Not a valid Year...' and set the error flag to 'Y'.

REQ-F-916: [Event-driven] When start date format validation is requested, the system shall invoke the date validation service (CSUTLDTC) with the start date and format mask; if the severity code returned is not '0000' and the message number is not '2513', the system shall display the error message 'Start Date - Not a valid date...' and set the error flag to 'Y'.

REQ-F-917: [Event-driven] When end date format validation is requested, the system shall invoke the date validation service (CSUTLDTC) with the end date and format mask; if the severity code returned is not '0000' and the message number is not '2513', the system shall display the error message 'End Date - Not a valid date...' and set the error flag to 'Y'.

REQ-F-918: [Ubiquitous] The system shall assemble the custom date range by moving the validated start date year, month, and day from the screen input fields to the start date group, and the validated end date year, month, and day from the screen input fields to the end date group.

REQ-F-919: [Ubiquitous] The system shall store the validated start date in the job data SYMNAMES and DATEPARM parameters, store the validated end date in the job data SYMNAMES and DATEPARM parameters, set the report name to 'Custom', and, if no errors have occurred, invoke the job submission process.

REQ-F-920: [Event-driven] When the job submission process begins and the confirmation field is empty, the system shall construct a message asking the user to confirm printing of the selected report type and set the error flag to 'Y'.

REQ-F-921: [Event-driven] When the user provides a confirmation response of 'Y' or 'y', the system shall proceed to submit JCL statements to the job queue.

REQ-F-922: [Event-driven] When the user provides a confirmation response of 'N' or 'n', the system shall initialize all input fields and set the error flag to 'Y'.

REQ-F-923: [Event-driven] When the user provides a confirmation response other than 'Y', 'y', 'N', or 'n', the system shall display an error message indicating the invalid confirmation value and set the error flag to 'Y'.

REQ-F-924: [State-driven] While JCL statements remain to be submitted and no errors have occurred, the system shall iterate through the JCL statement lines, and for each line that is not the end-of-file marker ('/*EOF'), spaces, or low-values, write the JCL statement to the job queue; the loop shall terminate when the end-of-file marker is encountered, an empty line is found, all 1000 possible lines are processed, or an error occurs.

REQ-F-925: [Event-driven] When a JCL statement write to the job queue fails, the system shall display the error message 'Unable to Write TDQ (JOBS)...' and set the error flag to 'Y'.

REQ-F-926: [Event-driven] When all validations pass and the job is submitted successfully, the system shall initialize all input fields, construct a confirmation message indicating the report type has been submitted for printing, and display the success message.


---


## 62. Date Validation Service
As a calling program, I want to delegate date validation to a shared service so that date format and value correctness is enforced consistently.

### Requirements

REQ-F-927: [Ubiquitous] The date validation service shall accept an input date string, a format mask, and a result area from the caller.

REQ-F-928: [Ubiquitous] The date validation service shall invoke the date conversion service with the input date string and format mask to convert the date to Lillian format, then extract the severity code and message number from the feedback structure and store them in the message record.

REQ-F-929: [Event-driven] When the date conversion service returns a feedback code, the date validation service shall map the feedback code to a result description using the following mappings: invalid-date condition → 'Date is valid'; insufficient-data condition → 'Insufficient'; bad-date-value condition → 'Datevalue error'; invalid-era condition → 'Invalid Era'; unsupported-range condition → 'Unsupp. Range'; invalid-month condition → 'Invalid month'; bad-picture-string condition → 'Bad Pic String'; non-numeric-data condition → 'Nonnumeric data'; year-in-era-zero condition → 'YearInEra is 0'; any other feedback code → 'Date is invalid'.

REQ-F-930: [Ubiquitous] The date validation service shall return the formatted message record containing the validation result, severity code, message number, test date, and format mask to the caller via the result area.


---


## 63. Screen Navigation and Program Transfer
As a user of the card demonstration application, I want the system to correctly route me to the appropriate screen based on my entry context and function key input so that I can navigate the application without errors.

### Requirements

REQ-F-931: [Event-driven] When the system is invoked with no session context (communication area length is zero), the system shall set the destination program to the signon screen ('COSGN00C') and transfer control to it.

REQ-F-932: [Event-driven] When the system is invoked with an existing session context (communication area length is greater than zero), the system shall unpack the passed session context into the local session context record.

REQ-F-933: [Event-driven] When the system is invoked with a non-empty session context and the program context indicator indicates initial entry, the system shall set the program context indicator to re-entry status and clear the transaction list screen output buffer.

REQ-F-934: [Ubiquitous] The system shall initialize the next-page flag to 'N' to indicate no additional page on entry.

REQ-F-935: [Event-driven] When the PF3 key is pressed during re-entry, the system shall set the destination program to the menu screen ('COMEN01C'), reset the program context indicator to zero, and transfer control to the menu screen.

REQ-F-936: [Unwanted] If the destination program name is empty or contains only spaces before transfer, the system shall set the destination program name to the signon screen ('COSGN00C').

REQ-F-937: [Ubiquitous] The system shall populate the session context with the current transaction ID ('CT00'), the current program name ('COTRN00C'), and reset the program context indicator to zero before transferring control to the destination program.

REQ-F-938: [Ubiquitous] The system shall transfer control to the destination program, passing the populated session context.


---


## 64. Transaction List Display and Pagination
As a user, I want to view a paginated list of transactions and navigate forward and backward through pages so that I can locate and select a specific transaction.

### Requirements

REQ-F-939: [State-driven] While the program context indicator is set to re-entry status, the system shall receive the transaction list screen input from the user, capturing all ten selection indicators and transaction identifiers, and record the response and reason codes.

REQ-F-940: [Event-driven] When the program is re-entered and the user presses a function key, the system shall dispatch based on the key pressed: if ENTER, process transaction selection; if PF7, process page-backward navigation; if PF8, process page-forward navigation; if any other key, store the invalid-key error message and send the screen with the error displayed.

REQ-F-941: [Ubiquitous] The system shall retrieve the current system date and time, format the date as MM/DD/YY and the time as HH:MM:SS, and populate the screen header with the screen titles, transaction ID, program name, formatted date, and formatted time.

REQ-F-942: [Event-driven] When the user enters a transaction ID search value or navigates to a new page, the system shall initiate a browse of the transaction data store (AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS) starting at the specified transaction ID; if the browse start encounters a not-found condition, the system shall set the end-of-file flag, store the message 'You are at the top of the page...', and send the screen with the error; if the browse start encounters any other error, the system shall set the error flag, store the message 'Unable to lookup transaction...', and send the screen with the error.

REQ-F-943: [State-driven] While the transaction data store cursor is positioned and the screen display rows are cleared, the system shall read each transaction record from the transaction data store, populate the corresponding screen row with transaction ID, date, description, and amount, and increment the row counter; the system shall continue until 10 rows are filled, end-of-file is reached, or an error occurs.

REQ-F-944: [Event-driven] When the first transaction record is successfully retrieved, the system shall clear all 10 transaction display rows on the screen to remove stale data before populating the new batch.

REQ-F-945: [Event-driven] When a next-transaction read encounters end-of-file during forward pagination, the system shall set the end-of-file flag, store the message 'You have reached the bottom of the page...', and send the screen with the error.

REQ-F-946: [Unwanted] If a next-transaction read encounters an error other than end-of-file during forward pagination, the system shall set the error flag, store the message 'Unable to lookup transaction...', and send the screen with the error.

REQ-F-947: [Event-driven] When the transaction batch retrieval loop completes and more transactions remain in the data store, the system shall increment the page counter and read the next record to position for the next page; if no more transactions remain but at least one transaction was displayed, the system shall increment the page counter to mark page completion.

REQ-F-948: [Ubiquitous] The system shall update the page number field on the screen, clear the transaction ID input field, and send the transaction list screen to the user.

REQ-F-949: [Event-driven] When forward-page navigation is requested, the system shall position the transaction data store cursor at the start of the next batch of records.

REQ-F-950: [Event-driven] When cursor positioning completes for forward navigation, the system shall verify no error occurred during cursor positioning and that the user pressed a valid navigation key; if both conditions are met, the system shall read the next transaction record; otherwise the system shall skip the read operation.

REQ-F-951: [Unwanted] If the browse start fails during forward-page navigation, the system shall stop processing; if the first read-next encounters end-of-file, the system shall set the end-of-file flag, store the message 'You are at the top of the page...', and send the screen; if any operation encounters a different error, the system shall set the error flag, store the message 'Unable to lookup transaction...', and send the screen.

REQ-F-952: [Event-driven] When the user requests backward page navigation, the system shall position the transaction data store cursor at the starting point for reverse-order record retrieval; if positioning fails, the system shall set the error flag to indicate the failure.

REQ-F-953: [Event-driven] When backward page navigation is initiated, the system shall position the cursor at the transaction data store starting point; if positioning succeeds and the user pressed PF8, the system shall read the first previous record to establish backward direction; if positioning fails, the system shall set the error flag and abort navigation.

REQ-F-954: [Complex] While no error has occurred during cursor positioning, when the user pressed the page-backward key, the system shall read the first transaction record in reverse order; if the read succeeds and no error occurs, the system shall initialize the display fields for all 10 transaction rows.

REQ-F-955: [State-driven] While the loop counter is set to 10 and records remain available, the system shall read each transaction record in reverse order; for each record successfully retrieved, the system shall populate the corresponding display row with transaction details and decrement the loop counter; the system shall continue until 10 records are populated, end-of-file is reached, or an error occurs.

REQ-F-956: [Event-driven] When a previous-transaction read encounters end-of-file during backward pagination, the system shall set the end-of-file flag, store the message 'You have reached the top of the page...', and send the screen with the error.

REQ-F-957: [Unwanted] If a previous-transaction read encounters an error other than end-of-file during backward pagination, the system shall set the error flag, store the message 'Unable to lookup transaction...', and send the screen with the error.

REQ-F-958: [Complex] While the transaction list has been populated and no error has occurred, when an additional record is successfully read to check for more pages, the system shall set the next-page flag; if the next-page flag is set and the current page number is greater than 1, the system shall decrement the page number by 1; otherwise the system shall set the page number to 1.

REQ-F-959: [Ubiquitous] The system shall move the current page number to the screen page-number field and send the transaction list screen to display the retrieved transactions.

REQ-F-960: [Ubiquitous] The system shall extract the transaction amount, parse the transaction timestamp to isolate year, month, and day components, and format the date as MM/DD/YY for screen display.


---


## 65. Transaction Row Population
As a user, I want each transaction row on the list screen to display the correct transaction ID, date, description, and amount so that I can identify and select the right transaction.

### Requirements

REQ-F-961: [Event-driven] When the row index is 1, the system shall populate the first transaction row with the transaction ID, formatted date, description, and amount, and store the transaction ID as the first transaction ID in the session context for pagination tracking.

REQ-F-962: [Event-driven] When the row index is 2, the system shall populate the second transaction row with the transaction ID, formatted date, description, and amount.

REQ-F-963: [Event-driven] When the row index is 3, the system shall populate the third transaction row with the transaction ID, formatted date, description, and amount.

REQ-F-964: [Event-driven] When the row index is 4, the system shall populate the fourth transaction row with the transaction ID, formatted date, description, and amount.

REQ-F-965: [Event-driven] When the row index is 5, the system shall populate the fifth transaction row with the transaction ID, formatted date, description, and amount.

REQ-F-966: [Event-driven] When the row index is 6, the system shall populate the sixth transaction row with the transaction ID, formatted date, description, and amount.

REQ-F-967: [Event-driven] When the row index is 7, the system shall populate the seventh transaction row with the transaction ID, formatted date, description, and amount.

REQ-F-968: [Event-driven] When the row index is 8, the system shall populate the eighth transaction row with the transaction ID, formatted date, description, and amount.

REQ-F-969: [Event-driven] When the row index is 9, the system shall populate the ninth transaction row with the transaction ID, formatted date, description, and amount.

REQ-F-970: [Event-driven] When the row index is 10, the system shall populate the tenth transaction row with the transaction ID, formatted date, description, and amount, and store the transaction ID as the last transaction ID in the session context for pagination tracking.

REQ-F-971: [Unwanted] If the row index is outside the range 1 through 10, the system shall take no action.

REQ-F-972: [Event-driven] When the row index is 1, the system shall clear the transaction ID, date, description, and amount fields for row 1 by setting each to spaces.

REQ-F-973: [Event-driven] When the row index is 2, the system shall clear the transaction ID, date, description, and amount fields for row 2 by setting each to spaces.

REQ-F-974: [Event-driven] When the row index is 3, the system shall clear the transaction ID, date, description, and amount fields for row 3 by setting each to spaces.

REQ-F-975: [Event-driven] When the row index is 4, the system shall clear the transaction ID, date, description, and amount fields for row 4 by setting each to spaces.

REQ-F-976: [Event-driven] When the row index is 5, the system shall clear the transaction ID, date, description, and amount fields for row 5 by setting each to spaces.

REQ-F-977: [Event-driven] When the row index is 6, the system shall clear the transaction ID, date, description, and amount fields for row 6 by setting each to spaces.

REQ-F-978: [Event-driven] When the row index is 7, the system shall clear the transaction ID, date, description, and amount fields for row 7 by setting each to spaces.

REQ-F-979: [Event-driven] When the row index is 8, the system shall clear the transaction ID, date, description, and amount fields for row 8 by setting each to spaces.

REQ-F-980: [Event-driven] When the row index is 9, the system shall clear the transaction ID, date, description, and amount fields for row 9 by setting each to spaces.

REQ-F-981: [Event-driven] When the row index is 10, the system shall clear the transaction ID, date, description, and amount fields for row 10 by setting each to spaces.

REQ-F-982: [Unwanted] If the row index is outside the range 1 to 10 during a clear operation, the system shall take no action.


---


## 66. Transaction Selection Capture and Routing
As a user, I want to select a transaction from the list and be routed to the transaction detail screen so that I can view full transaction information.

### Requirements

REQ-F-983: [Event-driven] When the user enters a non-empty selection indicator in transaction row 1, the system shall record the selection indicator and the corresponding transaction ID in the session context.

REQ-F-984: [Event-driven] When the user selects transaction option 2, the system shall extract the selection indicator and transaction identifier for option 2 from the screen input and store them in the session context.

REQ-F-985: [Event-driven] When the user selects transaction option 3, the system shall extract the selection indicator and transaction identifier for option 3 from the screen input and store them in the session context.

REQ-F-986: [Event-driven] When the user selects transaction option 4, the system shall extract the selection indicator and transaction identifier for option 4 from the screen input and store them in the session context.

REQ-F-987: [Event-driven] When the user selects transaction option 5, the system shall extract the selection indicator and transaction identifier for option 5 from the screen input and store them in the session context.

REQ-F-988: [Event-driven] When the user selects transaction option 6, the system shall extract the selection indicator and transaction identifier for option 6 from the screen input and store them in the session context.

REQ-F-989: [Event-driven] When the user selects transaction option 7, the system shall extract the selection indicator and transaction identifier for option 7 from the screen input and store them in the session context.

REQ-F-990: [Event-driven] When the user selects transaction option 8, the system shall extract the selection indicator and transaction identifier for option 8 from the screen input and store them in the session context.

REQ-F-991: [Event-driven] When the user selects transaction option 9, the system shall extract the selection indicator and transaction identifier for option 9 from the screen input and store them in the session context.

REQ-F-992: [Event-driven] When the user selects transaction option 10, the system shall extract the selection indicator and transaction identifier for option 10 from the screen input and store them in the session context.

REQ-F-993: [Event-driven] When no valid transaction option is selected (none of the ten selection indicators contain a non-space, non-low-value character), the system shall clear both the transaction selection flag and the transaction identifier in the session context.

REQ-F-994: [Complex] While the transaction selection flag and selected transaction ID are both populated, when the user enters a transaction ID search value and presses ENTER, the system shall validate the selection flag (accept 'S' or 's'; reject other values with the error message 'Invalid selection. Valid value is S'); validate the transaction ID input (accept numeric values or spaces/low-values for beginning-of-file search; reject non-numeric values with the error message 'Tran ID must be Numeric ...'); reset the page number to 0; and invoke page-forward processing to retrieve and display the first page of matching transactions.

REQ-F-995: [Event-driven] When the user presses the Enter key on the transaction list screen, the system shall process the transaction selection and route to the destination program.


---


## 67. Transaction Detail View (Invoked Program)
As a user, I want to view the full details of a selected transaction so that I can review all transaction and merchant information.

### Requirements

REQ-F-996: [Event-driven] When the invoked transaction detail handler is called with an empty session context, the system shall set the destination program to the signon screen and transfer control to it.

REQ-F-997: [Ubiquitous] The invoked transaction detail handler shall validate the destination program name and default to the signon program if the destination is missing, populate the session context with the current transaction ID, program name, and reset the program context to initial entry status, then transfer control to the destination program.

REQ-F-998: [Complex] While the invoked transaction detail handler is re-entering with a valid session context and program context indicator set to re-entry, when a function key is pressed, the system shall route to the appropriate destination: for PF3, route to the recorded calling program if available or to the menu program if not; for PF5, route to the transaction main program; for any other key, execute the applicable alternative branch.

REQ-F-999: [Event-driven] When the invoked transaction detail handler is reentered after sending the screen, the system shall receive the user's input from the transaction view screen into the input buffer and capture the response code and reason code.

REQ-F-1000: [Event-driven] When the user presses ENTER on the transaction view screen, the system shall validate that the transaction ID is not empty; if empty, the system shall set the error flag to 'Y', store the error message 'Tran ID can NOT be empty...', and send the screen with the error; if not empty, the system shall continue to the next validation step.

REQ-F-1001: [Event-driven] When transaction ID validation succeeds, the system shall clear all transaction detail display fields to spaces and retrieve the transaction record from the transaction data store (AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS) using the entered transaction ID (TRAN-ID, alphanumeric 16) as the key.

REQ-F-1002: [Event-driven] When transaction file retrieval succeeds, the system shall move the retrieved transaction details (transaction ID, card number, transaction type code, transaction category code, transaction source, transaction amount, transaction description, original timestamp, processing timestamp, merchant ID, merchant name, merchant city, and merchant zip) to the screen display fields and send the screen.

REQ-F-1003: [Unwanted] If the transaction ID is not found in the transaction data store, the system shall set the error flag to 'Y', store the error message 'Transaction ID NOT found...', and send the screen with the error.

REQ-F-1004: [Unwanted] If the transaction data store retrieval fails for any reason other than not-found, the system shall set the error flag to 'Y', store the error message 'Unable to lookup Transaction...', and send the screen with the error.

REQ-F-1005: [Event-driven] When the user presses PF4 on the transaction view screen, the system shall clear all transaction detail fields and the message buffer to their default values and send the blank screen to the user.

REQ-F-1006: [Unwanted] If the user presses an invalid key (any key other than ENTER, PF3, PF4, or PF5) on the transaction view screen, the system shall store the error message 'Invalid key pressed. Please see below... ' and send the screen with the error.

REQ-F-1007: [Ubiquitous] The invoked transaction detail handler shall move the error message from the message buffer to the screen error message output field before sending the screen.

REQ-F-1008: [Ubiquitous] The invoked transaction detail handler shall retrieve the current system date and time, format them as MM/DD/YY and HH:MM:SS respectively, and populate the screen header with the title lines, transaction ID, program name, formatted date, and formatted time.

REQ-F-1009: [Event-driven] When the invoked transaction detail handler is reentered (program reentry flag is set), the system shall receive user input and dispatch based on the key pressed: process transaction ID entry on ENTER, clear the screen on PF4, or display an invalid-key error message on any other key.


---


## 68. Transaction Navigation and Screen Routing
As a user of the card demonstration application, I want function key presses to route me to the correct destination screen so that I can navigate the application efficiently.

### Requirements

REQ-F-1010: [Event-driven] When the program is invoked with an empty session context, the system shall set the destination program to the sign-on program and transfer control to it.

REQ-F-1011: [Complex] While the program is re-entering with a valid session context and the program context indicator is set to re-entry, when the PF3 key is pressed, the system shall route to the recorded calling program if one is present in the session context, or to the menu program if no calling program is recorded.

REQ-F-1012: [Complex] While the program is re-entering with a valid session context and the program context indicator is set to re-entry, when the PF5 key is pressed, the system shall route to the transaction main program.

REQ-F-1013: [Ubiquitous] The system shall validate that a destination program name has been set before transferring control; if the destination program name is empty, the system shall default it to the sign-on program.

REQ-F-1014: [Ubiquitous] The system shall populate the session context with the current transaction identifier, the current program name, and reset the program context indicator to zero (initial entry status) before transferring control to the destination program.


---


## 69. Transaction View Screen — Input Handling and Display
As a user, I want to enter a transaction ID and view the corresponding transaction details and merchant information so that I can review individual transaction records.

### Requirements

REQ-F-1015: [Event-driven] When the program is invoked for the first time (session context length is zero or the program re-entry flag is not set) and a pre-selected transaction ID exists in the session context, the system shall process that transaction ID immediately; otherwise the system shall present a blank transaction inquiry screen.

REQ-F-1016: [Event-driven] When the program is re-entered (program re-entry flag is set), the system shall receive the user's input from the transaction inquiry screen and dispatch based on the key pressed: process transaction ID entry on Enter, clear the screen on PF4, or display an invalid-key error message on any other key.

REQ-F-1017: [Event-driven] When the user presses Enter, the system shall validate that the transaction ID field is not empty; if the transaction ID is empty, the system shall set the error flag to 'Y' and display the error message 'Tran ID can NOT be empty...'.

REQ-F-1018: [Event-driven] When transaction ID validation succeeds (error flag is not set), the system shall clear all transaction detail display fields (transaction ID, card number, transaction type code, transaction category code, transaction source, transaction amount, transaction description, original timestamp, processing timestamp, merchant ID, merchant name, merchant city, and merchant zip) to spaces.

REQ-F-1019: [Event-driven] When transaction ID validation succeeds, the system shall retrieve the transaction record from the transaction data store (AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS) using the entered transaction ID (TRAN-ID, alphanumeric 16) as the key.

REQ-F-1020: [Event-driven] When transaction file retrieval succeeds, the system shall move the retrieved transaction details (transaction ID, card number, transaction type code, transaction category code, transaction source, transaction amount, transaction description, original timestamp, processing timestamp, merchant ID, merchant name, merchant city, and merchant zip) to the corresponding screen display fields and present the screen to the user.

REQ-F-1021: [Unwanted] If the transaction ID is not found in the transaction data store, the system shall set the error flag to 'Y' and display the error message 'Transaction ID NOT found...'.

REQ-F-1022: [Unwanted] If the transaction file retrieval fails for any reason other than not-found, the system shall set the error flag to 'Y' and display the error message 'Unable to lookup Transaction...'.

REQ-F-1023: [Event-driven] When the user presses PF4, the system shall clear all transaction detail fields and the message buffer to their default values and present the blank screen to the user.

REQ-F-1024: [Unwanted] If the user presses any key other than Enter or PF4, the system shall display the error message 'Invalid key pressed. Please see below... '.

REQ-F-1025: [Ubiquitous] The system shall move any error message from the message buffer to the screen error message output field before presenting the screen to the user.

REQ-F-1026: [Ubiquitous] The system shall retrieve the current system date and time, format the date as MM/DD/YY and the time as HH:MM:SS, and populate the screen header with the title lines, transaction identifier, program name, formatted date, and formatted time before presenting the screen.


---


## 70. Transaction List Display and Pagination
As a user, I want to browse a paginated list of transactions and select one for detailed viewing so that I can locate and review specific transaction records.

### Requirements

REQ-F-1027: [Event-driven] When the program is invoked with a non-empty session context and the program context indicator indicates initial entry, the system shall set the program context indicator to re-entry status and clear the transaction list screen output buffer.

REQ-F-1028: [Event-driven] When the program is re-entered with the program context indicator set to re-entry status, the system shall receive the transaction list screen input and dispatch based on the key pressed: process transaction selection on Enter, process backward pagination on PF7, process forward pagination on PF8, or display an invalid-key error message on any other key.

REQ-F-1029: [Event-driven] When the program is invoked with no session context, the system shall set the destination program to the sign-on program and transfer control to it.

REQ-F-1030: [Event-driven] When the user presses PF3, the system shall set the destination program to the menu screen and transfer control to it, resetting the program context indicator to zero.

REQ-F-1031: [Ubiquitous] The system shall populate the session context with the current transaction identifier, the current program name, and reset the program context indicator to zero before transferring control to any destination program.

REQ-F-1032: [Event-driven] When the user enters a transaction ID search value or navigates to a new page, the system shall initiate a browse of the transaction data store starting at the specified transaction ID; if the browse start encounters a not-found condition, the system shall set the end-of-file flag and display the message 'You are at the top of the page...'; if the browse start encounters any other error, the system shall set the error flag and display the message 'Unable to lookup transaction...'.

REQ-F-1033: [Event-driven] When forward pagination is requested and cursor positioning completes without error, the system shall read the next transaction record from the transaction data store; if end-of-file is reached, the system shall set the end-of-file flag and display the message 'You have reached the bottom of the page...'; if any other error occurs, the system shall set the error flag and display the message 'Unable to lookup transaction...'.

REQ-F-1034: [Event-driven] When backward pagination is requested, the system shall position the transaction data store cursor at the starting point for reverse-order retrieval; if positioning fails, the system shall set the error flag and abort navigation.

REQ-F-1035: [Event-driven] When backward pagination is initiated and cursor positioning succeeds, the system shall read the previous transaction record from the transaction data store; if end-of-file is reached, the system shall set the end-of-file flag and display the message 'You have reached the top of the page...'; if any other error occurs, the system shall set the error flag and display the message 'Unable to lookup transaction...'.

REQ-F-1036: [Event-driven] When the first transaction record is successfully retrieved, the system shall clear all 10 transaction display rows before populating them with the new batch.

REQ-F-1037: [State-driven] While the transaction data store cursor is positioned and the display rows are cleared, the system shall read each transaction record, populate the corresponding display row with transaction ID, date, description, and amount, and continue until 10 rows are filled, end-of-file is reached, or an error occurs.

REQ-F-1038: [Ubiquitous] The system shall extract the transaction amount and parse the transaction timestamp to isolate year (last 2 digits), month, and day components, formatting the date as MM/DD/YY for display on the transaction list screen.

REQ-F-1039: [Event-driven] When the index is 1, the system shall populate the first transaction display row with the transaction ID, formatted date, description, and amount, and store the transaction ID as the first transaction ID in the session context for pagination tracking.

REQ-F-1040: [Event-driven] When the index is 2, the system shall populate the second transaction display row with the transaction ID, formatted date, description, and amount.

REQ-F-1041: [Event-driven] When the index is 3, the system shall populate the third transaction display row with the transaction ID, formatted date, description, and amount.

REQ-F-1042: [Event-driven] When the index is 4, the system shall populate the fourth transaction display row with the transaction ID, formatted date, description, and amount.

REQ-F-1043: [Event-driven] When the index is 5, the system shall populate the fifth transaction display row with the transaction ID, formatted date, description, and amount.

REQ-F-1044: [Event-driven] When the index is 6, the system shall populate the sixth transaction display row with the transaction ID, formatted date, description, and amount.

REQ-F-1045: [Event-driven] When the index is 7, the system shall populate the seventh transaction display row with the transaction ID, formatted date, description, and amount.

REQ-F-1046: [Event-driven] When the index is 8, the system shall populate the eighth transaction display row with the transaction ID, formatted date, description, and amount.

REQ-F-1047: [Event-driven] When the index is 9, the system shall populate the ninth transaction display row with the transaction ID, formatted date, description, and amount.

REQ-F-1048: [Event-driven] When the index is 10, the system shall populate the tenth transaction display row with the transaction ID, formatted date, description, and amount, and store the transaction ID as the last transaction ID in the session context for pagination tracking.

REQ-F-1049: [Unwanted] If the row index is outside the range 1 through 10, the system shall take no action.

REQ-F-1050: [Event-driven] When the transaction batch retrieval loop completes and more transactions remain in the data store, the system shall increment the page counter and read the next record to position for the next page; if no more transactions remain but at least one transaction was displayed, the system shall increment the page counter to mark page completion.

REQ-F-1051: [Complex] While the transaction list has been populated and no error has occurred, when an additional record is successfully read to check for more pages and the next-page flag is set, the system shall decrement the page number by 1 if the current page number is greater than 1; otherwise the system shall set the page number to 1.

REQ-F-1052: [Ubiquitous] The system shall update the page number field on the screen, clear the transaction ID input field, and send the transaction list screen to the user.

REQ-F-1053: [Ubiquitous] The system shall retrieve the current system date and time, format the date as MM/DD/YY and the time as HH:MM:SS, and populate the transaction list screen header with the title lines, transaction identifier, program name, formatted date, and formatted time.

REQ-F-1054: [Ubiquitous] The system shall set the next-page flag to 'N' on program initialization to indicate no additional page.


---


## 71. Transaction Selection from List
As a user, I want to select a transaction from the list so that I can navigate to its detail view.

### Requirements

REQ-F-1055: [Event-driven] When the user enters a non-empty selection indicator in transaction row 1, the system shall record the selection indicator and the corresponding transaction ID in the session context.

REQ-F-1056: [Event-driven] When the user selects transaction option 2, the system shall capture the selection indicator and transaction identifier for option 2 and store them in the session context.

REQ-F-1057: [Event-driven] When the user selects transaction option 3, the system shall capture the selection indicator and transaction identifier for option 3 and store them in the session context.

REQ-F-1058: [Event-driven] When the user selects transaction option 4, the system shall capture the selection indicator and transaction identifier for option 4 and store them in the session context.

REQ-F-1059: [Event-driven] When the user selects transaction option 5, the system shall capture the selection indicator and transaction identifier for option 5 and store them in the session context.

REQ-F-1060: [Event-driven] When the user selects transaction option 6, the system shall capture the selection indicator and transaction identifier for option 6 and store them in the session context.

REQ-F-1061: [Event-driven] When the user selects transaction option 7, the system shall capture the selection indicator and transaction identifier for option 7 and store them in the session context.

REQ-F-1062: [Event-driven] When the user selects transaction option 8, the system shall capture the selection indicator and transaction identifier for option 8 and store them in the session context.

REQ-F-1063: [Event-driven] When the user selects transaction option 9, the system shall capture the selection indicator and transaction identifier for option 9 and store them in the session context.

REQ-F-1064: [Event-driven] When the user selects transaction option 10, the system shall capture the selection indicator and transaction identifier for option 10 and store them in the session context.

REQ-F-1065: [Event-driven] When no valid transaction option is selected (none of the ten selection indicators contain a non-space, non-low-value character), the system shall clear both the transaction selection flag and the transaction identifier in the session context.

REQ-F-1066: [Complex] While the transaction selection flag and selected transaction ID are both populated, when the user enters a transaction ID search value and presses Enter, the system shall validate the selection flag (accept 'S' or 's'; reject other values with the error message 'Invalid selection. Valid value is S'); validate the transaction ID input (accept numeric values or spaces/low-values for beginning-of-file search; reject non-numeric values with the error message 'Tran ID must be Numeric ...'); then reset the page number to 0 and invoke page-forward processing to retrieve and display the first page of matching transactions.

REQ-F-1067: [Event-driven] When the user presses Enter on the transaction list screen, the system shall process the transaction selection and route to the destination program.

REQ-F-1068: [Event-driven] When the program is re-entered and the user submits the transaction list screen, the system shall receive all selection indicators and transaction identifiers for the ten transaction options.


---


## 72. Transaction Option Selection — Data Extraction
As a user navigating the transaction list, I want my selection of a specific transaction option to be captured accurately so that the correct transaction details can be retrieved and displayed.

### Requirements

REQ-F-1069: [Event-driven] When the user selects transaction option 1 on the transaction list screen, the system shall extract the selection indicator and transaction identifier for option 1 from the screen input and store them in the session context (communication area).


---


## 73. Navigation and Program Transfer Control
As an interactive user, I want the system to route me to the correct program based on my navigation context and key presses so that I can move through the card demonstration application seamlessly.

### Requirements

REQ-F-1070: [Event-driven] When the program is invoked with no communication area, the system shall set the destination program to the sign-on screen program and transfer control to it.

REQ-F-1071: [Event-driven] When a communication area is received from the calling program, the system shall unpack the communication area into working storage, preserving the originating program identifier, originating transaction identifier, and re-entry status indicator.

REQ-F-1072: [Complex] While the program is in re-entry status (program context indicator = 1), when the user presses PF3, the system shall route to the menu screen program ('COMEN01C') if the originating program identifier is empty or contains low-values, otherwise route back to the originating program.

REQ-F-1073: [Ubiquitous] The system shall validate the destination program name before transferring control; if the destination program name is empty or contains low-values, the system shall default it to the sign-on screen program ('COSGN00C').

REQ-F-1074: [Ubiquitous] The system shall populate the originating transaction identifier and originating program name with the current program's identifiers, and reset the program context indicator to zero (entry status) before transferring control to the destination program.


---


## 74. Transaction Entry Screen — Key Dispatch and Screen Lifecycle
As an interactive user, I want the transaction entry screen to respond correctly to function key presses so that I can enter, confirm, clear, or copy transactions efficiently.

### Requirements

REQ-F-1075: [Event-driven] When the program is re-invoked with a non-zero communication area length, the system shall restore the communication area from the caller's context and dispatch to the appropriate handler based on the key pressed: Enter to process transaction entry, PF3 to navigate away, PF4 to clear the screen, PF5 to retrieve the previous transaction.

REQ-F-1076: [Event-driven] When the user presses the Enter key on the transaction entry screen, the system shall receive the user input from the screen and delegate to the transaction entry processor to validate and add the transaction.

REQ-F-1077: [Event-driven] When the user presses the PF4 function key on the transaction entry screen, the system shall clear the current screen and redisplay it.

REQ-F-1078: [Event-driven] When the user presses PF5 to copy the last transaction, the system shall validate the key fields, retrieve the last transaction from the transaction data store (AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS), populate the screen with the retrieved transaction's type code, category code, source, amount, description, original date, processing date, merchant ID, merchant name, merchant city, and merchant ZIP, then process the Enter key.

REQ-F-1079: [Unwanted] If the user presses any key other than Enter, PF3, PF4, or PF5 on the transaction entry screen, the system shall set the error message to 'Invalid key pressed. Please see below...' and redisplay the transaction entry screen.

REQ-F-1080: [Ubiquitous] The system shall retrieve the current system date and time, reformat the date to MM/DD/YY and the time to HH:MM:SS, and populate the screen header with the titles, transaction ID code, program name, current date, and current time.

REQ-F-1081: [Ubiquitous] The system shall move any pending message to the error message field and send the transaction entry screen to the user terminal.


---


## 75. Key Field Validation (Account ID and Card Number)
As an interactive user, I want the system to validate that I have provided a valid account ID or card number so that transactions are associated with a known account.

### Requirements

REQ-F-1082: [Event-driven] When the user submits the transaction entry screen without entering either an account ID or a card number, the system shall set the error flag to 'Y', display the error message 'Account or Card Number must be entered...', position the cursor on the account ID field, and redisplay the screen.

REQ-F-1083: [Event-driven] When the user enters an account ID, the system shall validate that the account ID contains only numeric characters; if non-numeric, the system shall set the error flag to 'Y', display the error message 'Account ID must be Numeric...', position the cursor on the account ID field, and redisplay the screen.

REQ-F-1084: [Event-driven] When the account ID is numeric, the system shall look up the associated card number from the card-account index data store (AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH) using the account ID as the key and populate the card number field on the screen with the retrieved value.

REQ-F-1085: [Event-driven] When the account ID lookup in the card-account index data store returns a not-found response, the system shall set the error flag to 'Y', display an error message indicating the account ID was not found, position the cursor on the account ID field, and redisplay the screen.

REQ-F-1086: [Unwanted] If the account ID lookup in the card-account index data store fails with any response other than not-found, the system shall set the error flag to 'Y', display an error message indicating the account lookup failed, position the cursor on the account ID field, and redisplay the screen.

REQ-F-1087: [Event-driven] When the user enters a card number, the system shall validate that the card number contains only numeric characters; if non-numeric, the system shall set the error flag to 'Y', display the error message 'Card Number must be Numeric...', position the cursor on the card number field, and redisplay the screen.

REQ-F-1088: [Event-driven] When the card number is numeric, the system shall look up the associated account ID from the card cross-reference data store (AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS) using the card number as the key and populate the account ID field on the screen with the retrieved value.

REQ-F-1089: [Event-driven] When the card number lookup in the card cross-reference data store returns a not-found response, the system shall set the error flag to 'Y', display an error message indicating the card number was not found, position the cursor on the card number field, and redisplay the screen.

REQ-F-1090: [Unwanted] If the card number lookup in the card cross-reference data store fails with any response other than not-found, the system shall set the error flag to 'Y', display an error message indicating the card lookup failed, position the cursor on the card number field, and redisplay the screen.

REQ-F-1091: [Event-driven] When the error flag is set from a prior key field validation failure, the system shall clear all transaction data input fields (type code, category code, source, amount, description, original date, processing date, merchant ID, merchant name, merchant city, and merchant ZIP) by setting them to spaces.


---


## 76. Transaction Data Field Validation
As an interactive user, I want the system to validate all transaction data fields so that only correctly formatted and complete transactions are accepted.

### Requirements

REQ-F-1092: [Event-driven] When the transaction type code input field is empty or contains only spaces or low-values, the system shall display the error message 'Type CD can NOT be empty...' and redisplay the screen with the cursor positioned on the transaction type code field.

REQ-F-1093: [Event-driven] When the transaction type code input field contains non-numeric characters, the system shall display the error message 'Type CD must be Numeric...' and redisplay the screen with the cursor positioned on the transaction type code field.

REQ-F-1094: [Event-driven] When the transaction category code input field is empty or contains only spaces or low-values, the system shall display the error message 'Category CD can NOT be empty...' and redisplay the screen with the cursor positioned on the transaction category code field.

REQ-F-1095: [Event-driven] When the transaction category code input field contains non-numeric characters, the system shall display the error message 'Category CD must be Numeric...' and redisplay the screen with the cursor positioned on the transaction category code field.

REQ-F-1096: [Event-driven] When the transaction source input field is empty or contains only spaces or low-values, the system shall display the error message 'Source can NOT be empty...' and redisplay the screen with the cursor positioned on the transaction source field.

REQ-F-1097: [Event-driven] When the transaction amount input field is empty or contains only spaces or low-values, the system shall display the error message 'Amount can NOT be empty...' and redisplay the screen with the cursor positioned on the transaction amount field.

REQ-F-1098: [Event-driven] When the transaction amount input field does not conform to the format -99999999.99 (first character must be a sign, characters 2–9 must be numeric, character 10 must be a decimal point, characters 11–12 must be numeric), the system shall display the error message 'Amount should be in format -99999999.99' and redisplay the screen with the cursor positioned on the transaction amount field.

REQ-F-1099: [Ubiquitous] The system shall convert the transaction amount from its input string format to a numeric value using NUMVAL-C, store the result, format it with a leading sign and decimal places, and display the formatted amount on the screen.

REQ-F-1100: [Event-driven] When the transaction description input field is empty or contains only spaces or low-values, the system shall display the error message 'Description can NOT be empty...' and redisplay the screen with the cursor positioned on the transaction description field.

REQ-F-1101: [Event-driven] When the transaction original date input field is empty or contains only spaces or low-values, the system shall display the error message 'Orig Date can NOT be empty...' and redisplay the screen with the cursor positioned on the transaction original date field.

REQ-F-1102: [Event-driven] When the transaction original date input field does not conform to the format YYYY-MM-DD (characters 1–4 numeric, character 5 a hyphen, characters 6–7 numeric, character 8 a hyphen, characters 9–10 numeric), the system shall display the error message 'Orig Date should be in format YYYY-MM-DD' and redisplay the screen with the cursor positioned on the transaction original date field.

REQ-F-1103: [Event-driven] When the transaction original date requires validation against the date utility service, the system shall invoke the date utility service with the original date in YYYY-MM-DD format; if the service returns a severity code other than '0000' and a message number other than '2513', the system shall display the error message 'Orig Date - Not a valid date...' and redisplay the screen with the cursor positioned on the original date field.

REQ-F-1104: [Event-driven] When the transaction processing date input field is empty or contains only spaces or low-values, the system shall display the error message 'Proc Date can NOT be empty...' and redisplay the screen with the cursor positioned on the transaction processing date field.

REQ-F-1105: [Event-driven] When the transaction processing date input field does not conform to the format YYYY-MM-DD (characters 1–4 numeric, character 5 a hyphen, characters 6–7 numeric, character 8 a hyphen, characters 9–10 numeric), the system shall display the error message 'Proc Date should be in format YYYY-MM-DD' and redisplay the screen with the cursor positioned on the transaction processing date field.

REQ-F-1106: [Event-driven] When the transaction processing date requires validation against the date utility service, the system shall invoke the date utility service with the processing date in YYYY-MM-DD format; if the service returns a severity code other than '0000' and a message number other than '2513', the system shall display the error message 'Proc Date - Not a valid date...' and redisplay the screen with the cursor positioned on the processing date field.

REQ-F-1107: [Event-driven] When the merchant ID input field is empty or contains only spaces or low-values, the system shall display the error message 'Merchant ID can NOT be empty...' and redisplay the screen with the cursor positioned on the merchant ID field.

REQ-F-1108: [Event-driven] When the merchant ID input field contains non-numeric characters, the system shall display the error message 'Merchant ID must be Numeric...' and redisplay the screen with the cursor positioned on the merchant ID field.

REQ-F-1109: [Event-driven] When the merchant name input field is empty or contains only spaces or low-values, the system shall display the error message 'Merchant Name can NOT be empty...' and redisplay the screen with the cursor positioned on the merchant name field.

REQ-F-1110: [Event-driven] When the merchant city input field is empty or contains only spaces or low-values, the system shall display the error message 'Merchant City can NOT be empty...' and redisplay the screen with the cursor positioned on the merchant city field.

REQ-F-1111: [Event-driven] When the merchant ZIP code input field is empty or contains only spaces or low-values, the system shall display the error message 'Merchant Zip can NOT be empty...' and redisplay the screen with the cursor positioned on the merchant ZIP code field.


---


## 77. Transaction Confirmation and Persistence
As an interactive user, I want the system to confirm my intent before writing a transaction so that accidental submissions are prevented.

### Requirements

REQ-F-1112: [Event-driven] When the user presses Enter and all validations pass, the system shall evaluate the confirmation field: if the value is 'Y' or 'y', the system shall proceed to add the transaction; if the value is 'N', 'n', spaces, or low-values, the system shall display a message prompting the user to confirm and redisplay the screen with the cursor on the confirmation field; if the value is any other character, the system shall display an error message indicating that only 'Y' or 'N' are valid and redisplay the screen with the cursor on the confirmation field.

REQ-F-1113: [Event-driven] When the user confirms the transaction addition, the system shall position to the end of the transaction data store (AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS) by setting the transaction ID to high-values and starting a browse operation, read the last transaction record to obtain the highest existing transaction ID (TRAN-ID, 16-character alphanumeric), end the browse operation, and increment the highest transaction ID by one to generate a new unique transaction ID.

REQ-F-1114: [Event-driven] When a new transaction ID has been generated, the system shall assemble a transaction record populated with the user-entered values — transaction ID, type code, category code, source, description, amount (converted from edited format to numeric), card number, merchant ID, merchant name, merchant city, merchant ZIP, original date, and processing date — and write the record to the transaction data store.

REQ-F-1115: [Event-driven] When the write to the transaction data store succeeds, the system shall initialize all screen fields and display a success message.

REQ-F-1116: [Unwanted] If the write to the transaction data store fails due to a duplicate key, duplicate record, or any other error, the system shall display an error message indicating the failure condition.


---


## 78. Transaction File Browse Operations
As an interactive user, I want the system to correctly manage browse operations on the transaction data store so that last-transaction retrieval and ID generation work reliably.

### Requirements

REQ-F-1117: [Event-driven] When the program initiates a browse operation on the transaction data store at a specified transaction ID key and the browse start returns a not-found response, the system shall set the error flag to 'Y', display an error message indicating the transaction ID was not found, position the cursor on the account ID field, and redisplay the screen.

REQ-F-1118: [Unwanted] If the browse start on the transaction data store fails with any response other than not-found, the system shall set the error flag to 'Y', display an error message indicating the transaction lookup failed, position the cursor on the account ID field, and redisplay the screen.

REQ-F-1119: [Event-driven] When the program reads the previous record in a backward browse of the transaction data store and the end of file is reached, the system shall set the transaction ID to zeros to indicate no transaction was found.

REQ-F-1120: [Unwanted] If the backward read of the transaction data store fails with any response other than end-of-file, the system shall set the error flag to 'Y', display an error message indicating the transaction lookup failed, position the cursor on the account ID field, and redisplay the screen.

REQ-F-1121: [Ubiquitous] The system shall end the browse operation on the transaction data store after completing any browse-dependent processing.


---


## 79. Screen Initialization on First Entry
As an interactive user, I want the transaction entry screen to initialize correctly on first entry so that I am presented with a clean form ready for input.

### Requirements

REQ-F-1122: [Event-driven] When the program is first entered and a pre-selected transaction exists in the communication area, the system shall populate the card number field with that transaction identifier and process it as an Enter key submission.


---


## 80. Function Key Capture and Mapping
As a user navigating the credit card transaction application, I want my key presses recognized and mapped to business actions so that the correct operation is performed regardless of which physical key I use.

### Requirements

REQ-F-1123: [Ubiquitous] The system shall invoke the function key capture routine to evaluate the user's key press and map it to the corresponding action indicator before processing any business logic.

REQ-F-1124: [Event-driven] When the ENTER key is pressed, the system shall set the ENTER action indicator to active.

REQ-F-1125: [Event-driven] When the CLEAR key is pressed, the system shall set the CLEAR action indicator to active.

REQ-F-1126: [Event-driven] When the PA1 key is pressed, the system shall set the PA1 action indicator to active.

REQ-F-1127: [Event-driven] When the PA2 key is pressed, the system shall set the PA2 action indicator to active.

REQ-F-1128: [Event-driven] When the PF1 key is pressed, the system shall set the PF1 action indicator to active.

REQ-F-1129: [Event-driven] When the PF2 key is pressed, the system shall set the PF2 action indicator to active.

REQ-F-1130: [Event-driven] When the PF3 key is pressed, the system shall set the PF3 action indicator to active.

REQ-F-1131: [Event-driven] When the PF4 key is pressed, the system shall set the PF4 action indicator to active.

REQ-F-1132: [Event-driven] When the PF5 key is pressed, the system shall set the PF5 action indicator to active.

REQ-F-1133: [Event-driven] When the PF6 key is pressed, the system shall set the PF6 action indicator to active.

REQ-F-1134: [Event-driven] When the PF7 key is pressed, the system shall set the PF7 action indicator to active.

REQ-F-1135: [Event-driven] When the PF8 key is pressed, the system shall set the PF8 action indicator to active.

REQ-F-1136: [Event-driven] When the PF9 key is pressed, the system shall set the PF9 action indicator to active.

REQ-F-1137: [Event-driven] When the PF10 key is pressed, the system shall set the PF10 action indicator to active.

REQ-F-1138: [Event-driven] When the PF11 key is pressed, the system shall set the PF11 action indicator to active.

REQ-F-1139: [Event-driven] When the PF12 key is pressed, the system shall set the PF12 action indicator to active.

REQ-F-1140: [Event-driven] When the PF13 key is pressed, the system shall set the PF1 action indicator to active, treating PF13 as equivalent to PF1.

REQ-F-1141: [Event-driven] When the PF14 key is pressed, the system shall set the PF2 action indicator to active, treating PF14 as equivalent to PF2.

REQ-F-1142: [Event-driven] When the PF15 key is pressed, the system shall set the PF3 action indicator to active, treating PF15 as equivalent to PF3 (exit).

REQ-F-1143: [Event-driven] When the PF16 key is pressed, the system shall set the PF4 action indicator to active, treating PF16 as equivalent to PF4.

REQ-F-1144: [Event-driven] When the PF17 key is pressed, the system shall set the PF5 action indicator to active, treating PF17 as equivalent to PF5.

REQ-F-1145: [Event-driven] When the PF18 key is pressed, the system shall set the PF6 action indicator to active, treating PF18 as equivalent to PF6.

REQ-F-1146: [Event-driven] When the PF19 key is pressed, the system shall set the PF7 action indicator to active, treating PF19 as equivalent to PF7 (page up).

REQ-F-1147: [Event-driven] When the PF20 key is pressed, the system shall set the PF8 action indicator to active, treating PF20 as equivalent to PF8 (page down).

REQ-F-1148: [Event-driven] When the PF21 key is pressed, the system shall set the PF9 action indicator to active, treating PF21 as equivalent to PF9.

REQ-F-1149: [Event-driven] When the PF22 key is pressed, the system shall set the PF10 action indicator to active, treating PF22 as equivalent to PF10.

REQ-F-1150: [Event-driven] When the PF23 key is pressed, the system shall set the PF11 action indicator to active, treating PF23 as equivalent to PF11.

REQ-F-1151: [Event-driven] When the PF24 key is pressed, the system shall set the PF12 action indicator to active, treating PF24 as equivalent to PF12.


---


## 81. Function Key Validation
As a user of the transaction listing workflow, I want only valid key presses to trigger actions so that invalid key sequences do not cause unintended operations.

### Requirements

REQ-F-1152: [Event-driven] When a key press is received, the system shall validate it against the allowed set for the current processing state; the key is valid if it is ENTER, PF2, PF3, PF7 (page up), PF8 (page down), PF10 while a delete operation is requested, or PF10 while an update operation is requested.

REQ-F-1153: [Unwanted] If the pressed key is not in the allowed set for the current processing state, the system shall reset the action to ENTER to return to the default list view.


---


## 82. State Reset on External Entry
As a user entering the transaction listing from another part of the application, I want the screen state to be cleared so that previous session data does not interfere with a fresh retrieval.

### Requirements

REQ-F-1154: [Event-driven] When the program is entered from a different program (indicated by the program context being initial entry and the originating program name differing from the current program name), the system shall reset the program-specific working state and force the action to ENTER to initiate a fresh list retrieval.

REQ-F-1155: [Event-driven] When PF3 (exit) is pressed while the originating transaction is the additional transaction, the system shall reset the program-specific working state and force the action to ENTER.


---


## 83. PF3 Exit Navigation
As a user, I want pressing PF3 to return me to the appropriate destination so that I can navigate back through the application correctly.

### Requirements

REQ-F-1156: [Event-driven] When PF3 (exit) is pressed and the originating transaction identifier is empty, spaces, or matches the current transaction, the system shall set the destination to the admin transaction ('CA00') and admin program ('COADM01C').

REQ-F-1157: [Event-driven] When PF3 (exit) is pressed and the originating transaction identifier is populated with a different transaction, the system shall set the destination to the originating transaction and originating program, allowing the user to return to where they came from.

REQ-F-1158: [Event-driven] When PF3 (exit) is pressed, the system shall update the navigation context by recording the current transaction identifier and program name as the originating context, mark the user as administrator, set the program context to initial entry, record the current mapset and map names, and transfer control to the determined destination program passing the updated session context.


---


## 84. PF2 Add-Transaction Navigation
As a user viewing the transaction listing, I want pressing PF2 to navigate me to the add-transaction screen so that I can enter a new transaction.

### Requirements

REQ-F-1159: [Event-driven] When PF2 is pressed and the originating program is the current program (indicating the user is already in the transaction listing), the system shall set the session context with the originating transaction identifier set to the current transaction identifier ('CTLI'), the originating program name set to the current program name ('COTRTLIC'), the user type set to regular user, the last mapset name set to 'COTRTLI', the last map name set to 'CTRTLIA', and the destination program set to 'COTRTUPC'; mark the context as entry/initialization; and transfer control to the add-transaction program ('COTRTUPC') passing the updated session context.


---


## 85. Transaction Type List Management and Display
As a user, I want to browse, filter, and act on a paginated list of transaction types so that I can select records for update or delete operations.


**Function Key Capture**

### Requirements

REQ-F-1160: [Event-driven] When the ENTER key is pressed, the system shall set the ENTER function key indicator to active.

REQ-F-1161: [Event-driven] When the CLEAR key is pressed, the system shall set the CLEAR function key indicator to active.

REQ-F-1162: [Event-driven] When the PA1 key is pressed, the system shall set the PA1 function key indicator to active.

REQ-F-1163: [Event-driven] When the PA2 key is pressed, the system shall set the PA2 function key indicator to active.

REQ-F-1164: [Event-driven] When the PF1 key is pressed, the system shall set the PF1 function key indicator to active.

REQ-F-1165: [Event-driven] When the PF2 key is pressed, the system shall set the PF2 function key indicator to active.

REQ-F-1166: [Event-driven] When the PF3 key is pressed, the system shall set the PF3 function key indicator to active.

REQ-F-1167: [Event-driven] When the PF4 key is pressed, the system shall set the PF4 function key indicator to active.

REQ-F-1168: [Event-driven] When the PF5 key is pressed, the system shall set the PF5 function key indicator to active.

REQ-F-1169: [Event-driven] When the PF6 key is pressed, the system shall set the PF6 function key indicator to active.

REQ-F-1170: [Event-driven] When the PF7 key is pressed, the system shall set the PF7 function key indicator to active.

REQ-F-1171: [Event-driven] When the PF8 key is pressed, the system shall set the PF8 function key indicator to active.

REQ-F-1172: [Event-driven] When the PF9 key is pressed, the system shall set the PF9 function key indicator to active.

REQ-F-1173: [Event-driven] When the PF10 key is pressed, the system shall set the PF10 function key indicator to active.

REQ-F-1174: [Event-driven] When the PF11 key is pressed, the system shall set the PF11 function key indicator to active.

REQ-F-1175: [Event-driven] When the PF12 key is pressed, the system shall set the PF12 function key indicator to active.

REQ-F-1176: [Event-driven] When the PF13 key is pressed, the system shall set the PF1 function key indicator to active.

REQ-F-1177: [Event-driven] When the PF14 key is pressed, the system shall set the PF2 function key indicator to active.

REQ-F-1178: [Event-driven] When the PF15 key is pressed, the system shall set the PF3 function key indicator to active.

REQ-F-1179: [Event-driven] When the PF16 key is pressed, the system shall set the PF4 function key indicator to active.

REQ-F-1180: [Event-driven] When the PF17 key is pressed, the system shall set the PF5 function key indicator to active.

REQ-F-1181: [Event-driven] When the PF18 key is pressed, the system shall set the PF6 function key indicator to active.

REQ-F-1182: [Event-driven] When the PF19 key is pressed, the system shall set the PF7 function key indicator to active.

REQ-F-1183: [Event-driven] When the PF20 key is pressed, the system shall set the PF8 function key indicator to active.

REQ-F-1184: [Event-driven] When the PF21 key is pressed, the system shall set the PF9 function key indicator to active.

REQ-F-1185: [Event-driven] When the PF22 key is pressed, the system shall set the PF10 function key indicator to active.

REQ-F-1186: [Event-driven] When the PF23 key is pressed, the system shall set the PF11 function key indicator to active.

REQ-F-1187: [Event-driven] When the PF24 key is pressed, the system shall set the PF12 function key indicator to active.

REQ-F-1188: [Ubiquitous] The system shall evaluate the terminal attention identifier and dispatch to the corresponding function key handler.

REQ-F-1189: [Ubiquitous] The system shall invoke the function key capture routine before processing any business logic.

REQ-F-1190: [Event-driven] When a function key is pressed, the system shall validate that the pressed key is one of ENTER, PF2, PF3, PF7, PF8, or PF10 (when a delete or update action is pending); if the pressed key is not in this valid set, the system shall mark the key as invalid and force the key to ENTER as the default action.

REQ-F-1191: [Event-driven] When the user submits the screen, the system shall receive the screen input and extract the transaction type code filter, transaction type description filter, and row selection action flags for each of the 7 displayed rows; for each row, if the user entered an asterisk or spaces in the row description field, the field shall be left blank; otherwise, the trimmed description shall be extracted.

REQ-F-1192: [Event-driven] When the user enters a transaction type code filter, the system shall validate that the filter is either blank or a numeric 2-digit value; if blank (low-values, spaces, or zeros), the system shall clear the filter to zeros; if non-blank and non-numeric, the system shall set an input error, protect row selection, and display the error message 'TYPE CODE FILTER,IF SUPPLIED MUST BE A 2 DIGIT NUMBER'; if numeric, the system shall accept it as valid.

REQ-F-1193: [Event-driven] When the transaction type code filter is validated, the system shall compare the current filter to the previous filter; if they match (or both are blank), the system shall mark the filter as unchanged; if they differ, the system shall reinitialize pagination variables and mark the filter as changed.

REQ-F-1194: [Event-driven] When the user enters a transaction type description filter, the system shall validate that the filter is either blank or a non-blank string; if blank (low-values or spaces), the system shall mark it as blank; if non-blank, the system shall mark it as valid and wrap it with SQL LIKE wildcards (% prefix and % suffix).

REQ-F-1195: [Event-driven] When the transaction type description filter is validated, the system shall compare the current filter to the previous filter; if they match (or both are blank), the system shall mark the filter as unchanged; if they differ, the system shall reinitialize pagination variables and mark the filter as changed.

REQ-F-1196: [Ubiquitous] The system shall reset the total actions requested count, no-actions-selected count, deletes-requested count, updates-requested count, and valid-actions-selected count to zero, and clear the selection-action flags array at the start of row-selection validation.

REQ-F-1197: [Event-driven] When the transaction type filter and description filter have not changed, the system shall count rows with no action selected into the no-actions-selected count, count delete-action rows (marked 'D') into the deletes-requested count, count update-action rows (marked 'U') into the updates-requested count, compute total actions requested as 7 minus the no-actions-selected count, and compute valid-actions-selected as the sum of deletes-requested and updates-requested.

REQ-F-1198: [State-driven] While filter criteria have not changed and row selections are being validated, the system shall iterate through each of the 7 screen rows in reverse order; for each row with a valid action (delete or update), record its subscript as the selected row; if more than one total action has been requested, mark that row's selection as an error and set the bad-actions-selected flag; if an update action is requested, delegate to the row-description validation step; skip rows with blank or low-value selections; for any row with an invalid selection value, mark it as an error and set the bad-actions-selected flag.

REQ-F-1199: [Event-driven] When row selection validation is complete, the system shall compare the currently selected row subscript with the previously selected row number; if they are equal, the system shall set the row-selection-changed flag to no-change; if they differ, the system shall set the row-selection-changed flag to changed.

REQ-F-1200: [Event-driven] When a row is selected for update, the system shall compare the trimmed, uppercase row description input with the trimmed, uppercase row description output from the previous screen; if identical, the system shall mark as no changes; if different, the system shall mark as changes detected; the system shall then validate the description field for non-blank alphanumeric content.

REQ-F-1201: [Event-driven] When a row description field is validated during an update operation, the system shall validate that the field is either blank or contains non-blank content; if blank (low-values, spaces, or zero trimmed length), the system shall mark it as blank; if non-blank, the system shall mark it as valid.

REQ-F-1202: [Event-driven] When database connectivity is checked, the system shall execute a priming query to verify database connectivity; if the query fails, the system shall set the database error flag and format an error message with description 'Db2 access failure.'.

REQ-F-1203: [Ubiquitous] The system shall clear the output buffer and set pagination flags to begin a forward scan of transaction type records before reading records in forward order.

REQ-F-1204: [Ubiquitous] The system shall reset the row counter to zero and set the loop-control flag to indicate records remain to be processed before entering the fetch loop.

REQ-F-1205: [Event-driven] When forward pagination is initiated, the system shall open the forward cursor over the transaction type data store; if the open fails, the system shall set the database error flag and format an error message.

REQ-F-1206: [Event-driven] When a transaction type record is successfully retrieved from the transaction type data store, the system shall increment the row counter, populate the transaction type code and description into the output buffer at the current row position, and if this is the first record on a new page and the screen number is zero, increment the screen number to 1.

REQ-F-1207: [Event-driven] When the initial fetch returns end-of-data (SQL return code +100), the system shall exit the fetch loop, set the next-page indicator to no more records, set a 'no more records' message if PF8 was pressed and no message is currently displayed, and if this is the first page with no rows populated, set a 'no records found' message.

REQ-F-1208: [Unwanted] If the initial fetch encounters a database error, the system shall exit the fetch loop and, if no message is currently displayed, record the action description 'C-TR-TYPE-FORWARD close' and delegate to the database message formatter to format and display the error details.

REQ-F-1209: [Complex] While the current screen row counter has reached the maximum capacity of 7 rows, when the system probes the transaction type data store to determine whether additional pages exist, the system shall exit the read loop and probe the forward cursor for the next record; if a record is found, the system shall set the next-page indicator to exists; if no more records are found and the user pressed PF8 with no current message, the system shall set an informational message indicating no more records; if a database error occurs and no message is currently displayed, the system shall record the fetch action and delegate to the database message formatter to format the error.

REQ-F-1210: [Event-driven] When forward record retrieval is complete, the system shall close the forward cursor over the transaction type data store; if the close fails, the system shall format an error message.

REQ-F-1211: [Ubiquitous] The system shall close the forward cursor and release database resources after the fetch loop completes.

REQ-F-1212: [Ubiquitous] The system shall clear the output communication area, set the row counter to 7, and initialize flags to indicate records are available and the read loop should continue before reading records in reverse order.

REQ-F-1213: [Ubiquitous] The system shall invoke the backward cursor open procedure to position the cursor for reading transaction type records in reverse order.

REQ-F-1214: [Event-driven] When backward pagination is initiated, the system shall open the backward cursor over the transaction type data store; if the open fails, the system shall format an error message.

REQ-F-1215: [State-driven] While the backward cursor is open and positioned for reading, the system shall fetch each record from the transaction type data store into the buffer; when a record is successfully retrieved, the system shall place its code and description into the output row at the current position and decrement the row counter; when the row counter reaches 0 or a fetch error occurs, the system shall exit the loop.

REQ-F-1216: [Unwanted] If a fetch operation from the backward cursor fails or returns no more rows, the system shall exit the read loop; if no return message is already set, the system shall record the error action description 'Error on fetch Cursor C-TR-TYPE-BACKWARD' and delegate to the database message formatter to format the SQL error for display.

REQ-F-1217: [Event-driven] When backward record retrieval is complete, the system shall close the backward cursor over the transaction type data store; if the close fails, the system shall format an error message.

REQ-F-1218: [Ubiquitous] The system shall close the backward cursor to release database resources after the backward fetch loop completes.

REQ-F-1219: [State-driven] While rows remain to be processed in the screen array (loop counter 1 to 7), the system shall iterate through each row and check whether the row output data is populated; skip processing for empty rows and proceed to action evaluation for populated rows.

REQ-F-1220: [Ubiquitous] The system shall copy the transaction type code from the communication area row output to the corresponding row position in the screen output for the current row.

REQ-F-1221: [Complex] While an update action is in progress on the current row, when data changes have occurred in the row, the system shall display an asterisk character when the description field is blank, or display the user-entered description when the field is not blank; when no changes have occurred or when no update is in progress, the system shall display the transaction type description from the communication area output.

REQ-F-1222: [Ubiquitous] The system shall complete the iteration through all screen rows and terminate the loop.

REQ-F-1223: [Event-driven] When an update action is requested on the row, exactly one valid action is selected, and no conflicting actions are present, the system shall clear the row selection flag to blank when the update operation has completed successfully; if the update has not completed, the system shall leave the flag unchanged.

REQ-F-1224: [Event-driven] When the user presses PF8 (Page Down) and more pages of transaction records exist, the system shall increment the current screen number by one, retrieve forward-ordered transaction records from the transaction type data store, clear the row selection action flags array, and display the updated screen.

REQ-F-1225: [Event-driven] When the user presses PF7 (Page Up) and the current screen is not the first page, the system shall decrement the current screen number by one, retrieve backward-ordered transaction records from the transaction type data store, clear the row selection action flags array, and display the updated screen.

REQ-F-1226: [Event-driven] When the user presses PF7 (Page Up) while on the first page, the system shall retrieve forward-ordered transaction records from the transaction type data store and display the current screen.

REQ-F-1227: [Event-driven] When the user presses Enter with one or more delete actions requested and the originating program is the current program, the system shall retrieve forward-ordered transaction records from the transaction type data store if both the transaction type filter and description filter are valid, then display the screen.

REQ-F-1228: [Event-driven] When the user presses Enter with one or more update actions requested and the originating program is the current program, the system shall retrieve forward-ordered transaction records from the transaction type data store if both the transaction type filter and description filter are valid, then display the screen.

REQ-F-1229: [Event-driven] When the user presses PF10 (Confirm) with one or more delete actions requested and the originating program is the current program, the system shall delegate to the delete-record handler to remove the selected transaction record from the transaction type data store, set the deleted-yes flag if the delete succeeded or the deleted-no flag otherwise, and display the updated screen.

REQ-F-1230: [Event-driven] When the user presses PF10 (Confirm) with one or more update actions requested and the originating program is the current program, the system shall delegate to the update-record handler to modify the selected transaction record in the transaction type data store, set the update-completed flag if the update succeeded, retrieve forward-ordered transaction records, and display the updated screen.

REQ-F-1231: [Event-driven] When input validation fails on the transaction type screen, the system shall record the originating program name in the communication area, retrieve forward-ordered transaction records from the transaction type data store if both the transaction type filter and description filter are valid, and display the updated screen.

REQ-F-1232: [Event-driven] When user input does not match any recognized function key or action combination, the system shall retrieve forward-ordered transaction records from the transaction type data store and display the screen.

REQ-F-1233: [Event-driven] When a transaction type record update is requested, the system shall evaluate the SQL return code; if successful (SQLCODE = 0), the system shall commit the transaction and set the update-succeeded flag; if the record is not found (SQLCODE = +100), the system shall set the update-requested flag and format error message 'Record not found. Deleted by others ? '; if a deadlock occurs (SQLCODE = -911), the system shall set the update-requested flag, set an input error, and format error message 'Deadlock. Someone else updating ?'; if any other error (SQLCODE < 0), the system shall set the update-requested flag and format error message 'Update failed with'.

REQ-F-1234: [Event-driven] When a transaction type record delete is requested, the system shall evaluate the SQL return code; if successful (SQLCODE = 0), the system shall commit the transaction and set the delete-succeeded flag; if a referential integrity constraint violation occurs (SQLCODE = -532), the system shall set the delete-requested flag and format error message 'Please delete associated child records first:'; if any other error, the system shall format error message 'Delete failed with message:'.

REQ-F-1235: [Event-driven] When a database error occurs and needs to be formatted, the system shall delegate to the database error message formatter to format the error message; if the formatter succeeds (return code = 0), the system shall use the formatted message; otherwise, the system shall set error indicator 'DSNTIAC CD:'; the system shall then construct the final error message by concatenating the current action description, SQL code, and formatted message, and store it in the return message text.

REQ-F-1236: [Event-driven] When a record update is completed, the system shall set the information message to 'HIGHLIGHTED row was updated'.

REQ-F-1237: [Event-driven] When a record deletion is completed, the system shall set the information message to 'HIGHLIGHTED row deleted.Hit Enter to continue'.

REQ-F-1238: [Event-driven] When a next page of results exists, the system shall set the information message to 'Type U to update, D to delete any record'.

REQ-F-1239: [Event-driven] When the user presses PF8 when no next page exists and no information message is set, the system shall set the information message to 'Type U to update, D to delete any record'.

REQ-F-1240: [Event-driven] When the user presses PF7 while on the first page, the system shall set the return message to 'No previous pages to display'.

REQ-F-1241: [Event-driven] When the user presses Enter with exactly one valid delete action selected and no filter changes, the system shall set the information message to 'Delete HIGHLIGHTED row ? Press F10 to confirm'.

REQ-F-1242: [Event-driven] When the user presses Enter with exactly one valid update action selected and no filter changes, the system shall set the information message to 'Update HIGHLIGHTED row. Press F10 to save'.

REQ-F-1243: [Event-driven] When transaction type filter validation fails, the system shall take no message action.

REQ-F-1244: [Event-driven] When transaction description filter validation fails, the system shall take no message action.

REQ-F-1245: [Unwanted] If none of the preceding message-setup conditions apply, the system shall clear the information message.

REQ-F-1246: [Ubiquitous] The system shall calculate the center-justified position for the information message text within the 45-character information message field and move the trimmed message to the formatted output buffer at the calculated center position.

REQ-F-1247: [Ubiquitous] The system shall move the return message text to the screen error message output field.

REQ-F-1248: [Event-driven] When input validation is successful and no specific navigation keys are pressed or no actions are pending, the system shall position the cursor to the transaction type filter field by setting its length to -1.


### Non-Functional Requirements

REQ-N-002: [Event-driven] When a transaction type record update succeeds (SQLCODE = 0), the system shall commit the transaction as an atomic unit before setting the update-succeeded flag.

REQ-N-003: [Event-driven] When a transaction type record delete succeeds (SQLCODE = 0), the system shall commit the transaction as an atomic unit before setting the delete-succeeded flag.


---


## 86. Transaction Type Record Maintenance
As a user, I want to update or delete transaction type records so that the transaction type data store reflects current business definitions.

### Requirements

REQ-F-1249: [Event-driven] When a function key is pressed, the system shall validate the pressed key against the allowed set (ENTER, PF2, PF3, PF7, PF8, and PF10 when a delete or update action is pending); if the pressed key is not in this valid set, the system shall reset the attention identifier to ENTER.

REQ-F-1250: [Event-driven] When the user presses one of the programmable function keys PF1 through PF12, the system shall set the corresponding action flag (PFK01 through PFK12) matching the pressed function key.

REQ-F-1251: [Event-driven] When the user presses an extended function key code (PF13 through PF24), the system shall map the extended code to its primary equivalent and set the corresponding action flag (PFK01 through PFK12).

REQ-F-1252: [Event-driven] When the user presses the ENTER key, the system shall set the ENTER action flag; when the user presses the CLEAR key, the system shall set the CLEAR action flag.

REQ-F-1253: [Ubiquitous] The system shall evaluate the terminal attention identifier and dispatch to the corresponding function key handler.

REQ-F-1254: [Event-driven] When the user submits the screen form, the system shall extract the transaction type code filter, description filter, and row selection array (up to 7 rows); for each row, the system shall capture the selection action flag, transaction type code, and description; descriptions that are blank or marked with an asterisk shall be skipped; otherwise, the description shall be trimmed of leading and trailing spaces.

REQ-F-1255: [Ubiquitous] The system shall execute row selection validation, description filter validation, transaction type code filter validation, and cross-field validation in sequence.

REQ-F-1256: [Event-driven] When the row selection flags are received from the screen, the system shall count delete actions (flag = 'D'), update actions (flag = 'U'), and blank selections; identify the last selected row by iterating in reverse; if the selected row differs from the previously selected row, the system shall set the row selection change flag; otherwise, the system shall clear it.

REQ-F-1257: [Event-driven] When the transaction type code filter is received from the screen, the system shall accept a blank filter as valid; if supplied, the system shall verify it is numeric and 2 characters; if invalid, the system shall set an input error and record message 'TYPE CODE FILTER,IF SUPPLIED MUST BE A 2 DIGIT NUMBER'; if valid, the system shall store it for query; if the filter has changed from the previous invocation, the system shall reset paging variables.

REQ-F-1258: [Event-driven] When the user enters a transaction type description filter, the system shall validate that the filter is either blank or a non-blank string; if blank, the system shall mark it as blank; if non-blank, the system shall mark it as valid and wrap it with SQL LIKE wildcards (% prefix and % suffix); if the filter has changed from the previous screen, the system shall reinitialize pagination variables.

REQ-F-1259: [Ubiquitous] The system shall execute a priming query to verify database access; if the query fails, the system shall set the database error flag.

REQ-F-1260: [Event-driven] When the filter criteria are validated and ready for query, the system shall query the transaction type data store to count records matching the applied filters; if the query succeeds, the system shall store the count; if the query fails, the system shall set an input error and format a database error message.

REQ-F-1261: [Complex] While database connectivity is confirmed and input validation is complete, when the user presses PF10 with delete actions pending, the system shall execute the delete operation; when the user presses PF10 with update actions pending, the system shall execute the update operation.

REQ-F-1262: [Event-driven] When the user presses PF10 to confirm an update action, the system shall retrieve the selected row's transaction type code and description from the screen input array, trim the description, and update the record in the transaction type data store identified by the transaction type code with the new description.

REQ-F-1263: [Event-driven] When the user presses PF10 to confirm a delete action, the system shall retrieve the selected row's transaction type code from the screen input array and delete the corresponding record from the transaction type data store.

REQ-F-1264: [Event-driven] When a database operation fails, the system shall delegate to the database error message formatter to construct a human-readable error message from the SQL communication area.

REQ-F-1265: [Event-driven] When the user navigates from a different program or presses PF3 from the add-transaction screen, the system shall clear the program communication area, reset to initial entry mode, and activate the first page indicator.

REQ-F-1266: [Event-driven] When the program is re-invoked with a communication area from itself, the system shall receive and process screen input.


---


## 87. Administration Menu Screen Display
As an administrator, I want to view and interact with the administration menu so that I can select and navigate to administrative functions.

### Requirements

REQ-F-1267: [Ubiquitous] The system shall receive the administrator menu screen map and capture the response code and reason code.

REQ-F-1268: [Ubiquitous] The system shall populate the screen header and menu options, move the message text to the error message display field, and send the fully assembled administration menu screen to the terminal, erasing any previous content.

REQ-F-1269: [Event-driven] When the administration menu program is invoked with no prior session context, the system shall send the menu screen immediately.

REQ-F-1270: [Event-driven] When the administration menu program is re-entered and not yet in re-entry mode, the system shall send the menu screen with a cleared output area.

REQ-F-1271: [Event-driven] When the administration menu program is re-entered in re-entry mode and the user presses Enter, the system shall route to option processing.

REQ-F-1272: [Event-driven] When the administration menu program is re-entered in re-entry mode and the user presses PF3, the system shall route to the exit path.

REQ-F-1273: [Event-driven] When the administration menu program is re-entered in re-entry mode and the user presses any key other than Enter or PF3, the system shall place the standard invalid-key message in the message text and re-display the menu screen.

REQ-F-1274: [Event-driven] When the user presses Enter on the administration menu screen, the system shall trim and normalize the option input, convert it to a numeric option number, and display it on screen; if the value is non-numeric, out of range (greater than the admin option count), or zero, the system shall set the error flag active, display the message 'Please enter a valid option number...', and re-show the menu; if the value is valid but the target program is not installed, the system shall display 'This option is not installed ...' and re-show the menu.


---


## 88. Administrator Menu Option Selection and Navigation
As an administrator, I want to select a numbered menu option so that I can navigate to the corresponding administrative function.

### Requirements

REQ-F-1275: [Ubiquitous] The system shall receive the administrator menu screen map and capture the response code and reason code.

REQ-F-1276: [Complex] While the program is re-entered (program context indicator is 1), when the ENTER key is pressed, the system shall receive the administrator menu screen input and process the selected option.

REQ-F-1277: [Event-driven] When the user submits a menu option selection, the system shall extract and trim the option input, normalize spaces to zeros, convert to numeric, and validate that the option is numeric, within range 1–6, and not zero; if any validation fails, the system shall set the error flag indicator to 'Y'.


---


## 89. Signon Screen Navigation Control
As a user, I want navigation controls to return me to the signon screen so that I can exit the current context safely.

### Requirements

REQ-F-1278: [Event-driven] When the program is invoked with no communication area, the system shall transfer control to the signon screen program.

REQ-F-1279: [Event-driven] When the program is invoked with a communication area present, the system shall load the communication area data into the local record structure.

REQ-F-1280: [Event-driven] When the user presses the PF3 key, the system shall set the destination program to the signon screen program and transfer control to that program.

REQ-F-1281: [Event-driven] When the destination program name is empty or unset (contains low-values or spaces), the system shall default the destination program name to the signon screen program.

REQ-F-1282: [Ubiquitous] The system shall transfer control to the destination program specified in the destination program name field.


---


## 90. Transaction Type Database Update and Insert
As a user maintaining transaction type records, I want the system to save my changes to the transaction type data store so that transaction type codes and descriptions remain accurate and up to date.

### Requirements

REQ-F-1283: [Ubiquitous] The system shall invoke the key-mapping routine to capture and store the terminal input key pressed by the user.

REQ-F-1284: [Event-driven] When the attention identifier matches the ENTER key code, the system shall set the ENTER action indicator to active.

REQ-F-1285: [Event-driven] When the attention identifier matches the CLEAR key code, the system shall set the CLEAR action indicator to active.

REQ-F-1286: [Event-driven] When the attention identifier matches the PF1 key code, the system shall set the PF1 action indicator to active.

REQ-F-1287: [Event-driven] When the attention identifier matches the PF2 key code, the system shall set the PF2 action indicator to active.

REQ-F-1288: [Event-driven] When the attention identifier matches the PF3 key code, the system shall set the PF3 action indicator to active.

REQ-F-1289: [Event-driven] When the attention identifier matches the PF4 key code, the system shall set the PF4 action indicator to active.

REQ-F-1290: [Event-driven] When the attention identifier matches the PF5 key code, the system shall set the PF5 action indicator to active.

REQ-F-1291: [Event-driven] When the attention identifier matches the PF6 key code, the system shall set the PF6 action indicator to active.

REQ-F-1292: [Event-driven] When the attention identifier matches the PF7 key code, the system shall set the PF7 action indicator to active.

REQ-F-1293: [Event-driven] When the attention identifier matches the PF8 key code, the system shall set the PF8 action indicator to active.

REQ-F-1294: [Event-driven] When the attention identifier matches the PF9 key code, the system shall set the PF9 action indicator to active.

REQ-F-1295: [Event-driven] When the attention identifier matches the PF10 key code, the system shall set the PF10 action indicator to active.

REQ-F-1296: [Event-driven] When the attention identifier matches the PF11 key code, the system shall set the PF11 action indicator to active.

REQ-F-1297: [Event-driven] When the attention identifier matches the PF12 key code, the system shall set the PF12 action indicator to active.

REQ-F-1298: [Event-driven] When the attention identifier matches the PA1 key code, the system shall set the PA1 action indicator to active.

REQ-F-1299: [Event-driven] When the attention identifier matches the PA2 key code, the system shall set the PA2 action indicator to active.

REQ-F-1300: [Event-driven] When the attention identifier matches the PF13 key code, the system shall set the PF1 action indicator to active.

REQ-F-1301: [Event-driven] When the attention identifier matches the PF14 key code, the system shall set the PF2 action indicator to active.

REQ-F-1302: [Event-driven] When the attention identifier matches the PF15 key code, the system shall set the PF3 action indicator to active.

REQ-F-1303: [Event-driven] When the attention identifier matches the PF16 key code, the system shall set the PF4 action indicator to active.

REQ-F-1304: [Event-driven] When the attention identifier matches the PF17 key code, the system shall set the PF5 action indicator to active.

REQ-F-1305: [Event-driven] When the attention identifier matches the PF18 key code, the system shall set the PF6 action indicator to active.

REQ-F-1306: [Event-driven] When the attention identifier matches the PF19 key code, the system shall set the PF7 action indicator to active.

REQ-F-1307: [Event-driven] When the attention identifier matches the PF20 key code, the system shall set the PF8 action indicator to active.

REQ-F-1308: [Event-driven] When the attention identifier matches the PF21 key code, the system shall set the PF9 action indicator to active.

REQ-F-1309: [Event-driven] When the attention identifier matches the PF22 key code, the system shall set the PF10 action indicator to active.

REQ-F-1310: [Event-driven] When the attention identifier matches the PF23 key code, the system shall set the PF11 action indicator to active.

REQ-F-1311: [Event-driven] When the attention identifier matches the PF24 key code, the system shall set the PF12 action indicator to active.

REQ-F-1312: [Ubiquitous] The system shall complete the evaluation of all terminal input keys and close the mapping dispatch.

REQ-F-1313: [Ubiquitous] The system shall copy the new transaction type code and description into SQL host variables and compute the description length in preparation for a write to the transaction type data store.

REQ-F-1314: [Complex] While transaction type data has been prepared in SQL host variables, when the UPDATE statement executes against the transaction type data store, the system shall update the transaction type record when the SQL code is 0, invoke the insert routine when the SQL code is +100 (no matching record found), and handle database errors when the SQL code is -911 or any other negative value.

REQ-F-1315: [Event-driven] When the UPDATE statement returns SQL code +100 (no matching record), the system shall insert a new transaction type record into the transaction type data store using the prepared transaction type code and description host variables.

REQ-F-1316: [Event-driven] When the user presses PF5 while transaction type changes are pending confirmation, the system shall invoke the transaction type update processing routine.

REQ-F-1317: [Event-driven] When the user presses PF12 during detail display or record creation, or when transaction type changes are confirmed, failed, or backed out, or when deletion completes or fails, the system shall set the program context to entry mode and mark transaction type details as not fetched.


---


## 91. Transaction Type Deletion from Database
As a user maintaining transaction type records, I want the system to delete a transaction type record from the transaction type data store upon confirmed request so that obsolete transaction types are removed.

### Requirements

REQ-F-1318: [Ubiquitous] The system shall invoke the key-mapping routine to capture and store the terminal input key pressed by the user.

REQ-F-1319: [Event-driven] When the attention identifier matches the ENTER key code, the system shall set the ENTER action indicator to active.

REQ-F-1320: [Event-driven] When the attention identifier matches the CLEAR key code, the system shall set the CLEAR action indicator to active.

REQ-F-1321: [Event-driven] When the attention identifier matches the PF1 key code, the system shall set the PF1 action indicator to active.

REQ-F-1322: [Event-driven] When the attention identifier matches the PF2 key code, the system shall set the PF2 action indicator to active.

REQ-F-1323: [Event-driven] When the attention identifier matches the PF3 key code, the system shall set the PF3 action indicator to active.

REQ-F-1324: [Event-driven] When the attention identifier matches the PF4 key code, the system shall set the PF4 action indicator to active.

REQ-F-1325: [Event-driven] When the attention identifier matches the PF5 key code, the system shall set the PF5 action indicator to active.

REQ-F-1326: [Event-driven] When the attention identifier matches the PF6 key code, the system shall set the PF6 action indicator to active.

REQ-F-1327: [Event-driven] When the attention identifier matches the PF7 key code, the system shall set the PF7 action indicator to active.

REQ-F-1328: [Event-driven] When the attention identifier matches the PF8 key code, the system shall set the PF8 action indicator to active.

REQ-F-1329: [Event-driven] When the attention identifier matches the PF9 key code, the system shall set the PF9 action indicator to active.

REQ-F-1330: [Event-driven] When the attention identifier matches the PF10 key code, the system shall set the PF10 action indicator to active.

REQ-F-1331: [Event-driven] When the attention identifier matches the PF11 key code, the system shall set the PF11 action indicator to active.

REQ-F-1332: [Event-driven] When the attention identifier matches the PF12 key code, the system shall set the PF12 action indicator to active.

REQ-F-1333: [Event-driven] When the attention identifier matches the PA1 key code, the system shall set the PA1 action indicator to active.

REQ-F-1334: [Event-driven] When the attention identifier matches the PA2 key code, the system shall set the PA2 action indicator to active.

REQ-F-1335: [Event-driven] When the attention identifier matches the PF13 key code, the system shall set the PF1 action indicator to active.

REQ-F-1336: [Event-driven] When the attention identifier matches the PF14 key code, the system shall set the PF2 action indicator to active.

REQ-F-1337: [Event-driven] When the attention identifier matches the PF15 key code, the system shall set the PF3 action indicator to active.

REQ-F-1338: [Event-driven] When the attention identifier matches the PF16 key code, the system shall set the PF4 action indicator to active.

REQ-F-1339: [Event-driven] When the attention identifier matches the PF17 key code, the system shall set the PF5 action indicator to active.

REQ-F-1340: [Event-driven] When the attention identifier matches the PF18 key code, the system shall set the PF6 action indicator to active.

REQ-F-1341: [Event-driven] When the attention identifier matches the PF19 key code, the system shall set the PF7 action indicator to active.

REQ-F-1342: [Event-driven] When the attention identifier matches the PF20 key code, the system shall set the PF8 action indicator to active.

REQ-F-1343: [Event-driven] When the attention identifier matches the PF21 key code, the system shall set the PF9 action indicator to active.

REQ-F-1344: [Event-driven] When the attention identifier matches the PF22 key code, the system shall set the PF10 action indicator to active.

REQ-F-1345: [Event-driven] When the attention identifier matches the PF23 key code, the system shall set the PF11 action indicator to active.

REQ-F-1346: [Event-driven] When the attention identifier matches the PF24 key code, the system shall set the PF12 action indicator to active.

REQ-F-1347: [Ubiquitous] The system shall complete the evaluation of all terminal input keys and close the mapping dispatch.

REQ-F-1348: [Event-driven] When the user confirms a delete by pressing PF4 while in delete-confirmation state, the system shall retrieve the original transaction type code and delete the corresponding record from the transaction type data store.

REQ-F-1349: [Event-driven] When the user presses PF4 while the delete confirmation flag is active, the system shall invoke the delete processing procedure to remove the transaction type record from the transaction type data store using the original transaction type code as the deletion key.

REQ-F-1350: [Event-driven] When keyboard input is received and the user presses PF4 while confirming a delete, the system shall dispatch to the delete processing operation; otherwise the system shall dispatch to alternative branches based on the keyboard input and program state.

REQ-F-1351: [Event-driven] When the user presses PF12 during detail display or record creation, or when transaction type changes are confirmed, failed, or backed out, or when deletion completes or fails, the system shall set the program context to entry mode and mark transaction type details as not fetched.

REQ-F-1352: [Event-driven] When PF12 is pressed during detail display, new record creation, or detail-not-found state; or when changes are completed, failed, or backed out with empty old details; or when a delete operation completes or fails, the system shall set the program context to entry mode and mark details as not fetched.


---


## 92. Transaction Type Maintenance Screen Display
As a user maintaining transaction type records, I want the system to validate my input, display appropriate prompts and messages, and persist confirmed changes so that transaction type records are accurately maintained.

### Requirements

REQ-F-1353: [Ubiquitous] The system shall invoke the key-mapping routine to capture and store the terminal input key pressed by the user.

REQ-F-1354: [Event-driven] When terminal input is received, the system shall map the terminal AID key to the corresponding function key flag to indicate which key was pressed.

REQ-F-1355: [Ubiquitous] The system shall clear all screen output fields to low-values, populate the screen header with titles, transaction identifier, and program name, capture the current date and time, and format and display the date as MM/DD/YY and the time as HH:MM:SS.

REQ-F-1356: [Ubiquitous] The system shall receive the transaction type maintenance screen input from the terminal into the input buffer and capture the response code.

REQ-F-1357: [Ubiquitous] The system shall extract the transaction type code from screen input; if the user entered '*' or spaces, store low-values; otherwise trim and store the normalized code.

REQ-F-1358: [Ubiquitous] The system shall extract the transaction type description from screen input; if the user entered '*' or spaces, store low-values; otherwise trim and store the normalized description.

REQ-F-1359: [Event-driven] When the user is not creating a new record and has not yet confirmed changes, the system shall validate the transaction type code; if the code is blank, set an error message and reset the operation state; if validation fails, reset the operation state.

REQ-F-1360: [Ubiquitous] The system shall validate the transaction type code as a required numeric field; if valid, normalize by converting to numeric and back to alphanumeric with space-padding; if invalid, set an error flag and display an error message.

REQ-F-1361: [Event-driven] When a required numeric field is blank, contains only spaces, or has zero length, the system shall set the input error flag to error state, mark the field as blank in the validation flag, and record an error message stating the field name must be supplied.

REQ-F-1362: [Event-driven] When the numeric field contains non-numeric characters, the system shall set the input error flag to error state, mark the field as invalid in the validation flag, and record an error message stating the field name must be numeric.

REQ-F-1363: [Event-driven] When the numeric field value equals zero, the system shall set the input error flag to error state, mark the field as invalid in the validation flag, and record an error message stating the field name must not be zero.

REQ-F-1364: [Ubiquitous] The system shall mark the field validation as complete by setting the validation flag to valid state after all validation checks complete.

REQ-F-1365: [Ubiquitous] The system shall validate the transaction type description field for alphanumeric content; the field accepts up to 50 characters.

REQ-F-1366: [Ubiquitous] The system shall validate the description field as required alphanumeric; if blank or low-values, set an error flag and display a 'must be supplied' message; if non-alphanumeric characters are present, set an error flag and display a 'numbers or alphabets only' message.

REQ-F-1367: [Ubiquitous] The system shall mark the transaction type filter as valid and compare the new transaction type code and description against the stored values; if changes are detected, mark the operation state as changes made but not validated.

REQ-F-1368: [Ubiquitous] The system shall compare the new transaction type code and description with the previously fetched values using case-insensitive comparison; if identical, set the no-changes-found flag; if different, set the change-has-occurred flag.

REQ-F-1369: [Event-driven] When the transaction type record was not found in the database and the user re-entered the same transaction type code, the system shall skip validation and mark the transaction type filter as valid; if PF5 was not pressed, reset the operation state to indicate details have not been fetched.

REQ-F-1370: [Event-driven] When all field validations are complete, the system shall, if all validations passed, mark the operation state as changes validated and awaiting confirmation; if any validation error was detected, preserve the error state.

REQ-F-1371: [Event-driven] When a function key is pressed, the system shall validate the pressed key against the current processing state; if the key is invalid and no error message is already displayed, set the invalid-key-pressed flag.

REQ-F-1372: [Unwanted] If the user presses an invalid or unsupported function key, the system shall display the screen with an invalid key error message.

REQ-F-1373: [Event-driven] When the user enters a transaction type code and presses ENTER or PF12, the system shall retrieve the transaction type record from the transaction type data store using the entered code; if found, set the found flag; if not found, set an error flag and display a not-found message; if a database error occurs, set an error flag and display an error message with the SQLCODE.

REQ-F-1374: [Ubiquitous] The system shall store the retrieved transaction type code and description into the old transaction type details area.

REQ-F-1375: [Event-driven] When no transaction type record is found matching the search criteria, the system shall set the informational message to prompt the user to press F05 to add a new record or F12 to cancel.

REQ-F-1376: [Event-driven] When the user has chosen to create a new transaction type record, the system shall set the informational message to prompt the user to enter new transaction type details.

REQ-F-1377: [Event-driven] When the user presses PF5 after a search that found no matching record, the system shall set the state to create new record and display the screen with input fields for new transaction type details.

REQ-F-1378: [Event-driven] When the user presses PF5 after changes have been validated but not confirmed, the system shall invoke the write processing routine to save the transaction type changes to the transaction type data store and display the updated screen.

REQ-F-1379: [Event-driven] When the user confirms changes by pressing PF5, the system shall attempt to update the transaction type record in the transaction type data store; if successful (SQL code 0), commit the transaction; if no rows updated (SQL code +100), attempt an insert; if a lock conflict occurs (SQL code -911), set the lock-error state; if another database error occurs, set the failure state; after the attempt, set the completion state based on the outcome.

REQ-F-1380: [Event-driven] When the update returned no rows and an insert is attempted, the system shall insert a new transaction type record into the transaction type data store; if successful, commit the transaction; if a database error occurs, set an error flag and display an error message with the SQLCODE.

REQ-F-1381: [Event-driven] When the user presses PF4 while viewing transaction type details, the system shall set the delete state to confirm and display the confirmation prompt on the screen.

REQ-F-1382: [Event-driven] When the user presses PF4 while the delete confirmation prompt is active, the system shall set the delete state to start, execute the delete processing, and display the updated screen.

REQ-F-1383: [Event-driven] When the user confirms deletion by pressing PF4, the system shall delete the transaction type record from the transaction type data store; if successful (SQL code 0), set the delete-done flag and commit; if child records exist (SQL code -532), display a message instructing the user to delete associated child records first; if another database error occurs, set the delete-failed flag and display an error message with the SQLCODE.

REQ-F-1384: [Event-driven] When the user presses PF12 while changes are pending, a delete is pending, or details are displayed, the system shall reset the processing state and display the current transaction type details.

REQ-F-1385: [Event-driven] When the user cancels an operation (PF12 pressed) or an operation completes successfully or with failure, the system shall reset the program to initial entry mode, clearing transaction details and setting the details-not-fetched flag.

REQ-F-1386: [Event-driven] When transaction type changes are committed, the system shall set the informational message to 'Changes committed to database'; when changes fail due to a lock error or other failure, the system shall set the informational message to 'Changes unsuccessful'.

REQ-F-1387: [Event-driven] When a transaction type deletion operation completes successfully, the system shall set the informational message to 'Delete successful.'; when deletion fails, the system shall set the informational message to 'Changes unsuccessful'.

REQ-F-1388: [Event-driven] When the program is entered in entry mode, or details have not yet been fetched, or invalid search keys were provided, the system shall set the informational message to prompt the user to enter the transaction type code for maintenance.

REQ-F-1389: [Event-driven] When transaction type details are being displayed, or changes were backed out with no prior data, the system shall set the informational message to prompt the user to enter the transaction type code for maintenance.

REQ-F-1390: [Event-driven] When transaction type changes have been validated but not yet confirmed, the system shall set the informational message to prompt the user to press PF5 to save the validated changes.

REQ-F-1391: [Event-driven] When the user has initiated a delete operation and confirmation is required, the system shall set the informational message to prompt the user to confirm deletion by pressing PF4.

REQ-F-1392: [Event-driven] When changes were backed out, or changes were made but validation failed, the system shall set the informational message to prompt the user to update the transaction type details.

REQ-F-1393: [Event-driven] When no informational message has been set, the system shall set the informational message to the default search prompt.

REQ-F-1394: [Ubiquitous] The system shall calculate the center-justified position of the informational message, move the message to the output buffer at that position, and move both the informational message and any error message to the screen output record for display.

REQ-F-1395: [State-driven] While the screen is being prepared for display, the system shall populate the transaction type code and description fields based on current state: clear fields when in initial entry mode; display original values when showing details, confirming delete, showing delete failure, showing delete success, or backing out changes; display new values when showing a successful update.

REQ-F-1396: [Event-driven] When the user presses ENTER or any other unhandled function key, the system shall validate the transaction type code and description fields, determine the appropriate next action, and display the updated screen.

REQ-F-1397: [Unwanted] If an unexpected processing state is encountered, the system shall record the program name, error code '0001', and error message 'UNEXPECTED DATA SCENARIO' and route to the abend handler.

REQ-F-1398: [Unwanted] If an abnormal termination occurs, the system shall route the abnormal termination to the abend handler for error processing.

REQ-F-1399: [Ubiquitous] The system shall cancel abend handling.


### Non-Functional Requirements

REQ-N-004: [Event-driven] When the update or insert of a transaction type record succeeds, the system shall commit the transaction so that the change is durably persisted to the transaction type data store.

REQ-N-005: [Event-driven] When a transaction type deletion succeeds, the system shall commit the transaction so that the deletion is durably persisted to the transaction type data store.


---


## 93. Transaction Type Update Screen Navigation
As a user navigating the transaction type update workflow, I want the system to map my key presses to the correct actions and route control appropriately so that I can complete update, delete, and cancel operations.

### Requirements

REQ-F-1400: [Ubiquitous] The system shall invoke the key-mapping routine to capture and store the terminal input key pressed by the user.

REQ-F-1401: [Event-driven] When the attention identifier matches the ENTER key code, the system shall set the ENTER action indicator to active.

REQ-F-1402: [Event-driven] When the attention identifier matches the CLEAR key code, the system shall set the CLEAR action indicator to active.

REQ-F-1403: [Event-driven] When the attention identifier matches the PF1 key code, the system shall set the PF1 action indicator to active.

REQ-F-1404: [Event-driven] When the attention identifier matches the PF2 key code, the system shall set the PF2 action indicator to active.

REQ-F-1405: [Event-driven] When the attention identifier matches the PF3 key code, the system shall set the PF3 action indicator to active.

REQ-F-1406: [Event-driven] When the attention identifier matches the PF4 key code, the system shall set the PF4 action indicator to active.

REQ-F-1407: [Event-driven] When the attention identifier matches the PF5 key code, the system shall set the PF5 action indicator to active.

REQ-F-1408: [Event-driven] When the attention identifier matches the PF6 key code, the system shall set the PF6 action indicator to active.

REQ-F-1409: [Event-driven] When the attention identifier matches the PF7 key code, the system shall set the PF7 action indicator to active.

REQ-F-1410: [Event-driven] When the attention identifier matches the PF8 key code, the system shall set the PF8 action indicator to active.

REQ-F-1411: [Event-driven] When the attention identifier matches the PF9 key code, the system shall set the PF9 action indicator to active.

REQ-F-1412: [Event-driven] When the attention identifier matches the PF10 key code, the system shall set the PF10 action indicator to active.

REQ-F-1413: [Event-driven] When the attention identifier matches the PF11 key code, the system shall set the PF11 action indicator to active.

REQ-F-1414: [Event-driven] When the attention identifier matches the PF12 key code, the system shall set the PF12 action indicator to active.

REQ-F-1415: [Event-driven] When the attention identifier matches the PA1 key code, the system shall set the PA1 action indicator to active.

REQ-F-1416: [Event-driven] When the attention identifier matches the PA2 key code, the system shall set the PA2 action indicator to active.

REQ-F-1417: [Event-driven] When the attention identifier matches the PF13 key code, the system shall set the PF1 action indicator to active.

REQ-F-1418: [Event-driven] When the attention identifier matches the PF14 key code, the system shall set the PF2 action indicator to active.

REQ-F-1419: [Event-driven] When the attention identifier matches the PF15 key code, the system shall set the PF3 action indicator to active.

REQ-F-1420: [Event-driven] When the attention identifier matches the PF16 key code, the system shall set the PF4 action indicator to active.

REQ-F-1421: [Event-driven] When the attention identifier matches the PF17 key code, the system shall set the PF5 action indicator to active.

REQ-F-1422: [Event-driven] When the attention identifier matches the PF18 key code, the system shall set the PF6 action indicator to active.

REQ-F-1423: [Event-driven] When the attention identifier matches the PF19 key code, the system shall set the PF7 action indicator to active.

REQ-F-1424: [Event-driven] When the attention identifier matches the PF20 key code, the system shall set the PF8 action indicator to active.

REQ-F-1425: [Event-driven] When the attention identifier matches the PF21 key code, the system shall set the PF9 action indicator to active.

REQ-F-1426: [Event-driven] When the attention identifier matches the PF22 key code, the system shall set the PF10 action indicator to active.

REQ-F-1427: [Event-driven] When the attention identifier matches the PF23 key code, the system shall set the PF11 action indicator to active.

REQ-F-1428: [Event-driven] When the attention identifier matches the PF24 key code, the system shall set the PF12 action indicator to active.

REQ-F-1429: [Ubiquitous] The system shall complete the evaluation of all terminal input keys and close the mapping dispatch.

REQ-F-1430: [Complex] While the program has captured the terminal input key and mapped it to a key indicator, when the key pressed and application context match an evaluated condition, the system shall dispatch to the appropriate branch: when not re-entering from the administration program, perform administration-program logic; when not re-entering from the list program, perform list-program logic; when on initial entry with unfetched details, perform a detail fetch; when PF4 is pressed with delete confirmation or detail display active, perform update or delete; when PF5 is pressed with a not-found or unconfirmed-changes state, perform validation or confirmation; when PF12 is pressed with a confirmation, detail, or delete state active, perform cancel or reset; when an invalid key is pressed, handle the error; for any other combination, perform default handling.


---


## 94. Program Initialization and Communication Area Setup
As a batch operations team, I want the transaction type listing program to initialize correctly on first and subsequent entries so that session context and program state are consistently established.

### Requirements

REQ-F-1431: [Event-driven] When the program is invoked with no communication area passed, the system shall set the transaction identifier to 'CTLI', the program name to 'COTRTLIC', the program context to entering, and the current screen to the first page.

REQ-F-1432: [Event-driven] When the program is invoked with a communication area passed, the system shall restore the card demo and program communication areas from the passed data.

REQ-F-1433: [Event-driven] When the program is re-entered from a different program or after PF3 is pressed from the add-transaction screen, the system shall reinitialize the program communication area and reset pagination to the first page.

REQ-F-1434: [Event-driven] When the program is entered from a different program or PF3 is pressed while coming from the additional transaction, the system shall clear the delete and update flags and set the action to ENTER.

REQ-F-1435: [Event-driven] When the program is entered for the first time or re-entered from the administration program, the system shall initialize the transaction type filter fields.


---


## 95. Transaction Type List Initialization and Display on Entry
As an administrator, I want the transaction type list to be fully re-initialized when I exit or re-enter from a different program so that the display reflects a clean first-page state.

### Requirements

REQ-F-1436: [Event-driven] When the user presses PF3 (Exit) or the program re-enters from a different calling program, the system shall initialize all storage areas, record the current program as the originating program, set the program context to first-time entry, mark the first page and last-page-not-shown, retrieve forward-ordered transaction records, and display the screen.


---


## 96. Screen Output Map Initialization
As an administrator, I want the screen to display accurate header information on every display so that I can identify the current transaction, program, date, time, and page.

### Requirements

REQ-F-1437: [Ubiquitous] The system shall clear the screen output map to low-values and populate static header information including screen titles, transaction identifier ('CTLI'), program name ('COTRTLIC'), current date in MM/DD/YY format, current time in HH:MM:SS format, and current page number; the system shall also clear the information message and set its attribute to dark.


---


## 97. Screen Display Preparation Orchestration
As an administrator, I want the screen to be fully prepared before it is sent so that all field attributes, data, filter configurations, and messages are consistently applied.

### Requirements

REQ-F-1438: [Ubiquitous] The system shall orchestrate screen display preparation by invoking initialization, array attribute setup, array data population, filter field attribute setup, message setup, and screen send routines in sequence.


---


## 98. Transaction Type Screen Row Population
As an administrator, I want each retrieved transaction type record to be placed in the correct screen row so that the list is accurately displayed.

### Requirements

REQ-F-1439: [Event-driven] When a transaction type record is successfully retrieved from the transaction type store, the system shall increment the row counter, populate the current screen row with the transaction type code and description, and initialize the screen number to 1 if this is the first row and the screen number is currently zero.


---


## 99. Filter Validation and Record Count
As an administrator, I want the system to validate my filter criteria against the transaction type store so that I receive accurate results or a clear error when no records match.

### Requirements

REQ-F-1440: [Event-driven] When input validation is performed, the system shall execute a count query against the transaction type store to count records matching the current filter conditions; if the query succeeds, the system shall store the count.

REQ-F-1441: [Unwanted] If the count query fails, the system shall set the input error indicator and format an error message.

REQ-F-1442: [Unwanted] If the count is zero, the system shall set the input error indicator, mark the filters as invalid, protect the row selection field, and display the error message 'No Records found for these filter conditions'.


---


## 100. Screen Field Attribute Configuration
As an administrator, I want screen field attributes to reflect the current validation state and action context so that I can clearly identify which fields require attention.

### Requirements

REQ-F-1443: [State-driven] While rows remain to be processed, the system shall set the transaction type code field attribute to protected-with-MDT for each row.

REQ-F-1444: [Event-driven] When the row output data is empty or row protection is enabled, the system shall set the row selection field to protected.

REQ-F-1445: [Event-driven] When a row is not protected and has output data, the system shall set the row selection field attribute to MDT-set to enable user input.

REQ-F-1446: [Event-driven] When a row selection error is detected, the system shall highlight the row selection field in red and position the cursor to it.

REQ-F-1447: [Event-driven] When a delete action is requested with exactly one valid action and no conflicting actions, the system shall set the transaction type code and description field attributes to neutral color and position the cursor to the row selection field.

REQ-F-1448: [Event-driven] When an update action is requested with exactly one valid action and no conflicting actions, the system shall set the transaction type code field to neutral; if the update is complete, the system shall position the cursor to the row selection field and set the description to neutral; if the update is incomplete, the system shall position the cursor to the description field with MDT-set, and highlight the description in red if the description is invalid.


---


## 101. Filter Field Attribute Configuration
As an administrator, I want filter field attributes to reflect the current action and validation state so that I can identify which filter fields are active or in error.

### Requirements

REQ-F-1449: [Event-driven] When the program processes filter configuration after prior actions or validation results exist, the system shall set the transaction type filter field to autoskip-with-MDT and blue when actions are requested; set to MDT-set when validation is valid or invalid; and clear to low-values and set to MDT-set when the code is zero or any other value.

REQ-F-1450: [Event-driven] When the program processes filter configuration after prior actions or validation results exist, the system shall set the transaction type description filter field to autoskip-with-MDT and blue when actions are requested; set to MDT-set when validation is valid or invalid; and set to MDT-set for any other condition.

REQ-F-1451: [Event-driven] When the transaction type filter validation fails, the system shall set the transaction type filter field attribute to red and position the cursor to the transaction type filter field.

REQ-F-1452: [Event-driven] When the transaction type description filter validation fails, the system shall set the transaction type description filter field attribute to red and position the cursor to the transaction type description filter field.


---


## 102. Screen Information Message Display
As an administrator, I want informational messages to be displayed on screen when relevant so that I am informed of the outcome of my actions.

### Requirements

REQ-F-1453: [Event-driven] When an information message is set and is not the 'no records found' message, the system shall move the formatted information message to the screen output and set the field attribute to neutral.


---


## 103. Function Key Mapping
As a user of the transaction type maintenance function, I want my key presses recognized and mapped to the correct action indicators so that the system can route my input to the appropriate processing logic.

### Requirements

REQ-F-1454: [Event-driven] When the terminal input key matches the ENTER key, the system shall set the ENTER action indicator to active.

REQ-F-1455: [Event-driven] When the terminal input key matches the CLEAR key, the system shall set the CLEAR action indicator to active.

REQ-F-1456: [Event-driven] When the terminal input key matches the PA1 key, the system shall set the PA1 action indicator to active.

REQ-F-1457: [Event-driven] When the terminal input key matches the PA2 key, the system shall set the PA2 action indicator to active.

REQ-F-1458: [Event-driven] When the terminal input key matches PF1 or PF13, the system shall set the PF1 action indicator to active.

REQ-F-1459: [Event-driven] When the terminal input key matches PF2 or PF14, the system shall set the PF2 action indicator to active.

REQ-F-1460: [Event-driven] When the terminal input key matches PF3 or PF15, the system shall set the PF3 action indicator to active.

REQ-F-1461: [Event-driven] When the terminal input key matches PF4 or PF16, the system shall set the PF4 action indicator to active.

REQ-F-1462: [Event-driven] When the terminal input key matches PF5 or PF17, the system shall set the PF5 action indicator to active.

REQ-F-1463: [Event-driven] When the terminal input key matches PF6 or PF18, the system shall set the PF6 action indicator to active.

REQ-F-1464: [Event-driven] When the terminal input key matches PF7 or PF19, the system shall set the PF7 action indicator to active.

REQ-F-1465: [Event-driven] When the terminal input key matches PF8 or PF20, the system shall set the PF8 action indicator to active.

REQ-F-1466: [Event-driven] When the terminal input key matches PF9 or PF21, the system shall set the PF9 action indicator to active.

REQ-F-1467: [Event-driven] When the terminal input key matches PF10 or PF22, the system shall set the PF10 action indicator to active.

REQ-F-1468: [Event-driven] When the terminal input key matches PF11 or PF23, the system shall set the PF11 action indicator to active.

REQ-F-1469: [Event-driven] When the terminal input key matches PF12 or PF24, the system shall set the PF12 action indicator to active.

REQ-F-1470: [Ubiquitous] The system shall invoke the key-mapping routine to capture and store the terminal input key before evaluating any action routing.

REQ-F-1471: [Ubiquitous] The system shall complete evaluation of all terminal input keys and close the mapping dispatch after all key mappings have been assessed.


---


## 104. Navigation and Action Routing
As a user of the transaction type maintenance function, I want my key presses and current processing state to determine the correct next action so that the system routes me to the appropriate operation.

### Requirements

REQ-F-1472: [Complex] While the terminal input key has been captured and mapped to an action indicator, when the key pressed and application context are evaluated, the system shall dispatch to the appropriate operation: fetch details on initial entry with unfetched details; invoke update or delete processing when PF4 is pressed with delete confirmation or detail display active; invoke validation or confirmation when PF5 is pressed with details-not-found or unconfirmed-changes state; invoke cancel or reset when PF12 is pressed with confirmation, detail display, or delete-confirmation state; handle invalid key when an unsupported key is pressed; and perform default handling for any other combination.

REQ-F-1473: [Event-driven] When the user presses PF5 while transaction type changes are pending confirmation, the system shall invoke the transaction type update processing routine.

REQ-F-1474: [Event-driven] When the user presses PF12 while changes are pending, a delete is pending, or details are displayed, the system shall reset the processing state and display the current transaction type details.

REQ-F-1475: [Event-driven] When the user presses PF12 during detail display, new record creation, or detail-not-found state; or when transaction type changes are confirmed, failed, or backed out with empty prior details; or when a deletion completes or fails, the system shall set the program context to entry mode and mark transaction type details as not fetched.

REQ-F-1476: [Event-driven] When the user presses an invalid or unsupported function key, the system shall display the screen with an invalid key error message.

REQ-F-1477: [Event-driven] When a function key is pressed, the system shall validate the pressed key against the current processing state; if the key is invalid and no error message is already displayed, the system shall set the invalid-key-pressed flag.


---


## 105. Transaction Type Maintenance Screen Display
As a user of the transaction type maintenance function, I want the screen to display accurate transaction type details, contextual messages, and appropriate prompts based on the current processing state so that I can perform the correct action.

### Requirements

REQ-F-1478: [Ubiquitous] The system shall clear all screen output fields, populate the screen header with the application title, transaction identifier, and program name, capture the current system date and time, and display the date formatted as MM/DD/YY and the time formatted as HH:MM:SS.

REQ-F-1479: [Ubiquitous] The system shall receive the transaction type maintenance screen input from the terminal into the input buffer and capture the response code.

REQ-F-1480: [State-driven] While the screen is being prepared for display, the system shall populate the transaction type code and description fields as follows: clear the code field in initial entry mode; display original fetched values when showing details, confirming delete, showing delete failure, showing delete success, or backing out changes; display new user-entered values when showing a successful update; and display original values for all other states.

REQ-F-1481: [Ubiquitous] The system shall calculate the center-justified position of the informational message, place the message in the output buffer at that position, and move both the informational message and any error message to the screen output record for display.

REQ-F-1482: [Event-driven] When the program is entered in entry mode, or details have not yet been fetched, or invalid search keys were provided, the system shall set the informational message to prompt the user to enter the transaction type code for maintenance.

REQ-F-1483: [Event-driven] When no transaction type record is found matching the search criteria, the system shall set the informational message to prompt the user to press F05 to add a new record or F12 to cancel.

REQ-F-1484: [Event-driven] When the user has chosen to create a new transaction type record, the system shall set the informational message to prompt the user to enter new transaction type details.

REQ-F-1485: [Event-driven] When transaction type changes have been validated but not yet confirmed, the system shall set the informational message to prompt the user to press F5 to save the validated changes.

REQ-F-1486: [Event-driven] When the user has initiated a delete operation and confirmation is required, the system shall set the informational message to prompt the user to confirm deletion by pressing F4.

REQ-F-1487: [Event-driven] When transaction type details are being displayed, or changes were backed out with no prior data, the system shall set the informational message to prompt the user to enter the transaction type code for maintenance.

REQ-F-1488: [Event-driven] When changes were backed out or changes were made but validation failed, the system shall set the informational message to prompt the user to update the transaction type details.

REQ-F-1489: [Event-driven] When transaction type changes are committed, the system shall set the informational message to 'Changes committed to database'; when changes fail due to a lock error or other failure, the system shall set the informational message to 'Changes unsuccessful'.

REQ-F-1490: [Event-driven] When a transaction type deletion operation completes successfully, the system shall set the informational message to 'Delete successful.'; when deletion fails, the system shall set the informational message to 'Changes unsuccessful'.

REQ-F-1491: [Event-driven] When no informational message has been set, the system shall set the informational message to the default search prompt.


---


## 106. Transaction Type Input Validation
As a user of the transaction type maintenance function, I want my input validated before any database operation is attempted so that only valid data is written to the transaction type data store.

### Requirements

REQ-F-1492: [Ubiquitous] The system shall extract the transaction type code from screen input; if the user entered '*' or spaces, the system shall store a low-value marker; otherwise the system shall trim and store the normalized code.

REQ-F-1493: [Ubiquitous] The system shall extract the transaction type description from screen input; if the user entered '*' or spaces, the system shall store a low-value marker; otherwise the system shall trim and store the normalized description.

REQ-F-1494: [Event-driven] When the user is not creating a new record and has not yet confirmed changes, the system shall validate the transaction type code; if the code is blank, the system shall set an error message indicating no search criteria were received and reset the operation state; if validation fails, the system shall reset the operation state to indicate details have not been fetched.

REQ-F-1495: [Ubiquitous] The system shall validate the transaction type code as a required numeric field; if valid, the system shall normalize it by converting to numeric and back to alphanumeric with space-padding; if invalid, the system shall set an error flag and display an error message.

REQ-F-1496: [Ubiquitous] The system shall validate the transaction type description field for alphanumeric content with a maximum length of 50 characters.

REQ-F-1497: [Event-driven] When a required numeric field is blank, contains only spaces, or has zero length, the system shall set the input error flag to error state and record an error message stating the field name must be supplied.

REQ-F-1498: [Event-driven] When the numeric field contains non-numeric characters, the system shall set the input error flag to error state and record an error message stating the field name must be numeric.

REQ-F-1499: [Event-driven] When the numeric field value equals zero, the system shall set the input error flag to error state and record an error message stating the field name must not be zero; when the value is non-zero, no error shall be recorded.

REQ-F-1500: [Ubiquitous] The system shall mark the field validation as complete by setting the validation flag to valid state after all validation checks complete.

REQ-F-1501: [Ubiquitous] The system shall validate the description field as a required alphanumeric field; if blank or low-values, the system shall set an error flag and display a 'must be supplied' message; if non-alphanumeric characters are present, the system shall set an error flag and display a 'numbers or alphabets only' message.

REQ-F-1502: [Event-driven] When all field validations are complete, the system shall check the overall validation outcome; if all validations passed, the system shall mark the operation state as changes validated and awaiting confirmation; if any validation error was detected, the system shall preserve the error state.

REQ-F-1503: [Ubiquitous] The system shall mark the transaction type filter as valid and compare the new transaction type code and description against the stored values; if changes are detected, the system shall mark the operation state as changes made but not validated.

REQ-F-1504: [Ubiquitous] The system shall compare the new transaction type code and description with the previously fetched values using case-insensitive comparison; if identical, the system shall set the no-changes-found flag; if different, the system shall set the change-has-occurred flag.

REQ-F-1505: [Event-driven] When a previous lookup determined the transaction type record was not found and the user has re-entered the same transaction type code without modification, the system shall skip further validation and mark the transaction type filter as valid; if F5 was not pressed, the system shall reset the operation state to indicate details have not been fetched.

REQ-F-1506: [Event-driven] When the user presses Enter or any other unhandled key, the system shall validate the transaction type code and description fields, determine the appropriate next action, and display the updated screen.


---


## 107. Transaction Type Record Retrieval
As a user of the transaction type maintenance function, I want the system to retrieve the matching transaction type record from the transaction type data store so that I can view, update, or delete it.

### Requirements

REQ-F-1507: [Event-driven] When the user enters a transaction type code and presses ENTER or F12, the system shall retrieve the transaction type record from the transaction type data store (CARDDEMO.TRANSACTION_TYPE) using the entered code; if found, the system shall set the found flag; if not found, the system shall set an error flag and display a not-found message; if a database error occurs, the system shall set an error flag and display an error message with the SQLCODE.

REQ-F-1508: [Ubiquitous] The system shall store the retrieved transaction type code and description into the original transaction type details area to preserve the database values for subsequent change detection.


---


## 108. Transaction Type Record Creation
As a user of the transaction type maintenance function, I want to create a new transaction type record when no matching record exists so that new transaction types can be added to the transaction type data store.

### Requirements

REQ-F-1509: [Event-driven] When the user presses F5 after a search that found no matching transaction type record, the system shall set the state to create new record and display the screen with input fields for new transaction type details.

REQ-F-1510: [Event-driven] When the UPDATE statement finds no matching transaction type record (SQL code +100), the system shall insert a new transaction type record into the transaction type data store (CARDDEMO.TRANSACTION_TYPE) using the prepared transaction type code and description.

REQ-F-1511: [Event-driven] When the update returned no rows and an insert is attempted, the system shall insert the new transaction type record into the transaction type data store; if successful, the system shall commit the transaction; if a database error occurs, the system shall set an error flag and display an error message with the SQLCODE.


---


## 109. Transaction Type Record Update
As a user of the transaction type maintenance function, I want to save validated changes to an existing transaction type record so that the transaction type data store reflects the updated information.

### Requirements

REQ-F-1512: [Ubiquitous] The system shall copy and trim the new transaction type code and description into the SQL host variables and compute the description length before executing a database write against the transaction type data store (CARDDEMO.TRANSACTION_TYPE).

REQ-F-1513: [Complex] While transaction type data has been prepared in SQL host variables, when the UPDATE statement executes against the transaction type data store (CARDDEMO.TRANSACTION_TYPE), the system shall update the transaction type record when found (SQL code 0), invoke the insert routine when no matching record exists (SQL code +100), and handle database errors for SQL code -911 or other negative codes.

REQ-F-1514: [Event-driven] When the user presses F5 after changes have been validated but not yet confirmed, the system shall execute the write processing to save changes to the transaction type data store and display the updated screen.

REQ-F-1515: [Event-driven] When the user confirms changes by pressing F5, the system shall attempt to update the transaction type record in the transaction type data store; if successful, the system shall commit the transaction; if no rows are updated, the system shall attempt an insert; if a lock conflict occurs (SQL code -911), the system shall set the lock-error state; if another database error occurs, the system shall set the failure state; and upon completion, the system shall set the appropriate completion state.


### Non-Functional Requirements

REQ-N-006: [Event-driven] When a transaction type insert succeeds, the system shall commit the transaction before returning control.

REQ-N-007: [Event-driven] When a transaction type update succeeds, the system shall commit the transaction before returning control.


---


## 110. Transaction Type Record Deletion
As a user of the transaction type maintenance function, I want to delete a transaction type record after confirming the operation so that obsolete transaction types can be removed from the transaction type data store.

### Requirements

REQ-F-1516: [Event-driven] When the user presses F4 while viewing transaction type details, the system shall set the delete state to confirm and display the confirmation prompt on the screen.

REQ-F-1517: [Event-driven] When the user presses F4 while the delete confirmation prompt is active, the system shall set the delete state to start, execute the delete processing, and display the updated screen.

REQ-F-1518: [Event-driven] When the user presses PF4 while in delete-confirmation state, the system shall retrieve the original transaction type code and delete the corresponding record from the transaction type data store (CARDDEMO.TRANSACTION_TYPE).

REQ-F-1519: [Event-driven] When the user presses PF4 while the delete confirmation flag is active, the system shall invoke the delete processing procedure to remove the transaction type record from the transaction type data store (CARDDEMO.TRANSACTION_TYPE) using the original transaction type code as the deletion key.

REQ-F-1520: [Event-driven] When the user confirms deletion by pressing F4, the system shall delete the transaction type record from the transaction type data store; if successful, the system shall set the delete-done flag and commit the transaction; if child records exist (SQL code -532), the system shall display a message instructing the user to delete associated child records first; if any other database error occurs, the system shall set the delete-failed flag and display an error message with the SQLCODE.

REQ-F-1521: [Event-driven] When keyboard input is received and the program state is evaluated, the system shall dispatch to the delete processing operation when PF4 is pressed and the user is confirming a delete; otherwise the system shall dispatch to alternative branches based on the keyboard input and program state.

REQ-F-1522: [Event-driven] When PF12 is pressed during detail display, new record creation, or detail-not-found state; or when changes are completed, failed, or backed out with empty old details; or when a delete operation completes or fails, the system shall set the program context to entry mode and mark details as not fetched.


### Non-Functional Requirements

REQ-N-008: [Event-driven] When a transaction type deletion succeeds, the system shall commit the transaction before returning control.


---


## 111. Unexpected State Handling
As an operations team, I want the system to detect and handle unexpected processing states so that unrecognized scenarios do not result in silent data corruption.

### Requirements

REQ-F-1523: [Unwanted] If an unexpected processing state is encountered, the system shall record the program name, error code '0001', and error message 'UNEXPECTED DATA SCENARIO' and route to the abend handler.

REQ-F-1524: [Ubiquitous] The system shall cancel abend handling to allow abnormal terminations to proceed without special handling.

REQ-F-1525: [Unwanted] If an abnormal termination occurs, the system shall route the abnormal termination to the abend routine for error processing.


---


## 112. User List Navigation — Entry and Program Transfer Control
As an operator, I want the user list program to route me to the correct destination on entry and key press so that I always land on the appropriate screen for my workflow.

### Requirements

REQ-F-1526: [Ubiquitous] The system shall initialize the next-page flag to 'N' on every program entry to indicate no additional pages are available by default.

REQ-F-1527: [Event-driven] When the program is invoked with a zero-length session context, the system shall set the destination program to 'COSGN00C' and transfer control to that program.

REQ-F-1528: [Event-driven] When the program is invoked with a non-zero session context and the program context indicator is not set to re-entry status, the system shall copy the session context into the shared record, set the program context indicator to re-entry status (1), clear the user list screen output area, and immediately process the Enter key action.

REQ-F-1529: [Event-driven] When the program is re-entered and the user presses PF3, the system shall set the destination program to 'COADM01C' and transfer control to that program.

REQ-F-1530: [Event-driven] When the program is re-entered and the user presses a key other than Enter, PF3, PF7, or PF8, the system shall display an invalid key message and redisplay the user list screen without erasing it.

REQ-F-1531: [Event-driven] When the program is re-entered and the user presses a key, the system shall receive the user list screen input and dispatch to the appropriate handler based on the attention identifier: Enter triggers user selection processing; PF3, PF7, PF8, or other keys trigger their respective handlers.

REQ-F-1532: [Ubiquitous] Before transferring control to any destination program, the system shall validate that the destination program name is set (defaulting to 'COSGN00C' if empty or spaces), record the current transaction identifier and program name as the originating context, and reset the program context indicator to 0.


---


## 113. User List Display and Pagination
As an operator, I want to browse a paginated list of users so that I can locate the user record I need to act on.

### Requirements

REQ-F-1533: [Ubiquitous] The system shall retrieve the current date and time, format them as MM/DD/YY and HH:MM:SS respectively, and populate the user list screen header with the formatted date, time, screen titles, transaction ID, and program name before sending the screen.

REQ-F-1534: [Ubiquitous] The system shall clear all 10 user data rows before populating the user list display with the new page of records.

REQ-F-1535: [State-driven] While records remain available in the user security data store (AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS) and no error has occurred, the system shall read each user record, populate the corresponding screen row with user ID, first name, last name, and user type, and continue until 10 rows are filled or the file is exhausted.

REQ-F-1536: [Event-driven] When the user list population loop completes, the system shall increment the page number if more records exist beyond the current page or if at least one row was populated; if no rows were populated, the page number shall remain unchanged.

REQ-F-1537: [Ubiquitous] The system shall move the current page number to the screen display, clear the user ID search field, and send the populated user list screen to the terminal.

REQ-F-1538: [Event-driven] When the program attempts to read the next user record from the user security data store and end-of-file is reached, the system shall set the end-of-file flag and display a bottom-of-list message; if any other error occurs, the system shall set the error flag and display an error message.

REQ-F-1539: [Event-driven] When the program attempts to read the previous user record from the user security data store and end-of-file is reached, the system shall set the end-of-file flag and display a top-of-list message; if any other error occurs, the system shall set the error flag and display an error message.

REQ-F-1540: [Event-driven] When the program attempts to position the cursor at a starting user ID in the user security file and the user ID is not found, the system shall set the end-of-file flag and display a top-of-list message; if any other error occurs, the system shall set the error flag and display an error message.

REQ-F-1541: [Event-driven] When the user presses PF8 to navigate forward and the next-page flag is set, the system shall load the next page of users; if the next-page flag is not set, the system shall display a bottom-of-list message and send the screen without erasing.

REQ-F-1542: [Event-driven] When the user presses PF7 to navigate backward, the system shall set the next-page flag to indicate more pages are available; if the current page number is greater than 1, the system shall load the previous page; otherwise the system shall display a top-of-list message and send the screen without erasing.

REQ-F-1543: [Event-driven] When the user presses PF7 to navigate backward, the system shall position the cursor for backward navigation; if cursor positioning fails, the system shall skip the entire backward navigation sequence.

REQ-F-1544: [State-driven] While records remain available and no error has occurred during backward navigation, the system shall read the previous user record and populate the corresponding display row, decrementing the row counter, until 10 rows are filled, end-of-file is reached, or an error occurs.

REQ-F-1545: [Event-driven] When the backward page has been loaded, the system shall read one additional record to check for more pages; if more records exist and the current page number is greater than 1, the system shall decrement the page number by one; otherwise the system shall set the page number to 1.

REQ-F-1546: [Event-driven] When the user presses Enter, PF7, or PF8 to navigate forward through the user list, the system shall position the cursor to the next set of user records in the user security data store and then read the first record of that set; if positioning fails, the system shall set the error flag and skip further processing.

REQ-F-1547: [Event-driven] When the row index is set to a value within 1–10, the system shall copy the user ID, first name, last name, and user type from the retrieved security user record into the corresponding row of the user list display.

REQ-F-1548: [Event-driven] When the row index is set to 10, the system shall additionally store the user ID in the last user ID pagination marker to track the final user displayed on the current page.

REQ-F-1549: [Unwanted] If the row index is outside the range 1–10, the system shall take no action and leave the screen row data unchanged.


---


## 114. User List Selection and Routing
As an operator, I want to select a user record from the list and be routed to the appropriate update or deletion screen so that I can perform the intended action on that user.

### Requirements

REQ-F-1550: [Event-driven] When the user presses Enter, the system shall evaluate each of the 10 displayed rows and record the selection flag and user identifier from the first row whose selection flag is non-blank and non-null into the session context as the chosen selection flag and selected user identifier.

REQ-F-1551: [Event-driven] When no row on the user list screen contains a non-blank, non-null selection indicator after Enter is pressed, the system shall clear both the user selection flag and the user identifier in the session context.

REQ-F-1552: [Event-driven] When a user selection is present in the session context (both selection flag and user identifier are non-blank), the system shall validate the selection action code; valid values are 'U' or 'u' (update) and 'D' or 'd' (delete); if the action code is any other value, the system shall set the error message 'Invalid selection. Valid values are U and D' and mark the user ID search field as in error.

REQ-F-1553: [Event-driven] When the selection action code is 'U' or 'u', the system shall set the destination program to 'COUSR02C', populate the session context with the current transaction identifier and program name, reset the program context indicator to 0, and transfer control to 'COUSR02C'.

REQ-F-1554: [Event-driven] When the selection action code is 'D' or 'd', the system shall set the destination program to 'COUSR03C', record the current transaction identifier and program name as the originating context, set the program context indicator to 0, and transfer control to 'COUSR03C' carrying the full session record.

REQ-F-1555: [Event-driven] When selection validation completes, the system shall evaluate the user ID search input; if the search field is empty, the system shall clear the security user record's user ID to low-values for an unrestricted search; if the search field contains a value, the system shall copy it to the security user record's user ID field; in either case the system shall reset the page number to 0 and clear the user ID search output field on the display if no error flag is set.


---


## 115. User Account Update (COUSR02C — invoked by user list)
As an operator, I want to update a user's account details so that the user security data store reflects current information.

### Requirements

REQ-F-1556: [Event-driven] When the user account update program is invoked without a session context, the system shall set the destination program to the signon screen and transfer control to it.

REQ-F-1557: [Event-driven] When the user account update program is invoked with a session context, the system shall restore the session context from the linkage section into working storage.

REQ-F-1558: [Ubiquitous] Before transferring control to a destination program, the system shall validate that the destination program is set (defaulting to 'COSGN00C' if empty or low-values), populate the session context with the current program's transaction ID and program name, and reset the program context indicator to zero.

REQ-F-1559: [Event-driven] When the user presses PF12, the system shall set the destination program to 'COADM01C' and transfer control to it.

REQ-F-1560: [Ubiquitous] The system shall clear the message text buffer and error message display field before sending the user update screen.

REQ-F-1561: [Ubiquitous] The system shall populate the user update screen header with the current date formatted as MM/DD/YY, the current time formatted as HH:MM:SS, screen titles, transaction ID, and program name before sending the screen.

REQ-F-1562: [Event-driven] When the program is not in re-entry status, the system shall send the user update screen to the terminal.

REQ-F-1563: [Event-driven] When the program is in re-entry status and the user presses Enter, the system shall validate that the user ID is not empty; if empty, the system shall set the error flag, display an error message, and reposition the cursor to the user ID field; if not empty, the system shall clear the first name, last name, password, and user type fields, copy the user ID to the security user record, and retrieve the user record from the user security data store.

REQ-F-1564: [Event-driven] When a user record retrieval is requested and the response is successful, the system shall display an informational message and send the screen; if the user ID is not found, the system shall set the error flag, display a not-found message, and reposition the cursor to the user ID field; if any other error occurs, the system shall set the error flag, display an error message, and reposition the cursor.

REQ-F-1565: [Event-driven] When the user record is successfully retrieved, the system shall populate the screen input fields with the user's first name, last name, password, and user type from the user security data store and send the screen.

REQ-F-1566: [Event-driven] When the program is in re-entry status and the user presses PF3 or PF5 to update, the system shall validate that the user ID is not empty; if empty, the system shall set the error flag, display an error message, and reposition the cursor to the user ID field.

REQ-F-1567: [Event-driven] When an update request is initiated and the user ID is not empty, the system shall validate that the first name is not empty; if empty, the system shall set the error flag, display an error message, and reposition the cursor to the first name field.

REQ-F-1568: [Event-driven] When an update request is initiated and the first name is not empty, the system shall validate that the last name is not empty; if empty, the system shall set the error flag, display an error message, and reposition the cursor to the last name field.

REQ-F-1569: [Event-driven] When an update request is initiated and the last name is not empty, the system shall validate that the password is not empty; if empty, the system shall set the error flag, display an error message, and reposition the cursor to the password field.

REQ-F-1570: [Event-driven] When an update request is initiated and the password is not empty, the system shall validate that the user type is not empty; if empty, the system shall set the error flag, display an error message, and reposition the cursor to the user type field.

REQ-F-1571: [Event-driven] When all required fields pass validation, the system shall retrieve the current user record from the user security data store, compare each of first name, last name, password, and user type field-by-field with the screen input, update any fields that differ in the security user record, and set the user-modified flag for each changed field.

REQ-F-1572: [Event-driven] When the user-modified flag is set after field comparison, the system shall write the updated user record to the user security data store; if successful, the system shall display a success message; if the user ID is not found, the system shall display a not-found message and reposition the cursor to the user ID field; if any other error occurs, the system shall display an error message and reposition the cursor.

REQ-F-1573: [Event-driven] When no fields were changed after field comparison, the system shall display an informational message and send the screen.

REQ-F-1574: [Event-driven] When the user presses PF4, the system shall clear all screen input fields and send the screen.

REQ-F-1575: [Event-driven] When the program is in re-entry status and the user presses a key other than Enter, PF3, PF4, or PF5, the system shall display an invalid-key error message.


---


## 116. User Deletion (COUSR03C — invoked by user list)
As an operator, I want to look up and delete a user record so that the user is removed from the user security data store.

### Requirements

REQ-F-1576: [Event-driven] When the user deletion program is invoked without a session context, the system shall set the destination program to the signon screen and transfer control to it.

REQ-F-1577: [Event-driven] When the user deletion program is invoked with a session context, the system shall copy the session context into the local session record.

REQ-F-1578: [Ubiquitous] The system shall clear the error flag at program entry to indicate no error condition.

REQ-F-1579: [Ubiquitous] The system shall clear the message text buffer and error message output field before sending the user deletion screen.

REQ-F-1580: [Ubiquitous] The system shall retrieve the current system date and time, format them as MM/DD/YY and HH:MM:SS respectively, and populate the user deletion screen header with the transaction ID, program name, screen titles, formatted date, and formatted time.

REQ-F-1581: [Ubiquitous] The system shall send the user deletion screen to the terminal with the message text moved to the error message field.

REQ-F-1582: [Event-driven] When the program is re-entered and the user presses Enter, the system shall validate that the user ID is not empty; if empty, the system shall set the error flag and display an error message; if valid, the system shall clear the first name, last name, and user type fields, copy the entered user ID to the security user record, and retrieve the user record from the user security data store.

REQ-F-1583: [Event-driven] When a user record lookup is requested and the record is found, the system shall populate the first name, last name, and user type screen fields with the retrieved data and display a prompt to press PF5 to delete; if the user ID is not found, the system shall set the error flag and display a not-found message; if any other error occurs, the system shall set the error flag and display an error message.

REQ-F-1584: [Event-driven] When the program is re-entered and the user presses PF5, the system shall validate that the user ID is not empty; if empty, the system shall set the error flag and display an error message; if valid, the system shall retrieve the user record to confirm existence and then delete it from the user security data store, displaying the outcome message.

REQ-F-1585: [Event-driven] When a user deletion is requested and the deletion succeeds, the system shall clear all fields and display a success message; if the user ID is not found, the system shall display a not-found message; if any other error occurs, the system shall display an error message.

REQ-F-1586: [Event-driven] When the program is re-entered and the user presses PF4, the system shall clear all screen input fields and the message text buffer, reset the user ID field cursor position, and send the cleared screen.

REQ-F-1587: [Event-driven] When the program is re-entered and the user presses PF12, the system shall set the destination program to the administration program and transfer control to it.

REQ-F-1588: [Event-driven] When the program is re-entered and the user presses a key other than Enter, PF4, PF5, or PF12, the system shall display an invalid-key error message and redisplay the screen.

REQ-F-1589: [Ubiquitous] Before transferring control to a destination program, the system shall validate that the destination program name is set (defaulting to the signon program if empty or low-values), populate the session context with the current transaction identifier and program name, reset the program context indicator to entry status (0), and transfer control to the destination program.


### Open Questions

OQ-019: Rule `d8ae0be9_3d4f_4434_ae77_c3d98a3e03c4` states that when the user list program is invoked with a zero-length session context, an alternative branch handles the condition but the branch content is not described. Rule `8b1daf65_8172_4273_aeca_94d2eca14dbb` from group 1 states the program transfers to 'COSGN00C' in this case. Are these two rules describing the same behavior, or does the group-4 rule represent a distinct unspecified path? — Owner: application architect

OQ-020: The backward page navigation rule (`e8ed322d_a49e_4195_abb3_6d7a97b0715b`) conditions reading the previous record on the key being PF8 (not Enter) during a PF7 press. This appears to be a legacy branching artifact. Confirm whether this condition is intentional business logic or a defect to be corrected in the modernized system. — Owner: application architect


---


## 117. Card Demonstration Application Navigation
As a user of the card demonstration application, I want the system to route me to the correct screen based on my entry context and key presses so that I can navigate the application efficiently.

### Requirements

REQ-F-1590: [Event-driven] When the program is invoked without a communication area, the system shall set the destination to the signon screen and transfer control to the signon screen handler.

REQ-F-1591: [Ubiquitous] The system shall validate the destination program name and, if it is unset (contains low-values or spaces), default it to the signon screen; populate the session context with the originating transaction identifier and originating program name; set the program context indicator to initial-entry status; and transfer control to the destination program, passing the prepared session context.

REQ-F-1592: [Complex] While the program context indicator signals re-entry, when the program is re-entered with a session context and the user presses PF3, the system shall set the destination program name to the administration screen and transfer control to that screen.


---


## 118. User Account Creation and Screen Management
As an administrator, I want to create user accounts by entering required profile fields so that new users can be granted access to the card demonstration application.

### Requirements

REQ-F-1593: [Ubiquitous] The system shall receive user input capturing the first name, last name, user ID, password, and user type fields from the user-addition screen.

REQ-F-1594: [Ubiquitous] The system shall retrieve the current system date and time, format the date as MM/DD/YY and the time as HH:MM:SS, and populate the screen header with the formatted date, time, application titles, transaction identifier, and program name before displaying the user-addition screen.

REQ-F-1595: [Ubiquitous] The system shall clear the message text buffer and error message output field before displaying the user-addition screen.

REQ-F-1596: [Ubiquitous] The system shall display the user-addition screen with the populated header and current message.

REQ-F-1597: [Event-driven] When the user submits the form and the first name field is empty (spaces or low-values), the system shall set the error flag, store the error message "First Name can NOT be empty...", and redisplay the screen.

REQ-F-1598: [Event-driven] When the user submits the form and the last name field is empty (spaces or low-values), the system shall set the error flag, store the error message "Last Name can NOT be empty...", and redisplay the screen.

REQ-F-1599: [Event-driven] When the user submits the form and the user ID field is empty (spaces or low-values), the system shall set the error flag, store the error message "User ID can NOT be empty...", and redisplay the screen.

REQ-F-1600: [Event-driven] When the user submits the form and the password field is empty (spaces or low-values), the system shall set the error flag, store the error message "Password can NOT be empty...", and redisplay the screen.

REQ-F-1601: [Event-driven] When the user submits the form and the user type field is empty (spaces or low-values), the system shall set the error flag, store the error message "User Type can NOT be empty...", and redisplay the screen.

REQ-F-1602: [Event-driven] When all required fields (first name, last name, user ID, password, and user type) pass validation, the system shall copy the user ID (up to 8 alphanumeric characters), first name (up to 20 alphanumeric characters), last name (up to 20 alphanumeric characters), password (up to 8 alphanumeric characters), and user type (1 alphanumeric character) from the screen input into the user security record and write the record to the user security data store (AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS).

REQ-F-1603: [Event-driven] When the user presses a key other than Enter, PF3, or PF4, the system shall treat the key as invalid and redisplay the user-addition screen.

REQ-F-1604: [Event-driven] When the user record is written to the user security data store successfully, the system shall clear all screen input fields, display a success message, and redisplay the user-addition screen.

REQ-F-1605: [Event-driven] When the write to the user security data store fails because the user ID already exists, the system shall display a duplicate-user error message and redisplay the user-addition screen.

REQ-F-1606: [Event-driven] When the write to the user security data store fails for any reason other than a duplicate user ID, the system shall display a generic add-user error message and redisplay the user-addition screen.

REQ-F-1607: [Event-driven] When the user presses PF4, the system shall initialize all input fields to empty and redisplay the user-addition screen.

REQ-F-1608: [Event-driven] When the program receives control for the first time (zero session context length), the system shall initialize the screen input fields and display the user-addition screen.

REQ-F-1609: [Event-driven] When the program receives control on re-entry (non-zero session context length), the system shall receive user input and dispatch to the appropriate handler based on the key pressed: Enter to process and validate data, PF4 to clear the screen, or any other key to display an invalid-key error.


### Open Questions

OQ-021: Rule `c3d8da47` describes PF3 routing to the administration screen and mentions Enter and PF4 as handled by "alternative branches not present in this slice." The Enter and PF4 navigation targets for the navigation program (Group 1) are not specified in the provided rules. What are the destination programs for Enter and PF4 in the navigation context? — Owner: application design team


---


## 119. Screen Navigation and Program Transfer Control
As an interactive user, I want the application to route me to the correct screen based on my navigation actions so that I can move through the card demonstration application workflows without losing context.

### Requirements

REQ-F-1610: [Event-driven] When the program is invoked without a communication area, the system shall set the destination to the signon screen and transfer control to it.

REQ-F-1611: [Event-driven] When the program is re-invoked with a communication area, the system shall unpack the incoming communication area into working storage to restore the navigation context (originating program, transaction, and destination) from the caller.

REQ-F-1612: [Event-driven] When the user presses the PF3 key, the system shall set the destination program to the administration screen if no origin program is recorded; otherwise, set the destination to the recorded origin program, and transfer control to it.

REQ-F-1613: [Event-driven] When the user presses the PF12 key, the system shall set the destination program to the administration screen and transfer control to it.

REQ-F-1614: [Ubiquitous] The system shall validate that a destination program is set before transferring control; if the destination program field is empty or contains low-values, the system shall default it to the signon screen.

REQ-F-1615: [Ubiquitous] The system shall populate the communication area with the current program's transaction ID and program name, and reset the program context indicator to initial entry status before transferring control to the destination program.

REQ-F-1616: [Ubiquitous] The system shall transfer control to the destination program, passing the updated communication area as shared context.


---


## 120. User Account Update and Screen Management
As an administrator, I want to retrieve, validate, and update user account records so that user profile data in the security data store remains accurate and current.

### Requirements

REQ-F-1617: [Event-driven] When the communication area length is zero, the system shall execute initial entry logic and display the user account update screen.

REQ-F-1618: [Event-driven] When the communication area length is greater than zero, the system shall restore the communication area from the linkage section into working storage.

REQ-F-1619: [Event-driven] When the program is not re-entering from a previous invocation, the system shall initialize the screen output area, set cursor focus to the user ID field, populate the user ID if pre-selected, and retrieve the user record from the user security data store (AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS) if a user ID is present.

REQ-F-1620: [Event-driven] When the program is re-entering and user input is received, the system shall receive the screen input and dispatch to the appropriate handler based on the function key pressed: ENTER triggers user record retrieval, PF3 or PF5 triggers user record update, PF4 clears all screen input fields, and any other key causes an invalid-key error message to be displayed.

REQ-F-1621: [Event-driven] When the ENTER key is pressed, the system shall validate that the user ID is not empty; if empty, the system shall set the error flag, display an error message, and reposition the cursor to the user ID field.

REQ-F-1622: [Event-driven] When the ENTER key is pressed and the user ID is not empty, the system shall clear the first name, last name, password, and user type input fields, move the user ID to the security user data record, and retrieve the user record from the user security data store.

REQ-F-1623: [Event-driven] When a user record retrieval is requested, the system shall retrieve the record from the user security data store keyed by user ID; if successful, display an informational message; if the user ID is not found, set the error flag, display a not-found message, and reposition the cursor to the user ID field; if any other error occurs, set the error flag, display an error message, and reposition the cursor to the first name field.

REQ-F-1624: [Event-driven] When a user record is successfully retrieved and no error flag is set, the system shall populate the screen input fields with the retrieved user's first name, last name, password, and user type from the user security data store, then display the screen.

REQ-F-1625: [Event-driven] When an update request is initiated (PF3 or PF5 pressed), the system shall validate that the user ID is not empty; if empty, set the error flag, display an error message, and reposition the cursor to the user ID field.

REQ-F-1626: [Event-driven] When an update request is initiated and the user ID is not empty, the system shall validate that the first name is not empty; if empty, set the error flag, display an error message, and reposition the cursor to the first name field.

REQ-F-1627: [Event-driven] When an update request is initiated and the first name is not empty, the system shall validate that the last name is not empty; if empty, set the error flag, display an error message, and reposition the cursor to the last name field.

REQ-F-1628: [Event-driven] When an update request is initiated and the last name is not empty, the system shall validate that the password is not empty; if empty, set the error flag, display an error message, and reposition the cursor to the password field.

REQ-F-1629: [Event-driven] When an update request is initiated and the password is not empty, the system shall validate that the user type is not empty; if empty, set the error flag, display an error message, and reposition the cursor to the user type field.

REQ-F-1630: [Event-driven] When all required fields (user ID, first name, last name, password, and user type) pass validation, the system shall retrieve the current user record from the user security data store, compare each field with the screen input, update any fields that differ in the security user data record, and set the user-modified flag for each changed field.

REQ-F-1631: [Event-driven] When field comparison is complete and the user-modified flag is set, the system shall write the updated user record back to the user security data store; if successful, display a success message containing the user ID; if the user ID is not found, display a not-found message and reposition the cursor to the user ID field; if any other error occurs, display an error message and reposition the cursor to the first name field.

REQ-F-1632: [Event-driven] When field comparison is complete and no fields were changed, the system shall display an informational message and display the screen without writing to the user security data store.

REQ-F-1633: [Event-driven] When the PF4 key is pressed, the system shall clear all screen input fields and display the screen.

REQ-F-1634: [Ubiquitous] The system shall clear the message text buffer and error message display field before sending the screen.

REQ-F-1635: [Ubiquitous] The system shall move the message text to the screen error message field before sending the screen.

REQ-F-1636: [Ubiquitous] The system shall populate the screen header with the current date formatted as MM/DD/YY, the current time formatted as HH:MM:SS, screen titles, transaction ID, and program name before sending the screen.

REQ-F-1637: [Ubiquitous] The system shall send the user account update screen to the terminal, erasing the previous display and positioning the cursor as previously set.

REQ-F-1638: [Ubiquitous] The system shall receive user input from the screen into the input buffer, capturing the response code and reason code for subsequent error handling.

REQ-F-1639: [Event-driven] When all required fields pass validation, the system shall position the cursor to the first name field for the next interaction.


### Open Questions

OQ-022: Rule ec9cd385 (noise_context) states that PF3 routes to the administration screen if no origin program is recorded, or to the recorded origin program otherwise — but the group_label for group 1 describes PF3 routing to the signon screen. The rule text and the group description conflict. Which destination is correct for PF3 when an origin program is recorded? — Owner: navigation/UX team


---


## 121. Program Navigation and Screen Transition Control
As an interactive user, I want the application to route me to the correct screen based on the function key I press so that I can navigate the card demonstration application consistently.

### Requirements

REQ-F-1640: [Event-driven] When the program is invoked without a communication area, the system shall set the destination program to the sign-on program and transfer control to it.

REQ-F-1641: [Event-driven] When a communication area is passed to the program, the system shall restore the communication area from the caller into working storage, preserving all navigation state and context information for subsequent processing.

REQ-F-1642: [Event-driven] When the PF3 key is pressed, the system shall set the destination program to the administration program if no originating program is recorded; otherwise the system shall set the destination program to the originating program name.

REQ-F-1643: [Event-driven] When the PF12 key is pressed, the system shall set the destination program to the administration program and transfer control to it.

REQ-F-1644: [Ubiquitous] The system shall validate the destination program name before transferring control; if the destination program name is empty, the system shall default it to the sign-on program.

REQ-F-1645: [Ubiquitous] The system shall populate the communication area with the current transaction identifier and program name, reset the program context indicator to entry status, and transfer control to the destination program passing the updated communication area.


---


## 122. User Deletion Screen Management and Security File Operations
As an operator, I want to search for users by ID, view their details, and delete them from the security data store so that user accounts can be removed when required.

### Requirements

REQ-F-1646: [Event-driven] When the program is invoked with a communication area present, the system shall copy the communication area into the local communication area structure to access any pre-selected user information or navigation state passed by the caller; when no communication area is present, the system shall proceed with fresh initialization.

REQ-F-1647: [Ubiquitous] The system shall clear the error flag to indicate no error condition before processing any user interactions.

REQ-F-1648: [Ubiquitous] The system shall clear the message text buffer and error message output field before displaying the user deletion screen.

REQ-F-1649: [Ubiquitous] The system shall retrieve the current system date and time, format the date as MM/DD/YY and the time as HH:MM:SS, and populate the screen header with the transaction identifier, program name, screen titles, formatted date, and formatted time.

REQ-F-1650: [Ubiquitous] The system shall populate the screen header with current date, time, transaction identifier, and program name; move the message text to the error message field; and send the user deletion screen to the terminal.

REQ-F-1651: [Event-driven] When the program is invoked for the first time (program context is not re-entry), the system shall set the user ID input field cursor to the first position; if a user ID was pre-selected by the caller, the system shall populate the user ID input field and process it as an Enter key action; otherwise the system shall display the blank screen.

REQ-F-1652: [Ubiquitous] The system shall receive the user's input from the user deletion screen, capturing the user ID and other fields, and record any response or reason codes.

REQ-F-1653: [Event-driven] When the program is re-entered after the user has interacted with the screen, the system shall evaluate the function key pressed and route to the appropriate handler: the Enter key processes the user lookup request, PF4 clears the screen, PF5 initiates user deletion, and any other key displays an invalid-key error message.

REQ-F-1654: [Event-driven] When the Enter key is pressed, the system shall validate that the user ID is not empty; if the user ID is empty, the system shall set the error flag and display an error message with the user ID field repositioned for correction.

REQ-F-1655: [Event-driven] When the Enter key is pressed and a valid user ID is provided, the system shall clear the first name, last name, and user type fields, retrieve the matching user record from the user security data store (AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS) using the user ID as the key, and if retrieval succeeds, populate the first name, last name, and user type fields and display the user details.

REQ-F-1656: [Event-driven] When a user record lookup is performed and the user ID is not found in the user security data store, the system shall set the error flag and display a not-found message with the user ID field repositioned for correction.

REQ-F-1657: [Event-driven] When a user record lookup is performed and an error other than not-found occurs, the system shall set the error flag and display an error message with the first name field repositioned for correction.

REQ-F-1658: [Event-driven] When a user record is found during lookup, the system shall display a prompt instructing the operator to press PF5 to delete the user.

REQ-F-1659: [Event-driven] When the PF4 key is pressed, the system shall clear all screen input fields (user ID, first name, last name, user type) and the message text buffer, reset the user ID field cursor position, and send the cleared screen to the terminal.

REQ-F-1660: [Event-driven] When the PF5 key is pressed, the system shall validate that the user ID is not empty; if the user ID is empty, the system shall set the error flag and display an error message with the user ID field repositioned for correction.

REQ-F-1661: [Event-driven] When the PF5 key is pressed and a valid user ID is provided, the system shall retrieve the user record from the user security data store to confirm existence and then delete it, displaying the outcome message.

REQ-F-1662: [Event-driven] When a user deletion is requested and the deletion succeeds, the system shall clear all fields and display a success message.

REQ-F-1663: [Event-driven] When a user deletion is requested and the user record is not found, the system shall display a not-found message.

REQ-F-1664: [Event-driven] When a user deletion is requested and an error occurs, the system shall display an error message.


---



## Shared Capability Dependencies

This capability depends on the following shared capabilities.
Do not reimplement their behavior — integrate with the shared service.

- **COMEN01C** (`_shared/COMEN01C/`)
