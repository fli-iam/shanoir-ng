package org.shanoir.ng.dataset.modality;

/**
 * Spectroscopy dataset nature.
 * 
 * @author msimon
 *
 */
public enum SpectDatasetNature {

	// Nuclear medicine tomo dataset
	NUCLEAR_MEDICINE_TOMO_DATASET(1),

	// Nuclear medicine projection dataset
	NUCLEAR_MEDICINE_PROJECTION_DATASET(2);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private SpectDatasetNature(final int id) {
		this.id = id;
	}

	/**
	 * Get a spectroscopy dataset nature by its id.
	 * 
	 * @param id
	 *            nature id.
	 * @return spectroscopy dataset nature.
	 */
	public static SpectDatasetNature getNature(final Integer id) {
		if (id == null) {
			return null;
		}
		for (SpectDatasetNature nature : SpectDatasetNature.values()) {
			if (id.equals(nature.getId())) {
				return nature;
			}
		}
		throw new IllegalArgumentException("No matching spectroscopy dataset nature for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
