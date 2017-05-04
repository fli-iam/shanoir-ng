package org.shanoir.ng.subject;


public enum ImagedObjectCategory {

	PHANTOM(Values.PHANTOM),
	LIVING_HUMAN_BEING(Values.LIVING_HUMAN_BEING),
	HUMAN_CADAVER(Values.HUMAN_CADAVER),
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
		public static final String PHANTOM = "Phantom";
		public static final String LIVING_HUMAN_BEING = "Living human being";
		public static final String HUMAN_CADAVER = "Human cadaver";
		public static final String ANATOMICAL_PIECE = "Anatomical piece";
	}

}
