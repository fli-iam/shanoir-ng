package org.shanoir.ng.dataset.modality;

/**
 * Registration dataset type.
 * 
 * @author msimon
 *
 */
public enum RegistrationDatasetType {

	// Displacement Field Dataset
	DISPLACEMENT_FIELD_DATASET(1);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private RegistrationDatasetType(final int id) {
		this.id = id;
	}

	/**
	 * Get a registration dataset type by its id.
	 * 
	 * @param id
	 *            type id.
	 * @return registration dataset type.
	 */
	public static RegistrationDatasetType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (RegistrationDatasetType type : RegistrationDatasetType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching registration dataset type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
