package org.shanoir.ng.preclinical.therapies;


public enum TherapyType {

	DRUG(Values.DRUG),
	RADIATION(Values.RADIATION),
	SURGERY(Values.SURGERY),
	ULTRASOUND(Values.ULTRASOUND);

	private String value;

	/**
	 * Constructor.
	 *
	 * @param val
	 *            value
	 */
	private TherapyType(final String value) {
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
		public static final String DRUG = "Drug";
		public static final String RADIATION = "Radiation";
		public static final String SURGERY = "Surgery";
		public static final String ULTRASOUND = "Ultrasound";
		
	}

}
