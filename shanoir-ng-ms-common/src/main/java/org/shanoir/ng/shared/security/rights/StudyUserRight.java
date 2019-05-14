package org.shanoir.ng.shared.security.rights;

/**
 * Study rights for an user.
 * 
 * @author msimon, jlouis
 *
 */
public enum StudyUserRight {

	
	/**
	 * The member can edit the study's parameters, the study's members and their rights and protocol files for this study..
	 */
	CAN_ADMINISTRATE(1),
	
	/**
	 *  The member can import data in this study. Must come with CAN_SEE_ALL otherwise the user cannot see the data he has imported.
	 */
	CAN_IMPORT(2),
	
	/**
	 * The member can downlad data from this study.
	 */
	CAN_DOWNLOAD(3),
	
	/**
	 * The member can see all the study's data.
	 */
	CAN_SEE_ALL(4);
	

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private StudyUserRight(final int id) {
		this.id = id;
	}

	/**
	 * Get a study right for an user by its id.
	 * 
	 * @param id
	 *            right id.
	 * @return study right.
	 */
	public static StudyUserRight getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (StudyUserRight type : StudyUserRight.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching study right for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
