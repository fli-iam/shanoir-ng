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
