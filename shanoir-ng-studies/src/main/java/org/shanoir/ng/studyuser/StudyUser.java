package org.shanoir.ng.studyuser;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * Relation between the study and the users.
 * 
 * For performance reasons in microservices architectures and as the user name
 * is an information entity that changes nearly never, we duplicate the info here:
 * we have a user name as well in study_user. The master record is managed in the
 * ms users. If the user name is changed there for an user id, the ms studies will
 * pull for events of this change and apply the change on this object to remain
 * synchronized: usage of asynchronous REST here to increase the independence and
 * resilience between the microservices.
 *
 * @author ifakhfak
 * @author mkain
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "studyId", "userId" }, name = "study_user_idx") })
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class StudyUser extends AbstractGenericItem{

	/**
	 * UID
	 */
	private static final long serialVersionUID = 5813071870148636187L;

	/** is the anonymization report to be sent to the user. */
	private boolean receiveAnonymizationReport;

	/** Advice the user when new import done in the study. */
	private boolean receiveNewImportReport;

	/** Study id. */
	private Long studyId;

	/** Type of the relationship. */
	@NotNull
	private Integer studyUserType;

	/** User id. */
	private Long userId;
	
	/** User name. Duplicate: master record in ms users. */
	@NotBlank
	private String userName;

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
	public StudyUserRight getStudyUserType() {
		return StudyUserRight.getType(studyUserType);
	}

	/**
	 * @param studyUserType
	 *            the studyUserType to set
	 */
	public void setStudyUserType(StudyUserRight studyUserType) {
		if (studyUserType != null) {
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
