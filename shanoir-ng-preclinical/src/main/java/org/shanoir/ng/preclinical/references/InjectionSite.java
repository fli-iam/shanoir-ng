package org.shanoir.ng.preclinical.references;


public enum InjectionSite {

	CAUDAL_VEIN(Values.CAUDAL_VEIN),
	INTRACEREBRAL(Values.INTRACEREBRAL);

	private String value;

	/**
	 * Constructor.
	 *
	 * @param val
	 *            value
	 */
	private InjectionSite(final String value) {
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
		public static final String CAUDAL_VEIN = "Caudal Vein";
		public static final String INTRACEREBRAL = "Intracerebral";
	}

}
