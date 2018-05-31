package org.shanoir.ng.studyuser;

/**
 * Study rights for an user.
 * 
 * @author msimon
 *
 */
public enum StudyUserType {

	/**
	 * Is responsible for the research study.
	 */
	RESPONSIBLE(1),

	/**
	 * Can see, download, import datasets and modify the study parameters.
	 */
	SEE_DOWNLOAD_IMPORT_MODIFY(2),

	/**
	 * Can see, download and import datasets.
	 */
	SEE_DOWNLOAD_IMPORT(3),

	/**
	 * Cannot see or download datasets.
	 */
	NOT_SEE_DOWNLOAD(4),

	/**
	 * Can see and download datasets.
	 */
	SEE_DOWNLOAD(5);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private StudyUserType(final int id) {
		this.id = id;
	}

	/**
	 * Get a study right for an user by its id.
	 * 
	 * @param id
	 *            right id.
	 * @return study right.
	 */
	public static StudyUserType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (StudyUserType type : StudyUserType.values()) {
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
