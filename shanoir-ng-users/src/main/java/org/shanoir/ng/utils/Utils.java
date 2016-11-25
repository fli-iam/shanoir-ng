package org.shanoir.ng.utils;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.model.exception.RestServiceException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * Utility class
 *
 * @author jlouis
 */
public class Utils {

	/**
	 * Convert Iterable to List
	 *
	 * @param iterable
	 * @return a list
	 */
	public static <E> List<E> toList(Iterable<E> iterable) {
		if (iterable instanceof List) {
			return (List<E>) iterable;
		}
		ArrayList<E> list = new ArrayList<E>();
		if (iterable != null) {
			for (E e : iterable) {
				list.add(e);
			}
		}
		return list;
	}


	/**
	 * Build a ready to use exception for field errors
	 * @param result
	 * @return
	 */
	public static RestServiceException buildFieldErrorException(BindingResult result) {
		StringBuilder msgStrBuilder = new StringBuilder();
		for (ObjectError objectError : result.getAllErrors()) {
			FieldError fieldError = (FieldError) objectError;
			if (msgStrBuilder.length() > 0) {
				msgStrBuilder.append("; ");
			}
			msgStrBuilder.append(fieldError.getField());
			msgStrBuilder.append(" : ");
			msgStrBuilder.append(fieldError.getDefaultMessage());
		}
		return new RestServiceException(422, msgStrBuilder.toString());
	}

}
