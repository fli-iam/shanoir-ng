package org.shanoir.ng.study;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/**
 * Relation between the study and the users.
 *
 * @author ifakhfak
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "studyId", "userId" }, name = "study_user_idx") })
public class StudyUser implements Serializable {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -6816811624812002519L;

	/** is the anonymization report to be sent to the user. */
	private boolean receiveAnonymizationReport;

	/** Advice the user when new import done in the study. */
	private boolean receiveNewImportReport;

	/** Study id. */
	@Id
	private Long studyId;

	/** Type of the relationship. */
	@NotNull
	private Integer studyUserType;

	/** User id. */
	@Id
	private Long userId;

	/**
	 * @return the receiveAnonymizationReport
	 */
	public boolean isReceiveAnonymizationReport() {
		return receiveAnonymizationReport;
	}

	/**
	 * @param receiveAnonymizationReport
	 *            the receiveAnonymizationReport to set
	 */
	public void setReceiveAnonymizationReport(boolean receiveAnonymizationReport) {
		this.receiveAnonymizationReport = receiveAnonymizationReport;
	}

	/**
	 * @return the receiveNewImportReport
	 */
	public boolean isReceiveNewImportReport() {
		return receiveNewImportReport;
	}

	/**
	 * @param receiveNewImportReport
	 *            the receiveNewImportReport to set
	 */
	public void setReceiveNewImportReport(boolean receiveNewImportReport) {
		this.receiveNewImportReport = receiveNewImportReport;
	}

	/**
	 * @return the studyId
	 */
	public Long getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId
	 *            the studyId to set
	 */
	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	/**
	 * @return the studyUserType
	 */
	public StudyUserType getStudyUserType() {
		return StudyUserType.getType(studyUserType);
	}

	/**
	 * @param studyUserType
	 *            the studyUserType to set
	 */
	public void setStudyUserType(StudyUserType studyUserType) {
		if (studyUserType == null) {
			this.studyUserType = null;
		} else {
			this.studyUserType = studyUserType.getId();
		}
	}

	/**
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

}
