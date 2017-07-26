package org.shanoir.ng.subject;

/**
 * Hemispheric dominance.
 * 
 * @author msimon
 *
 */
public enum HemisphericDominance {

	/**
	 * Left.
	 */
	Left(1),

	/**
	 * Right.
	 */
	Right(2);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private HemisphericDominance(final int id) {
		this.id = id;
	}

	/**
	 * Get a hemispheric dominance by its id.
	 * 
	 * @param id
	 *            dominance id.
	 * @return hemispheric dominance.
	 */
	public static HemisphericDominance getDominance(final Integer id) {
		if (id == null) {
			return null;
		}
		for (HemisphericDominance dominance : HemisphericDominance.values()) {
			if (id.equals(dominance.getId())) {
				return dominance;
			}
		}
		throw new IllegalArgumentException("No matching hemispheric dominance for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
