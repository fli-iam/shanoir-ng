package org.shanoir.ng.preclinical.extra_data;


public enum ExtraDataType {

	EXTRADATA(Values.EXTRADATA),
	PHYSIOLOGICALDATA(Values.PHYSIOLOGICALDATA),
	BLOODGASDATA(Values.BLOODGASDATA);
	
	private String value;

	/**
	 * Constructor.
	 *
	 * @param val
	 *            value
	 */
	private ExtraDataType(final String value) {
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
		public static final String EXTRADATA = "extradata";
		public static final String PHYSIOLOGICALDATA = "physiologicaldata";
		public static final String BLOODGASDATA = "bloodgasdata";
		
	}

}
