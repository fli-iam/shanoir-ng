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

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetType;

import jakarta.persistence.Entity;

/**
 * MESH dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class MeshDataset extends Dataset {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 5177847059488327065L;

	public MeshDataset() {}

	public MeshDataset(Dataset other) {
		super(other);
	}

	@Override
	public DatasetType getType() {
		return DatasetType.Mesh;
	}

}
