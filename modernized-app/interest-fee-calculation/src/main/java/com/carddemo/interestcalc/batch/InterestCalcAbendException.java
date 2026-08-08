package com.carddemo.interestcalc.batch;

/**
 * Thrown for conditions that made CBACT04C.cbl call 9999-ABEND-PROGRAM
 * (missing account, missing cross-reference, missing default discount
 * group). Failing the batch job/step is the Spring Batch equivalent of a
 * mainframe ABEND.
 */
public class InterestCalcAbendException extends RuntimeException {

    public InterestCalcAbendException(String message) {
        super(message);
    }
}
