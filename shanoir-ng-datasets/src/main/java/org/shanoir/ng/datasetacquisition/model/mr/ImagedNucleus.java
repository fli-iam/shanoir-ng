package org.shanoir.ng.datasetacquisition.model.mr;

/**
 * Imaged nucleus.
 * 
 * @author msimon
 *
 */
public enum ImagedNucleus {

	// 1H
	H1(1),

	// 31P
	P31(2);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private ImagedNucleus(final int id) {
		this.id = id;
	}

	/**
	 * Get an imaged nucleus by its id.
	 * 
	 * @param id
	 *            imaged nucleus id.
	 * @return imaged nucleus.
	 */
	public static ImagedNucleus getNucleus(final Integer id) {
		if (id == null) {
			return null;
		}
		for (ImagedNucleus nucleus : ImagedNucleus.values()) {
			if (id.equals(nucleus.getId())) {
				return nucleus;
			}
		}
		throw new IllegalArgumentException("No matching imaged nucleus for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
}
