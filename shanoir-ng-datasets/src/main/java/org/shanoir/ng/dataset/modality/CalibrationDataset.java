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

import jakarta.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetType;

/**
 * Calibration dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class CalibrationDataset extends Dataset {
	
	/**
	 * UID
	 */
	private static final long serialVersionUID = -6251439584065614144L;

	/** Calibration Dataset Nature. */
	private Integer calibrationDatasetType;

	public CalibrationDataset() { }

	public CalibrationDataset(Dataset other) {
		super(other);
		if (((CalibrationDataset) other).getCalibrationDatasetType() != null) {
			this.calibrationDatasetType = ((CalibrationDataset) other).getCalibrationDatasetType().getId();
		} else {
			this.calibrationDatasetType = null;
		}
	}

	/**
	 * @return the calibrationDatasetType
	 */
	public CalibrationDatasetType getCalibrationDatasetType() {
		return CalibrationDatasetType.getType(calibrationDatasetType);
	}

	/**
	 * @param calibrationDatasetType
	 *            the calibrationDatasetType to set
	 */
	public void setCalibrationDatasetType(CalibrationDatasetType calibrationDatasetType) {
		if (calibrationDatasetType == null) {
			this.calibrationDatasetType = null;
		} else {
			this.calibrationDatasetType = calibrationDatasetType.getId();
		}
	}

	@Override
	public DatasetType getType() {
		return DatasetType.Calibration;
	}

}
