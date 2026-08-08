# Shared Capability: COMEN01C

## 1. Program Entry and Communication Area Handling

As a calling program, I want the navigation controller to validate the incoming session context and restore prior navigation state so that subsequent processing has a consistent baseline.

### Requirements

REQ-F-001: [Event-driven] WHEN the program is invoked and no communication area is present (communication area length is zero), THE navigation controller SHALL immediately transfer control to the sign-on screen program.

REQ-F-002: [Event-driven] WHEN the program is invoked and the communication area length is greater than zero, THE navigation controller SHALL restore the incoming communication area into the local communication area record, preserving the originating transaction identifier, originating program name, and program context indicator.

REQ-F-003: [Ubiquitous] THE navigation controller SHALL clear the error flag indicator to OFF state and initialize the message text buffer to spaces at the start of each invocation.

---

## 2. First Invocation vs. Re-entry Dispatch

As a menu user, I want the system to distinguish between a first-time display and a return visit so that the correct screen state is presented each time.

### Requirements

REQ-F-004: [Event-driven] WHEN the communication area is present and the program context indicator indicates first invocation (not re-entry), THE navigation controller SHALL display the menu screen with cleared output fields.

REQ-F-005: [Event-driven] WHEN the communication area is present and the program context indicator indicates re-entry, THE navigation controller SHALL receive the user's menu screen input, capture the response status and reason code, and dispatch based on the attention identifier pressed.

REQ-F-006: [Event-driven] WHEN the attention identifier indicates the ENTER key was pressed during re-entry processing, THE navigation controller SHALL route to the menu option processing handler.

REQ-F-007: [Event-driven] WHEN the attention identifier indicates the PF3 key was pressed during re-entry processing, THE navigation controller SHALL set the destination program to the sign-on screen program and transfer control to it.

REQ-F-008: [Event-driven] WHEN any key other than Enter or PF3 is pressed during re-entry, THE Menu Screen Handler SHALL display an invalid-key error message and send the menu screen.

---

## 3. Menu Option Input Normalization and Validation

As a menu user, I want my option selection to be validated before any transfer occurs so that only valid, authorized choices are processed.

### Requirements

REQ-F-009: [Ubiquitous] THE menu option input normalizer SHALL trim trailing spaces from the menu option input by scanning from the end of the input backward to the last non-space character, replace any remaining spaces in the selection with zeros, and convert the resulting alphanumeric value to numeric form.

REQ-F-010: [Unwanted] WHEN the normalized menu option is non-numeric, exceeds the available menu option count of 11, or equals zero, THE menu option validator SHALL set the error flag indicator to 'Y' and store the error message 'Please enter a valid option number...' in the message text.

REQ-F-011: [Event-driven] WHEN a regular user selects a menu option whose authorization code is 'A' (restricted to administrators only), THE menu authorization enforcer SHALL set the error flag indicator to 'Y', store the error message 'No access - Admin Only option... ' in the message text, and send the menu screen.

---

## 4. Menu Option Transfer Handling

As a menu user, I want a validated selection to transfer me to the correct target program so that I reach the intended function.

### Requirements

REQ-F-012: [Complex] WHILE the error flag indicator is not set AND the selected menu option program name is 'COPAUS0C', THE program transfer handler SHALL verify that the target program is available; when available, populate the communication area with the originating transaction identifier and originating program name, reset the program context indicator to zero, and transfer control to the target program; when the target program is not available, set the error message color to red and display an error message indicating the option is not installed.

REQ-F-013: [Complex] WHILE the error flag indicator is not set AND the selected menu option program name begins with 'DUMMY', THE menu option status handler SHALL set the error message color to green and display an informational message constructed by concatenating 'This option ', the menu option display name, and 'is coming soon ...'.

REQ-F-014: [Complex] WHILE the error flag indicator is not set AND the selected menu option is a standard option (not 'COPAUS0C' and not beginning with 'DUMMY'), THE menu option processor SHALL populate the communication area with the originating transaction identifier and originating program name, reset the program context indicator to zero, and transfer control to the selected menu option program.

---

## 5. Destination Validation Before Transfer

As a calling program, I want the navigation controller to guarantee a valid transfer target is always set so that control is never transferred to an empty destination.

### Requirements

REQ-F-015: [State-driven] WHEN the program is about to transfer control and the destination program name is empty or uninitialized (contains LOW-VALUES or SPACES), THE navigation controller SHALL default the destination program name to the sign-on screen program before transferring control.

REQ-F-016: [Ubiquitous] THE navigation controller SHALL transfer control to the destination program specified in the communication area as its final operation.

---

## 6. Menu Screen Display

As a menu user, I want the menu screen to be fully populated with current header information and all available options so that I can make an informed selection.

### Requirements

