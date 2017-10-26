package org.shanoir.ng.dataset;

/**
 * Processed dataset type.
 * 
 * @author msimon
 *
 */
public enum ProcessedDatasetType {

	// ReconstructedDataset
	RECONSTRUCTEDDATASET(1),

	// NonReconstructedDataset
	NONRECONSTRUCTEDDATASET(2);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private ProcessedDatasetType(final int id) {
		this.id = id;
	}

	/**
	 * Get a processed dataset type by its id.
	 * 
	 * @param id
	 *            type id.
	 * @return processed dataset type.
	 */
	public static ProcessedDatasetType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (ProcessedDatasetType type : ProcessedDatasetType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching processed dataset type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
