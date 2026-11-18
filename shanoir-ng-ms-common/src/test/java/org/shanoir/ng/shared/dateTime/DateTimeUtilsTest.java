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

package org.shanoir.ng.shared.dateTime;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests the parsing of DICOM date and time values.
 */
public class DateTimeUtilsTest {

    /**
     * The DICOM VR TM is HH[MM[SS[.FFFFFF]]]: all these forms are legal and the
     * missing components default to 0.
     */
    @Test
    public void testStringToLocalTimeAcceptsAllDicomTimeForms() {
        Assertions.assertEquals(LocalTime.of(12, 0, 0), DateTimeUtils.stringToLocalTime("12"));
        Assertions.assertEquals(LocalTime.of(12, 21, 0), DateTimeUtils.stringToLocalTime("1221"));
        Assertions.assertEquals(LocalTime.of(12, 21, 5), DateTimeUtils.stringToLocalTime("122105"));
    }

    /**
     * The fractional part holds one to six digits. 120000.00 is a legal value that
     * used to raise a DateTimeParseException, as only six digits were accepted.
     */
    @Test
    public void testStringToLocalTimeAcceptsOneToSixFractionalDigits() {
        Assertions.assertEquals(LocalTime.of(12, 0, 0), DateTimeUtils.stringToLocalTime("120000.00"));
        Assertions.assertEquals(LocalTime.of(12, 0, 0, 500000000), DateTimeUtils.stringToLocalTime("120000.5"));
        Assertions.assertEquals(LocalTime.of(13, 57, 5, 123456000), DateTimeUtils.stringToLocalTime("135705.123456"));
    }

    /**
     * The formats accepted before must keep working.
     */
    @Test
    public void testStringToLocalTimeStillAcceptsPreviousFormats() {
        Assertions.assertEquals(LocalTime.of(13, 57, 5), DateTimeUtils.stringToLocalTime("135705"));
        Assertions.assertEquals(LocalTime.of(13, 57, 5, 123456000), DateTimeUtils.stringToLocalTime("135705.123456"));
    }

    /**
     * DICOM pads values with a trailing space to reach an even length.
     */
    @Test
    public void testStringToLocalTimeIgnoresPadding() {
        Assertions.assertEquals(LocalTime.of(12, 21, 0), DateTimeUtils.stringToLocalTime("1221 "));
    }

    /**
     * A null, empty or blank value is an absent value, not an error: a DICOM
     * date/time tag can be present but empty (type 2).
     */
    @Test
    public void testStringToLocalTimeReturnsNullForAbsentValue() {
        Assertions.assertNull(DateTimeUtils.stringToLocalTime(null));
        Assertions.assertNull(DateTimeUtils.stringToLocalTime(""));
        Assertions.assertNull(DateTimeUtils.stringToLocalTime("  "));
    }

    /**
     * A value that is not a legal TM must still be reported as a parse error, so
     * that callers can distinguish it from an absent value.
     */
    @Test
    public void testStringToLocalTimeRejectsInvalidValue() {
        Assertions.assertThrows(DateTimeParseException.class, () -> DateTimeUtils.stringToLocalTime("not-a-time"));
        Assertions.assertThrows(DateTimeParseException.class, () -> DateTimeUtils.stringToLocalTime("1200000"));
        Assertions.assertThrows(DateTimeParseException.class, () -> DateTimeUtils.stringToLocalTime("250000"));
    }

    /**
     * The DICOM VR DA is YYYYMMDD, and an empty value means absent.
     */
    @Test
    public void testPacsStringToLocalDate() {
        Assertions.assertEquals(java.time.LocalDate.of(1995, 10, 31), DateTimeUtils.pacsStringToLocalDate("19951031"));
        Assertions.assertNull(DateTimeUtils.pacsStringToLocalDate(null));
        Assertions.assertNull(DateTimeUtils.pacsStringToLocalDate(""));
    }
}