REQ-F-017: [Ubiquitous] THE menu screen header populator SHALL retrieve the current system date and time, populate the screen title lines with 'AWS Mainframe Modernization' and 'CardDemo', populate the transaction identifier field with 'CM00' and the program name field with 'COMEN01C', format and display the current date as MM/DD/YY, and format and display the current time as HH:MM:SS.

REQ-F-018: [Ubiquitous] THE menu screen sender SHALL populate the menu screen header information and all menu option display lines, move the accumulated message text to the error message output field, and send the menu screen to the terminal with the ERASE option to clear the screen before display.

---

## 7. Menu Option Assembly

As a menu user, I want all configured menu options to be formatted and displayed in their correct positions so that the full menu is visible.

### Requirements

REQ-F-019: [Ubiquitous] THE menu option assembly SHALL initialize the loop counter to 1 and iterate through each menu option up to the configured count of 11.

REQ-F-020: [Event-driven] WHEN the loop counter reaches 1, THE menu option assembly SHALL format the menu option text by concatenating the sequence number, a period and space delimiter, and the display name, then assign the result to menu option output position 1.

REQ-F-021: [Event-driven] WHEN the loop counter reaches 2, THE menu option assembly SHALL format the menu option text by concatenating the sequence number, a period and space delimiter, and the display name, then assign the result to menu option output position 2.

REQ-F-022: [Event-driven] WHEN the loop counter reaches 3, THE menu option assembly SHALL format the menu option text by concatenating the sequence number, a period and space delimiter, and the display name, then assign the result to menu option output position 3.

REQ-F-023: [Event-driven] WHEN the loop counter reaches 4, THE menu option assembly SHALL format the menu option text by concatenating the sequence number, a period and space delimiter, and the display name, then assign the result to menu option output position 4.

REQ-F-024: [Event-driven] WHEN the loop counter reaches 5, THE menu option assembly SHALL format the menu option text by concatenating the sequence number, a period and space delimiter, and the display name, then assign the result to menu option output position 5.

REQ-F-025: [Event-driven] WHEN the loop counter reaches 6, THE menu option assembly SHALL format the menu option text by concatenating the sequence number, a period and space delimiter, and the display name, then assign the result to menu option output position 6.

REQ-F-026: [Event-driven] WHEN the loop counter reaches 7, THE menu option assembly SHALL format the menu option text by concatenating the sequence number, a period and space delimiter, and the display name, then assign the result to menu option output position 7.

REQ-F-027: [Event-driven] WHEN the loop counter reaches 8, THE menu option assembly SHALL format the menu option text by concatenating the sequence number, a period and space delimiter, and the display name, then assign the result to menu option output position 8.

REQ-F-028: [Event-driven] WHEN the loop counter reaches 9, THE menu option assembly SHALL format the menu option text by concatenating the sequence number, a period and space delimiter, and the display name, then assign the result to menu option output position 9.

REQ-F-029: [Event-driven] WHEN the loop counter reaches 10, THE menu option assembly SHALL format the menu option text by concatenating the sequence number, a period and space delimiter, and the display name, then assign the result to menu option output position 10.

REQ-F-030: [Event-driven] WHEN the loop counter reaches 11, THE menu option assembly SHALL format the menu option text by concatenating the sequence number, a period and space delimiter, and the display name, then assign the result to menu option output position 11.

REQ-F-031: [Event-driven] WHEN the loop counter reaches 12, THE menu option assembly SHALL format the menu option text by concatenating the sequence number, a period and space delimiter, and the display name, then assign the result to menu option output position 12.

REQ-F-032: [Unwanted] WHEN the loop counter exceeds the range of defined menu option positions (1 through 12), THE menu option assembly SHALL take no action and continue to the next loop iteration.

REQ-F-033: [Ubiquitous] THE menu option assembly SHALL terminate the loop after all menu options have been processed.

### Open Questions

OQ-001: Rules 7ad957ea and 8bf90c8b both state the maximum valid menu option count is 11, while rule 04a5424a states the assembly loop iterates up to 11 options, yet rules c7c03249 and 32d5bdc1 describe a 12th output position. It is unclear whether 12 is a valid selectable option or solely a display-only slot. Owner: Business/Product Owner.

OQ-002: Rule 32d5bdc1 states the out-of-range branch covers loop counter values outside positions 1 through 12, implying 12 is the upper bound of defined positions. However, the validation rules cap selectable options at 11. Clarification is needed on whether position 12 is ever populated during normal operation and whether it is selectable. Owner: Business/Product Owner.

OQ-003: Rules 31f64527 and fc55f924 both describe the 'COPAUS0C' transfer path with program availability checking. It is unclear whether the availability check applies exclusively to 'COPAUS0C' or whether it is also required for standard options covered by rule 1bae53bd. Owner: Business/Product Owner.
