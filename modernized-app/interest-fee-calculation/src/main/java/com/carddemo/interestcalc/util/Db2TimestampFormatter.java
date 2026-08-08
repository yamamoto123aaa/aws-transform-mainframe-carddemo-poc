package com.carddemo.interestcalc.util;

import java.time.LocalDateTime;

/**
 * Replicates paragraph Z-GET-DB2-FORMAT-TIMESTAMP from CBACT04C.cbl.
 *
 * <p>COBOL's {@code FUNCTION CURRENT-DATE} returns
 * {@code YYYYMMDDHHMISSuu+HHMM} (21 chars; {@code uu} = hundredths of a
 * second, not milliseconds). CBACT04C copies YYYY/MM/DD/HH/MI/SS/uu into a
 * DB2 timestamp layout and hardcodes the last 4 digits to {@code "0000"}
 * (it does not use the UTC offset at all). Result: 26 chars,
 * {@code YYYY-MM-DD-HH.MI.SS.uu0000} — note this is a 2-digit hundredths
 * field, not 3-digit milliseconds.
 */
public final class Db2TimestampFormatter {

    private Db2TimestampFormatter() {
    }

    public static String format(LocalDateTime dateTime) {
        int hundredths = dateTime.getNano() / 10_000_000; // 2-digit centiseconds, matches COB-MIL
        return String.format(
                "%04d-%02d-%02d-%02d.%02d.%02d.%02d0000",
                dateTime.getYear(),
                dateTime.getMonthValue(),
                dateTime.getDayOfMonth(),
                dateTime.getHour(),
                dateTime.getMinute(),
                dateTime.getSecond(),
                hundredths);
    }

    public static String formatNow() {
        return format(LocalDateTime.now());
    }
}
