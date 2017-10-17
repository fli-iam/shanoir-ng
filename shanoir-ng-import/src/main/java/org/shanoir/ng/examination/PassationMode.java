package org.shanoir.ng.examination;


/**
 * PassationMode.
 * 
 * @author ifakhfakh
 *
 */


public enum PassationMode {

	/***
	 *  Questionnaire.
	 */
	QUESTIONNAIRE(1),
	
	/**
	 * Test-instrument.
	 */
	TEST_INSTRUMENT(2);
		

	private int id;
	

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private PassationMode(final int id) {
		this.id = id;
	}

	/**
	 * Get a passation mode by its id.
	 * 
	 * @param id
	 *            passation mode id.
	 * @return passation mode.
	 */
	public static PassationMode getPassationMode(final Integer id) {
		if (id == null) {
			return null;
		}
		for (PassationMode passationMode : PassationMode.values()) {
			if (id.equals(passationMode.getId())) {
				return passationMode;
			}
		}
		throw new IllegalArgumentException("No matching passation mode for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
