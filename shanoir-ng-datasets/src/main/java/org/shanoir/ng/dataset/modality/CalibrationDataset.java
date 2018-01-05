package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.Dataset;

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

}
