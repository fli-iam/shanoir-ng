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

<<<<<<< HEAD:shanoir-ng-datasets/src/main/java/org/shanoir/ng/datasetacquisition/model/mr/MrScanningSequence.java
package org.shanoir.ng.datasetacquisition.model.mr;
=======
package org.shanoir.ng.datasetacquisition.mr;
>>>>>>> upstream/develop:shanoir-ng-datasets/src/main/java/org/shanoir/ng/datasetacquisition/mr/MrScanningSequence.java

/**
 * Scanning Sequence.
 * 
 * @author atouboul
 *
 */
public enum MrScanningSequence {

	// Spin Echo
	SE(1),

	// Inversion Recovery
	IR(2),

	// Gradient Recalled
	GR(3),

	// Echo Planar
	EP(4),
	
	//Research Mode
	RM(5);
	
	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private MrScanningSequence(final int id) {
		this.id = id;
	}

	/**
	 * Get a Scanning Sequence by its id.
	 * 
	 * @param id
	 *            sequence id.
	 * @return Scanning Sequence.
	 */
	public static MrScanningSequence getScanningSequence(final Integer id) {
		if (id == null) {
			return null;
		}
		for (MrScanningSequence scanningSequence : MrScanningSequence.values()) {
			if (id.equals(scanningSequence.getId())) {
				return scanningSequence;
			}
		}
		throw new IllegalArgumentException("No matching scanning sequence for id " + id);
	}
	
	/**
	 * Get a Scanning Sequence by its name.
	 * 
	 * @param type
	 *            sequence id.
	 * @return Scanning Sequence.
	 */
	public static MrScanningSequence getIdByType(final String type) {
		if (type == null) {
			return null;
		}
		return MrScanningSequence.valueOf(type);
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
