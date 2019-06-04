package org.shanoir.ng.study.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.rights.StudyUserInterface;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@Table(name = "study_user", uniqueConstraints = { @UniqueConstraint(columnNames = { "study_id", "userId" }, name = "study_user_idx") })
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class StudyUser extends AbstractEntity implements StudyUserInterface {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 5813071870148636187L;

	/** is the anonymization report to be sent to the user. */
	private boolean receiveAnonymizationReport;

	/** Advice the user when new import done in the study. */
	private boolean receiveNewImportReport;

	/** Study id. */
	@ManyToOne
	@JsonIgnore
	private Study study;
		
	/** User id. */
	private Long userId;

	/** Type of the relationship. */
	@ElementCollection
	@CollectionTable(name="study_user_study_user_rights", joinColumns=@JoinColumn(name="study_user_id"))
	private List<Integer> studyUserRights;
	
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
	@JsonInclude
	@Transient
	public Long getStudyId() {
		return study.getId();
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	/**
	 * @return the studyUserRight
	 */
	public List<StudyUserRight> getStudyUserRights() {
		List<StudyUserRight> list = new ArrayList<>();
		for (Integer id : studyUserRights) list.add(StudyUserRight.getType(id));
		return list;
	}

	/**
	 * @param studyUserRight the studyUserRight to set
	 */
	public void setStudyUserRights(List<StudyUserRight> studyUserRights) {
		this.studyUserRights = new ArrayList<>();
		if (studyUserRights != null) {
			for (StudyUserRight sur : studyUserRights)  {
				this.studyUserRights.add(sur.getId());
			}
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
