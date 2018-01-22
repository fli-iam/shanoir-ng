package org.shanoir.ng.datasetacquisition.mr;

/**
 * Parallel acquisition technique.
 * 
 * @author msimon
 *
 */
public enum ParallelAcquisitionTechnique {

	// PILS
	PILS(1),

	// SENSE
	SENSE(2),

	// SMASH
	SMASH(3),

	// GRAPPA
	GRAPPA(4),

	// mSENSE
	M_SENSE(5);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private ParallelAcquisitionTechnique(final int id) {
		this.id = id;
	}

	/**
	 * Get a parallel acquisition technique by its id.
	 * 
	 * @param id
	 *            technique id.
	 * @return parallel acquisition technique.
	 */
	public static ParallelAcquisitionTechnique getTechnique(final Integer id) {
		if (id == null) {
			return null;
		}
		for (ParallelAcquisitionTechnique technique : ParallelAcquisitionTechnique.values()) {
			if (id.equals(technique.getId())) {
				return technique;
			}
		}
		throw new IllegalArgumentException("No matching parallel acquisition technique for id " + id);
	}

	/**
	 * Get a parallel acquisition technique by its id.
	 * 
	 * @param id
	 *            technique id.
	 * @return parallel acquisition technique.
	 */
	public static ParallelAcquisitionTechnique getIdByTechnique(final String technique) {
		if (technique == null) {
			return null;
		}
		return ParallelAcquisitionTechnique.valueOf(technique);
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
