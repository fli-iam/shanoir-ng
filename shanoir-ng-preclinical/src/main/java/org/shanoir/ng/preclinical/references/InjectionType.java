package org.shanoir.ng.preclinical.references;


public enum InjectionType {

	BOLUS(Values.BOLUS),
	INFUSION(Values.INFUSION);

	private String value;

	/**
	 * Constructor.
	 *
	 * @param val
	 *            value
	 */
	private InjectionType(final String value) {
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
		public static final String BOLUS = "Bolus";
		public static final String INFUSION = "Infusion";
	}

}
