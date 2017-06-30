package org.shanoir.ng.subject;

/**
 * Subject type.
 * 
 * @author msimon
 *
 */
public enum SubjectType {

	/**
	 * Healthy volunteer.
	 */
	HEALTHY_VOLUNTEER(1),

	/**
	 * Patient.
	 */
	PATIENT(2),

	/**
	 * Phantom.
	 */
	PHANTOM(3);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private SubjectType(final int id) {
		this.id = id;
	}

	/**
	 * Get a subject type by its id.
	 * 
	 * @param id
	 *            type id.
	 * @return subject type.
	 */
	public static SubjectType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (SubjectType type : SubjectType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching subject type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
