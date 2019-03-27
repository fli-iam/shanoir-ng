package org.shanoir.ng.coil.model;

/**
 * Coil type.
 * 
 * @author msimon
 *
 */
public enum CoilType {

	/**
	 * BODY.
	 */
	BODY(1),

	/**
	 * HEAD.
	 */
	HEAD(2),

	/**
	 * SURFACE.
	 */
	SURFACE(3),

	/**
	 * MULTICOIL.
	 */
	MULTICOIL(4),

	/**
	 * EXTREMITY
	 */
	EXTREMITY(5);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private CoilType(final int id) {
		this.id = id;
	}

	/**
	 * Get a coil type by its id.
	 * 
	 * @param id
	 *            type id.
	 * @return coil type.
	 */
	public static CoilType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (CoilType type : CoilType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching coil type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
