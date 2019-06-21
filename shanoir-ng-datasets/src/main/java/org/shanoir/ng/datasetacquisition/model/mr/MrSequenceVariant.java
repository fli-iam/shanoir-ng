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

<<<<<<< HEAD:shanoir-ng-datasets/src/main/java/org/shanoir/ng/datasetacquisition/model/mr/MrSequenceVariant.java
package org.shanoir.ng.datasetacquisition.model.mr;
=======
package org.shanoir.ng.datasetacquisition.mr;
>>>>>>> upstream/develop:shanoir-ng-datasets/src/main/java/org/shanoir/ng/datasetacquisition/mr/MrSequenceVariant.java

/**
 * Sequence Variant.
 * 
 * @author atouboul
 *
 */

public enum MrSequenceVariant {

	// segmented k-space
	SK(1),

	// magnetization transfer contrast
	MTC(2),

	// steady state
	SS(3),

	// time reversed steady state
	TRSS(4),
	
	// spoiled
	SP(5),
	
	// MAG prepared;
	MP(6),
	
	// oversampling phase 
	OSP(7),
	
	// no sequence variant
	NONE(8);
	
	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private MrSequenceVariant(final int id) {
		this.id = id;
	}

	/**
	 * Get a Sequence Variant by its id.
	 * 
	 * @param id Sequence Variant id.
	 * @return Sequence Variant.
	 */
	public static MrSequenceVariant getSequenceVariant(final Integer id) {
		if (id == null) {
			return null;
		}
		for (MrSequenceVariant sequenceVariant : MrSequenceVariant.values()) {
			if (id.equals(sequenceVariant.getId())) {
				return sequenceVariant;
			}
		}
		throw new IllegalArgumentException("No matching scanning sequence for id " + id);
	}
	
	/**
	 * Get an Sequence Variant by its name.
	 * 
	 * @param type
	 *            Sequence Variant
	 * @return Sequence Variant.
	 */
	public static MrSequenceVariant getIdByType(final String type) {
		if (type == null) {
			return null;
		}
		return MrSequenceVariant.valueOf(type);
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
