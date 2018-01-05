package org.shanoir.ng.dataset;

/**
 * Cardinality of related subjects.
 * 
 * @author msimon
 *
 */
public enum CardinalityOfRelatedSubjects {

	// Single-Subject Dataset
	SINGLE_SUBJECT_DATASET(1),

	// Multiple-Subjects Dataset
	MULTIPLE_SUBJECTS_DATASET(2);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private CardinalityOfRelatedSubjects(final int id) {
		this.id = id;
	}

	/**
	 * Get a cardinality by its id.
	 * 
	 * @param id
	 *            cardinality id.
	 * @return cardinality of related subjects.
	 */
	public static CardinalityOfRelatedSubjects getCardinality(final Integer id) {
		if (id == null) {
			return null;
		}
		for (CardinalityOfRelatedSubjects cardinality : CardinalityOfRelatedSubjects.values()) {
			if (id.equals(cardinality.getId())) {
				return cardinality;
			}
		}
		throw new IllegalArgumentException("No matching cardinality of related subjects for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
