package org.shanoir.ng.subject;



public enum HemisphericDominance {

	/**
	 * Left.
	 */
	LEFT(Values.LEFT),

	/**
	 * Right.
	 */
	RIGHT(Values.RIGHT);

	private String value;

	/**
	 * Constructor.
	 * 
	 * @param val
	 *            value
	 */
	private HemisphericDominance(final String value) {
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
		public static final String LEFT = "LEFT";
		public static final String RIGHT = "RIGHT";
	}

}
