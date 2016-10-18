package org.shanoir.challengeScores.utils;

import java.util.ArrayList;
import java.util.List;

public class Utils {

	/**
	 * Convert an Iterable to a List
	 *
	 * @param iterable
	 * @return a List
	 */
	public static <E> List<E> toList(Iterable<E> iterable) {
	    List<E> list = new ArrayList<E>();
	    for (E item : iterable) {
	        list.add(item);
	    }
	    return list;
	}


	/**
	 * Check equality, manage null values
	 *
	 * @param name
	 * @param name2
	 * @return
	 */
	public static boolean equals(Object obj1, Object obj2) {
		if (obj1 == null) {
			return obj2 == null;
		} else {
			return obj1.equals(obj2);
		}
	}


	/**
	 * Join an Iterable
	 *
	 * @param iterable
	 * @param separator
	 * @return a String
	 */
	public static String join(Iterable<?> iterable, String separator) {
		StringBuilder str = new StringBuilder();
		boolean first = true;
		for (Object obj : iterable) {
			if (first) {
				first = false;
			} else {
				str.append(separator);
			}
			str.append(obj.toString());
		}
		return str.toString();
	}
}
