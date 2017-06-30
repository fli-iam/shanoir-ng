package org.shanoir.ng.subject;

/**
 * Imaged object category.
 * 
 * @author msimon
 *
 */
public enum ImagedObjectCategory {

	/**
	 * Phantom
	 */
	PHANTOM(1),

	/**
	 * Living human being
	 */
	LIVING_HUMAN_BEING(2),

	/**
	 * Human cadaver
	 */
	HUMAN_CADAVER(3),

	/**
	 * Anatomical piece
	 */
	ANATOMICAL_PIECE(5);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private ImagedObjectCategory(final int id) {
		this.id = id;
	}

	/**
	 * Get an imaged object category by its id.
	 * 
	 * @param id
	 *            category id.
	 * @return imaged object category.
	 */
	public static ImagedObjectCategory getCategory(final Integer id) {
		if (id == null) {
			return null;
		}
		for (ImagedObjectCategory category : ImagedObjectCategory.values()) {
			if (id.equals(category.getId())) {
				return category;
			}
		}
		throw new IllegalArgumentException("No matching imaged object category for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
