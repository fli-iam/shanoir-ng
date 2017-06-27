package org.shanoir.ng.manufacturermodel;

/**
 * Dataset modality type.
 * 
 * @author msimon
 *
 */
public enum DatasetModalityType {

	/**
	 * MR dataset.
	 */
	MR_DATASET(1),

	/**
	 * MEG Dataset.
	 */
	MEG_DATASET(2),

	/**
	 * CT Dataset.
	 */
	CT_DATASET(3),

	/**
	 * SPECT Dataset.
	 */
	SPECT_DATASET(4),

	/**
	 * PET Dataset
	 */
	PET_DATASET(5),

	/**
	 * EEG Dataset
	 */
	EEG_DATASET(6);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private DatasetModalityType(final int id) {
		this.id = id;
	}

	/**
	 * Get a dataset modality type by its id.
	 * 
	 * @param id
	 *            type id.
	 * @return dataset modality type.
	 */
	public static DatasetModalityType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (DatasetModalityType type : DatasetModalityType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching dataset modality type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
