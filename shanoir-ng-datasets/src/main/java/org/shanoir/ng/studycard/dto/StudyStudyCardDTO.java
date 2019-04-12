package org.shanoir.ng.studycard.dto;

/**
 * DTO for link between a study card and a study.
 * 
 * @author msimon
 *
 */
public class StudyStudyCardDTO {

	// Create new link to study
	private Long newStudyId;

	// Delete old link to study
	private Long oldStudyId;

	private Long studyCardId;

	/**
	 * Simple constructor.
	 */
	public StudyStudyCardDTO() {
	}

	/**
	 * Constructor.
	 * 
	 * @param studyCardId
	 *            study card id.
	 * @param newStudyId
	 *            study id for link creation.
	 * @param oldStudyId
	 *            study id for link deletion.
	 */
	public StudyStudyCardDTO(final Long studyCardId, final Long newStudyId, final Long oldStudyId) {
		this.studyCardId = studyCardId;
		this.newStudyId = newStudyId;
		this.oldStudyId = oldStudyId;
	}

	/**
	 * @return the newStudyId
	 */
	public Long getNewStudyId() {
		return newStudyId;
	}

	/**
	 * @param newStudyId
	 *            the newStudyId to set
	 */
	public void setNewStudyId(Long newStudyId) {
		this.newStudyId = newStudyId;
	}

	/**
	 * @return the oldStudyId
	 */
	public Long getOldStudyId() {
		return oldStudyId;
	}

	/**
	 * @param oldStudyId
	 *            the oldStudyId to set
	 */
	public void setOldStudyId(Long oldStudyId) {
		this.oldStudyId = oldStudyId;
	}

	/**
	 * @return the studyCardId
	 */
	public Long getStudyCardId() {
		return studyCardId;
	}

	/**
	 * @param studyCardId
	 *            the studyCardId to set
	 */
	public void setStudyCardId(Long studyCardId) {
		this.studyCardId = studyCardId;
	}

}
