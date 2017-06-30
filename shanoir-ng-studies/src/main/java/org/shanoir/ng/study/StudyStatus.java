package org.shanoir.ng.study;

/**
 * Study status.
 * 
 * @author msimon
 *
 */
public enum StudyStatus {

	/**
	 * in_progress.
	 */
	IN_PROGRESS(1),

	/**
	 * finished.
	 */
	FINISHED(2);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private StudyStatus(final int id) {
		this.id = id;
	}

	/**
	 * Get a study status by its id.
	 * 
	 * @param id
	 *            status id.
	 * @return study status.
	 */
	public static StudyStatus getStatus(final Integer id) {
		if (id == null) {
			return null;
		}
		for (StudyStatus status : StudyStatus.values()) {
			if (id.equals(status.getId())) {
				return status;
			}
		}
		throw new IllegalArgumentException("No matching study status for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
