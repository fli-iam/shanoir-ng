package org.shanoir.ng.dataset.model;

/**
 * Explored entity.
 * 
 * @author msimon
 *
 */
public enum ExploredEntity {

	// Anatomical Dataset
	ANATOMICAL_DATASET(1),

	// Functional Dataset
	FUNCTIONAL_DATASET(2),

	// Hemodynamic Dataset
	HEMODYNAMIC_DATASET(3),

	// Metabolic Dataset
	METABOLIC_DATASET(4),

	// Calibration
	CALIBRATION(5);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private ExploredEntity(final int id) {
		this.id = id;
	}

	/**
	 * Get an explored entity by its id.
	 * 
	 * @param id
	 *            explored entity id.
	 * @return explored entity.
	 */
	public static ExploredEntity getEntity(final Integer id) {
		if (id == null) {
			return null;
		}
		for (ExploredEntity entity : ExploredEntity.values()) {
			if (id.equals(entity.getId())) {
				return entity;
			}
		}
		throw new IllegalArgumentException("No matching explored entity for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
