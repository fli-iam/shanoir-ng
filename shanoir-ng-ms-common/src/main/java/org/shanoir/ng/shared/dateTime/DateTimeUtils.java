/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

/**
 *
 */
package org.shanoir.ng.shared.dateTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;

/**
 * @author yyao
 *
 */
public final class DateTimeUtils {

    private DateTimeUtils() { }

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static LocalDate dateToLocalDate(Date date) {
        if (date == null) return null;
        else return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date localDateToDate(LocalDate localDate) {
        if (localDate == null) return null;
        // Here we use UTC, otherwise the date can be "changed" if the system is not in UTC
        else return Date.from(localDate.atStartOfDay().atZone(ZoneId.of("UTC")).toInstant());
    }

    /**
     * The DICOM VR TM is HH[MM[SS[.FFFFFF]]], where the fractional part holds one
     * to six digits, so HH, HHMM, HHMMSS and HHMMSS.F to HHMMSS.FFFFFF are all
     * legal. Values are also padded with a trailing space to an even length.
     *
     * Only HHMMSS and HHMMSS.FFFFFF used to be accepted, so a legal value such as
     * 120000.00 raised a DateTimeParseException. The components that are not
     * present default to 0.
     */
    private static final DateTimeFormatter DICOM_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .optionalStart()
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 6, true)
            .optionalEnd()
            .optionalEnd()
            .optionalEnd()
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
            .toFormatter();

    public static LocalTime stringToLocalTime(String time) {
        if (time == null) return null;
        String trimmedTime = time.trim();
        if (trimmedTime.isEmpty()) return null;
        return LocalTime.parse(trimmedTime, DICOM_TIME_FORMATTER);
    }

    public static LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) return null;
        else return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static LocalDate pacsStringToLocalDate(String yyyyMMdd) {
        if (yyyyMMdd != null && !yyyyMMdd.isEmpty()) {
            final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
            LocalDate localDate = LocalDate.parse(yyyyMMdd, dtf);
            return localDate;
        } else {
            return null;
        }
    }

    public static String localDateToSolrString(LocalDate localDate) {
        if (localDate == null) return null;
        else {
            LocalDateTime ldt = localDate.atTime(0, 0, 0);
            ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
            final DateTimeFormatter dtf = DateTimeFormatter.ISO_INSTANT;
            return zdt.format(dtf);
        }
    }

}
