package org.shanoir.ng.preclinical.anesthetics.anesthetic;


public enum AnestheticType {

	GAS(Values.GAS),
	INJECTION(Values.INJECTION);

	private String value;

	/**
	 * Constructor.
	 *
	 * @param val
	 *            value
	 */
	private AnestheticType(final String value) {
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
		public static final String GAS = "Gas";
		public static final String INJECTION = "Injection";
	}

}
