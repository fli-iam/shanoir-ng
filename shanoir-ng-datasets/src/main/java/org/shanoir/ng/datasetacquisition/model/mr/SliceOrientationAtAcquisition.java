package org.shanoir.ng.datasetacquisition.model.mr;

/**
 * Slice orientation at acquisition.
 * 
 * @author msimon
 *
 */
public enum SliceOrientationAtAcquisition {

	// Transverse
	TRANSVERSE(1),

	// Coronal
	CORONAL(2),

	// Sagittal
	SAGITTAL(3),

	// Oblique
	OBLIQUE(4);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private SliceOrientationAtAcquisition(final int id) {
		this.id = id;
	}

	/**
	 * Get a slice orientation at acquisition by its id.
	 * 
	 * @param id
	 *            orientation id.
	 * @return slice orientation at acquisition.
	 */
	public static SliceOrientationAtAcquisition getOrientation(final Integer id) {
		if (id == null) {
			return null;
		}
		for (SliceOrientationAtAcquisition orientation : SliceOrientationAtAcquisition.values()) {
			if (id.equals(orientation.getId())) {
				return orientation;
			}
		}
		throw new IllegalArgumentException("No matching slice orientation at acquisition for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
