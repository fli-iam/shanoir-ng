package org.shanoir.ng.datasetacquisition.model.mr;

/**
 * Patient position.
 * 
 * @author msimon
 *
 */
public enum PatientPosition {

	// Head First-Prone
	HEAD_FIRST_PRONE(1),

	// Head First-Decubitus Right
	HEAD_FIRST_DECUBITUS_RIGHT(2),

	// Feet First-Decubitus Right
	FEET_FIRST_DECUBITUS_RIGHT(3),

	// Feet First-Prone
	FEET_FIRST_PRONE(4),

	// Head First-Supine
	HEAD_FIRST_SUPINE(5),

	// Head First-Decubitus Left
	HEAD_FIRST_DECUBITUS_LEFT(6),

	// Feet First-Decubitus Left
	FEET_FIRST_DECUBITUS_LEFT(7),

	// Feet First-Supine
	FEET_FIRST_SUPINE(8);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private PatientPosition(final int id) {
		this.id = id;
	}

	/**
	 * Get a patient position by its id.
	 * 
	 * @param id
	 *            position id.
	 * @return patient position.
	 */
	public static PatientPosition getPosition(final Integer id) {
		if (id == null) {
			return null;
		}
		for (PatientPosition position : PatientPosition.values()) {
			if (id.equals(position.getId())) {
				return position;
			}
		}
		throw new IllegalArgumentException("No matching patient position for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
