package org.shanoir.ng.study;

/**
 * Study type.
 * 
 * @author msimon
 *
 */
public enum StudyType {

	/**
	 * Clinical.
	 */
	CLINICAL(Values.CLINICAL),

	/**
	 * Preclinical.
	 */
	PRECLINICAL(Values.PRECLINICAL),

	/**
	 * Methodological.
	 */
	SEE_DOWNLOAD_IMPORT_MODIFY(Values.METHODOLOGICAL);

	private String value;

	/**
	 * Constructor.
	 * 
	 * @param val
	 *            value
	 */
	private StudyType(final String value) {
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
		public static final String CLINICAL = "Clinical";
		public static final String PRECLINICAL = "Preclinical";
		public static final String METHODOLOGICAL = "Methodological";
	}

}
