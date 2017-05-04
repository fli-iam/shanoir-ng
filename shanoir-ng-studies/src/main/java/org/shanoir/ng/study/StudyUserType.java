package org.shanoir.ng.study;

/**
 * @author msimon
 *
 */
public enum StudyUserType {

	/**
	 * Is responsible for the research study.
	 */
	RESPONSIBLE(Values.RESPONSIBLE),

	/**
	 * Can see, download, import datasets and modify the study parameters.
	 */
	SEE_DOWNLOAD_IMPORT_MODIFY(Values.SEE_DOWNLOAD_IMPORT_MODIFY),

	/**
	 * Can see, download and import datasets.
	 */
	SEE_DOWNLOAD_IMPORT(Values.SEE_DOWNLOAD_IMPORT),

	/**
	 * Cannot see or download datasets.
	 */
	NOT_SEE_DOWNLOAD(Values.NOT_SEE_DOWNLOAD),

	/**
	 * Can see and download datasets.
	 */
	SEE_DOWNLOAD(Values.SEE_DOWNLOAD);

	private String value;

	/**
	 * Constructor.
	 * 
	 * @param val
	 *            value
	 */
	private StudyUserType(final String value) {
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
		public static final String RESPONSIBLE = "Is responsible for the research study";
		public static final String SEE_DOWNLOAD_IMPORT_MODIFY = "Can see, download, import datasets and modify the study parameters";
		public static final String SEE_DOWNLOAD_IMPORT = "Can see, download and import datasets";
		public static final String NOT_SEE_DOWNLOAD = "Cannot see or download datasets";
		public static final String SEE_DOWNLOAD = "Can see and download datasets";
	}

}
