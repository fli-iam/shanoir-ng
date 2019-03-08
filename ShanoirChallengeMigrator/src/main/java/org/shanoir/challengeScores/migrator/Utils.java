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

package org.shanoir.challengeScores.migrator;

public class Utils {

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


	public static String repeat(String string, int nb) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<nb; i++) {
			sb.append(string);
		}
		return sb.toString();
	}


	public static String join(Iterable<Object> iterable, String separator) {
		StringBuilder sb = new StringBuilder();
		for (Object obj : iterable) {
			if (sb.length() > 0) {
				sb.append(separator);
			}
			sb.append(obj);
		}
		return sb.toString();
	}

}
