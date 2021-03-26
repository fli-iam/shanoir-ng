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

package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.shared.core.model.AbstractEntity;

/**
 * MR dataset metadata that could be updated by study card.
 * 
 * @author msimon
 *
 */
@Entity
public class MrDatasetMetadata extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 2523777086183952849L;

	/** MR Dataset Nature. */
	private Integer mrDatasetNature;

	/**
	 * @return the mrDatasetNature
	 */
	public MrDatasetNature getMrDatasetNature() {
		return MrDatasetNature.getNature(mrDatasetNature);
	}

	/**
	 * @param mrDatasetNature
	 *            the mrDatasetNature to set
	 */
	public void setMrDatasetNature(MrDatasetNature mrDatasetNature) {
		if (mrDatasetNature == null) {
			this.mrDatasetNature = null;
		} else {
			this.mrDatasetNature = mrDatasetNature.getId();
		}
	}

}
