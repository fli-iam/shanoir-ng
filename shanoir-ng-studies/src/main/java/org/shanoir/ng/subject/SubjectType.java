package org.shanoir.ng.subject;

/**
 * Subject type.
 * 
 * @author msimon
 *
 */
public enum SubjectType {

	/**
	 * Healthy volunteer.
	 */
	HEALTHY_VOLUNTEER(Values.HEALTHY_VOLUNTEER),

	/**
	 * Patient.
	 */
	PATIENT(Values.PATIENT),

	/**
	 * Phantom.
	 */
	PHANTOM(Values.PHANTOM);

	private String value;

	/**
	 * Constructor.
	 * 
	 * @param val
	 *            value
	 */
	private SubjectType(final String value) {
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
	 *
	 */
	public static class Values {
		public static final String HEALTHY_VOLUNTEER = "Healthy volunteer";
		public static final String PATIENT = "Patient";
		public static final String PHANTOM = "Phantom";
	}

}
