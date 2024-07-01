package org.shanoir.uploader.gui;

/**
 * Date formatter for Birth Date and Examination date fields
 * 
 * @author ifakhfakh
 * 
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JFormattedTextField.AbstractFormatter;

public class DateLabelFormatter extends AbstractFormatter {

	private String datePattern = "dd/MM/yyyy";

	private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

	public Object stringToValue(String text) throws ParseException {
		return dateFormatter.parseObject(text);
	}

	public String valueToString(Object value) throws ParseException {
		if (value != null) {
			if (value instanceof Calendar) {
				Calendar cal = (Calendar) value;
				return dateFormatter.format(cal.getTime());
			}
			if (value instanceof Date) {
				return dateFormatter.format(value);
			}
		}
		return "";
	}

}
