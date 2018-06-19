package org.shanoir.ng.datasetacquisition.mr;

/**
 * Acquisition contrast.
 * 
 * @author msimon
 *
 */
public enum AcquisitionContrast {

	// T1
	T1(1),

	// T2
	T2(2),

	// T2 star
	T2STAR(3),

	// Spin density
	SPIN_DENSITY(4),
	
	// Diffusion
	DIFFUSION(5),
	
	FLOW_ENCODED(6),
	
	FLUID_ATTENUATED(7),
	
	PERFUSION(8),
	
	PROTON_DENSITY(9),
	
	STIR(10),
	
	TAGGING(11),
	
	TOF(12),
	
	UNKNOWN(13),
	
	MIXED(14);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private AcquisitionContrast(final int id) {
		this.id = id;
	}

	/**
	 * Get an acquisition contrast by its id.
	 * 
	 * @param id
	 *            contrast id.
	 * @return acquisition contrast.
	 */
	public static AcquisitionContrast getContrast(final Integer id) {
		if (id == null) {
			return null;
		}
		for (AcquisitionContrast contrast : AcquisitionContrast.values()) {
			if (id.equals(contrast.getId())) {
				return contrast;
			}
		}
		throw new IllegalArgumentException("No matching acquisition contrast for id " + id);
	}
	
	/**
	 * Get an acquisition contrast by its name.
	 * 
	 * @param type
	 *            technique id.
	 * @return parallel acquisition technique.
	 */
	public static AcquisitionContrast getIdByType(final String type) {
		if (type == null) {
			return null;
		}
		return AcquisitionContrast.valueOf(type);
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
