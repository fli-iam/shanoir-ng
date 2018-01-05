package org.shanoir.ng.dataset.modality;

/**
 * Calibration dataset type.
 * 
 * @author msimon
 *
 */
public enum CalibrationDatasetType {

	// Field Map Dataset
	FIELD_MAP_DATASET(1),

	// Voxel Displacement Map Dataset
	VOXEL_DISPLACEMENT_MAP_DATASET(2),

	// Bias Field Dataset
	BIAS_FIELD_DATASET(3);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private CalibrationDatasetType(final int id) {
		this.id = id;
	}

	/**
	 * Get a calibration dataset type by its id.
	 * 
	 * @param id
	 *            type id.
	 * @return calibration dataset type.
	 */
	public static CalibrationDatasetType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (CalibrationDatasetType type : CalibrationDatasetType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching calibration dataset type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
