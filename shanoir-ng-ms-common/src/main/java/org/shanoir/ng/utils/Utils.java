package org.shanoir.ng.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.shanoir.ng.shared.model.AbstractGenericItem;

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

	public static boolean equalsIgnoreNull(Object o1, Object o2) {
		if (o1 == null)
			return o2 == null;
		if (o2 == null)
			return o1 == null;
		if (o1 instanceof AbstractGenericItem && o2 instanceof AbstractGenericItem) {
			return ((AbstractGenericItem) o1).getId().equals(((AbstractGenericItem) o2).getId());
		}
		return o1.equals(o2) || o2.equals(o1);
		// o1.equals(o2) is not equivalent to o2.equals(o1) ! For instance with
		// java.sql.Timestamp and java.util.Date
	}
	public static LocalDate DateToLocalDate(Date date)  {
		return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
	}
}
