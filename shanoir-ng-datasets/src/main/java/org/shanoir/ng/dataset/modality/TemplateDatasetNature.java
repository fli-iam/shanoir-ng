package org.shanoir.ng.dataset.modality;

/**
 * Template dataset nature.
 * 
 * @author msimon
 *
 */
public enum TemplateDatasetNature {

	// T1 weighted MR template dataset
	T1_WEIGHTED_MR_TEMPLATE_DATASET(1),

	// T2 weighted MR template dataset
	T2_WEIGHTED_MR_TEMPLATE_DATASET(2),

	// Proton density weighted MR template dataset
	PROTON_DENSITY_WEIGHTED_MR_TEMPLATE_DATASET(3);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private TemplateDatasetNature(final int id) {
		this.id = id;
	}

	/**
	 * Get a template dataset nature by its id.
	 * 
	 * @param id
	 *            nature id.
	 * @return template dataset nature.
	 */
	public static TemplateDatasetNature getNature(final Integer id) {
		if (id == null) {
			return null;
		}
		for (TemplateDatasetNature nature : TemplateDatasetNature.values()) {
			if (id.equals(nature.getId())) {
				return nature;
			}
		}
		throw new IllegalArgumentException("No matching template dataset nature for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
