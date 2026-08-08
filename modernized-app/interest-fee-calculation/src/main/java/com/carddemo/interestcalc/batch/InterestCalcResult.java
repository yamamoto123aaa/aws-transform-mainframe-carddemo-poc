package com.carddemo.interestcalc.batch;

/** Summary of one InterestCalcProcessor#run invocation (WS-RECORD-COUNT etc. in the original). */
public class InterestCalcResult {

    private final int tcatBalRecordsRead;
    private final int transactionsWritten;

    public InterestCalcResult(int tcatBalRecordsRead, int transactionsWritten) {
        this.tcatBalRecordsRead = tcatBalRecordsRead;
        this.transactionsWritten = transactionsWritten;
    }

    public int getTcatBalRecordsRead() {
        return tcatBalRecordsRead;
    }

    public int getTransactionsWritten() {
        return transactionsWritten;
    }
}
