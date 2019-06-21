/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

<<<<<<< HEAD:shanoir-ng-datasets/src/main/java/org/shanoir/ng/examination/model/InstrumentType.java
package org.shanoir.ng.examination.model;
=======
package org.shanoir.ng.examination;
>>>>>>> upstream/develop:shanoir-ng-datasets/src/main/java/org/shanoir/ng/examination/InstrumentType.java

/**
 * Instrument type.
 * 
 * @author ifakhfakh
 *
 */
public enum InstrumentType {

	/***
	 * Behavioural instrument.
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
	public static InstrumentType getType(final Integer id) {
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
