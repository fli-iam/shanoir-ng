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

<<<<<<< HEAD:shanoir-ng-datasets/src/main/java/org/shanoir/ng/examination/model/PassationMode.java
package org.shanoir.ng.examination.model;
=======
package org.shanoir.ng.examination;
>>>>>>> upstream/develop:shanoir-ng-datasets/src/main/java/org/shanoir/ng/examination/PassationMode.java

/**
 * Passation mode.
 * 
 * @author ifakhfakh
 *
 */
public enum PassationMode {

	/***
	 * Questionnaire.
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
	public static PassationMode getMode(final Integer id) {
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
