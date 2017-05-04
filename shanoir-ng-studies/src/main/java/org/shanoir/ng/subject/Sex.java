package org.shanoir.ng.subject;

public enum Sex {

	/**
	 * Male.
	 */
	M(Values.M),

	/**
	 * Female.
	 */
	F(Values.F);

	private String value;

	/**
	 * Constructor.
	 *
	 * @param val
	 *            value
	 */
	private Sex(final String value) {
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
		public static final String M = "M";
		public static final String F = "F";
	}

}
