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
	MR_DATASET(Values.MR_DATASET),

	/**
	 * MEG Dataset.
	 */
	MEG_DATASET(Values.MEG_DATASET),

	/**
	 * CT Dataset.
	 */
	CT_DATASET(Values.CT_DATASET),

	/**
	 * SPECT Dataset.
	 */
	SPECT_DATASET(Values.SPECT_DATASET),

	/**
	 * PET Dataset
	 */
	PET_DATASET(Values.PET_DATASET),

	/**
	 * EEG Dataset
	 */
	EEG_DATASET(Values.EEG_DATASET);

	private String value;

	/**
	 * Constructor.
	 * 
	 * @param val
	 *            value
	 */
	private DatasetModalityType(final String value) {
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * List of enum values.
	 * 
	 * @author msimon
	 *
	 */
	public static class Values {
		public static final String MR_DATASET = "MR_DATASET";
		public static final String MEG_DATASET = "MEG_DATASET";
		public static final String CT_DATASET = "CT_DATASET";
		public static final String SPECT_DATASET = "SPECT_DATASET";
		public static final String PET_DATASET = "PET_DATASET";
		public static final String EEG_DATASET = "EEG_DATASET";
	}

}
