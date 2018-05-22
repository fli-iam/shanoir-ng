package org.shanoir.ng.preclinical.references;


public enum InjectionInterval {

	BEFORE(Values.BEFORE),
	DURING(Values.DURING);

	private String value;

	/**
	 * Constructor.
	 *
	 * @param val
	 *            value
	 */
	private InjectionInterval(final String value) {
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
		public static final String BEFORE = "Before";
		public static final String DURING = "During";
	}

}
