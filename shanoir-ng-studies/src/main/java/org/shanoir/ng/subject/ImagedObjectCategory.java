package org.shanoir.ng.subject;


public enum ImagedObjectCategory {

	PHANTOM(Values.PHANTOM),
	LIVING_HUMAN_BEING(Values.LIVING_HUMAN_BEING),
	HUNAM_CADAVER(Values.HUNAM_CADAVER),
	ANATOMICAL_PIECE(Values.ANATOMICAL_PIECE);

	private String value;

	/**
	 * Constructor.
	 * 
	 * @param val
	 *            value
	 */
	private ImagedObjectCategory(final String value) {
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
		public static final String PHANTOM = "PHANTOM";
		public static final String LIVING_HUMAN_BEING = "LIVING HUMAN BEING";
		public static final String HUNAM_CADAVER = "HUNAM CADAVER";
		public static final String ANATOMICAL_PIECE = "ANATOMICAL PIECE";
	}

}
