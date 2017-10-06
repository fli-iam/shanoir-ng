package org.shanoir.ng.importer.examination;


/**
 * InstrumentType.
 * 
 * @author ifakhfakh
 *
 */

public enum InstrumentType {

	/***
	 *  Behavioural instrument.
	 */
	BEHAVIOURAL_INSTRUMENT(1),
	
	/**
	 * Experimental psychology instrument.
	 */
	EXPERIMENTAL_PSYCHOLOGY_INSTRUMENT(2),
	
	/**
	 * Neuroclinical instrument.
	 */
	NEUROCLINICAL_INSTRUMENT(3),
	
	/**
	 * Neuropsychological instrument.
	 */
	NEUROPSYCHOLOGICAL_INSTRUMENT(4),
	
	/**
	 * Psychological instrument.
	 */
	PSYCHOLOGICAL_INSTRUMENT(5),
	
	/**
	 * Psychophysical instrument.
	 */
	PSYCHOPHYSICAL_INSTRUMENT(6);
	

	private int id;
	

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private InstrumentType(final int id) {
		this.id = id;
	}

	/**
	 * Get an instrument type by its id.
	 * 
	 * @param id
	 *            instrument type id.
	 * @return instrument type.
	 */
	public static InstrumentType getInstrumentType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (InstrumentType instrumentType : InstrumentType.values()) {
			if (id.equals(instrumentType.getId())) {
				return instrumentType;
			}
		}
		throw new IllegalArgumentException("No matching instrument type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
