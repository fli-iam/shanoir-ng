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

package org.shanoir.ng.preclinical.references;


public enum Frequency {

	DAILY(Values.DAILY),
	WEEKLY(Values.WEEKLY),
	MONTHLY(Values.MONTHLY),
	BIMONTHLY(Values.BIMONTHLY),
	BIANNUALLY(Values.BIANNUALLY),
	ANNUALLY(Values.ANNUALLY);

	private String value;

	/**
	 * Constructor.
	 *
	 * @param val
	 *            value
	 */
	private Frequency(final String value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * List of enum values.
	 *
	 */
	public static class Values {
		public static final String DAILY = "Daily";
		public static final String WEEKLY = "Weekly";
		public static final String MONTHLY = "Monthly";
		public static final String BIMONTHLY = "Bimonthly";
		public static final String BIANNUALLY = "Biannually";
		public static final String ANNUALLY = "Annually";
	}

}
