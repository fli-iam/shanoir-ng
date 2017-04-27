package org.shanoir.ng.study;


public enum StudyStatus {

	/**
	 * finished.
	 */
	 FINISHED(Values.FINISHED),

	/**
	 * in_progress.
	 */
	IN_PROGRESS(Values.IN_PROGRESS);

	private String value;

	/**
	 * Constructor.
	 * 
	 * @param val
	 *            value
	 */
	private StudyStatus(final String value) {
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
		public static final String FINISHED = "FINISHED";
		public static final String IN_PROGRESS = "IN PROGRESS";
	}

}
