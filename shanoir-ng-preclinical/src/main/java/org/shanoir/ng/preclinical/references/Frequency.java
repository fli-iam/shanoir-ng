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
