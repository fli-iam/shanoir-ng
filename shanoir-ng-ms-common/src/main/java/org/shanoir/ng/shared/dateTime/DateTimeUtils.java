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
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * @author yyao
 *
 */
public class DateTimeUtils {

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

	public static LocalTime stringToLocalTime(String time) {
		if (time == null || time.isEmpty()) return null;

		DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("HHmmss.SSSSSS");
		DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("HHmmss");

		try {
            return LocalTime.parse(time, fullFormatter);
        } catch (DateTimeParseException e) {
            return LocalTime.parse(time, shortFormatter);
        }
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
