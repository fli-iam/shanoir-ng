/**
 * 
 */
package org.shanoir.ng.shared.dateTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author yyao
 *
 */
public class DateTimeUtils {
	
	public static LocalDate dateToLocalDate(Date date) {
		if (date == null) return null;
		else return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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

}
