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
