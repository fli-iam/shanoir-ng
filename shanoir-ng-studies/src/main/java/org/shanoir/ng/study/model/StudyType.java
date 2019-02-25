package org.shanoir.ng.study.model;

/**
 * Study type.
 * 
 * @author msimon
 *
 */
public enum StudyType {

	/**
	 * Clinical.
	 */
	CLINICAL(1),

	/**
	 * Preclinical.
	 */
	PRECLINICAL(2),

	/**
	 * Methodological.
	 */
	METHODOLOGICAL(3);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private StudyType(final int id) {
		this.id = id;
	}

	/**
	 * Get a study type by its id.
	 * 
	 * @param id
	 *            type id.
	 * @return study type.
	 */
	public static StudyType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (StudyType type : StudyType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching study type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
