package org.shanoir.ng.study;

import java.sql.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.EditableOnlyBy;
import org.shanoir.ng.shared.validation.Unique;
import org.shanoir.ng.subject.SubjectStudy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Entity
@Table(name = "study")
@JsonPropertyOrder({ "_links", "id", "name" })
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class Study extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 2182323766659913794L;

	/** Is clinical. */
	@NotNull
	private boolean clinical;

	/** Coordinator. */
	// TODO: replace by investigator
	private Long coordinatorId;

	/** Is with downloadable by default. */
	private boolean downloadableByDefault;

	/** End date. */
	private Date endDate;

	/** The is mono center. */
	@NotNull
	private boolean monoCenter;

	@NotBlank
	@Column(unique = true)
	@Unique
	@EditableOnlyBy(roles = { "ROLE_ADMIN", "ROLE_EXPERT" })
	private String name;

	/** Start date. */
	private Date startDate;

	/** Associated study card lists. */
	@ElementCollection
	@CollectionTable(name = "study_study_card")
	@Column(name = "study_card_id")
	private List<Long> studyCardIds;

	@NotNull
	@Enumerated(EnumType.STRING)
	private StudyStatus studyStatus;

	/** Relations between the subjects and the studies. */
	@JsonIgnore
	@OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<SubjectStudy> subjectStudyList;

	/** Users associated to the research study. */
	
	@OneToMany(mappedBy = "studyId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<StudyUser> studyUsers;

	@NotNull
	@Enumerated(EnumType.STRING)
	private StudyType studyType;

	/** Is visible by default. */
	private boolean visibleByDefault;

	/** Is with examination. */
	private boolean withExamination;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "study/" + getId());
	}

	/**
	 * @return the clinical
	 */
	public boolean isClinical() {
		return clinical;
	}

	/**
	 * @param clinical
	 *            the clinical to set
	 */
	public void setClinical(boolean clinical) {
		this.clinical = clinical;
	}

	/**
	 * @return the coordinatorId
	 */
	public Long getCoordinatorId() {
		return coordinatorId;
	}

	/**
	 * @param coordinatorId
	 *            the coordinatorId to set
	 */
	public void setCoordinatorId(Long coordinatorId) {
		this.coordinatorId = coordinatorId;
	}

	/**
	 * @return the downloadableByDefault
	 */
	public boolean isDownloadableByDefault() {
		return downloadableByDefault;
	}

	/**
	 * @param downloadableByDefault
	 *            the downloadableByDefault to set
	 */
	public void setDownloadableByDefault(boolean downloadableByDefault) {
		this.downloadableByDefault = downloadableByDefault;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the monoCenter
	 */
	public boolean isMonoCenter() {
		return monoCenter;
	}

	/**
	 * @param monoCenter
	 *            the monoCenter to set
	 */
	public void setMonoCenter(boolean monoCenter) {
		this.monoCenter = monoCenter;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the studyCardIds
	 */
	public List<Long> getStudyCardIds() {
		return studyCardIds;
	}

	/**
	 * @param studyCardIds
	 *            the studyCardIds to set
	 */
	public void setStudyCardIds(List<Long> studyCardIds) {
		this.studyCardIds = studyCardIds;
	}

	/**
	 * @return the studyStatus
	 */
	public StudyStatus getStudyStatus() {
		return studyStatus;
	}

	/**
	 * @param studyStatus
	 *            the studyStatus to set
	 */
	public void setStudyStatus(StudyStatus studyStatus) {
		this.studyStatus = studyStatus;
	}

	/**
	 * @return the subjectStudyList
	 */
	public List<SubjectStudy> getSubjectStudyList() {
		return subjectStudyList;
	}

	/**
	 * @param subjectStudyList
	 *            the subjectStudyList to set
	 */
	public void setSubjectStudyList(List<SubjectStudy> subjectStudyList) {
		this.subjectStudyList = subjectStudyList;
	}

	/**
	 * @return the studyUsers
	 */
	public List<StudyUser> getStudyUsers() {
		return studyUsers;
	}

	/**
	 * @param studyUsers
	 *            the studyUsers to set
	 */
	public void setStudyUsers(List<StudyUser> studyUsers) {
		this.studyUsers = studyUsers;
	}

	/**
	 * @return the studyType
	 */
	public StudyType getStudyType() {
		return studyType;
	}

	/**
	 * @param studyType
	 *            the studyType to set
	 */
	public void setStudyType(StudyType studyType) {
		this.studyType = studyType;
	}

	/**
	 * @return the visibleByDefault
	 */
	public boolean isVisibleByDefault() {
		return visibleByDefault;
	}

	/**
	 * @param visibleByDefault
	 *            the visibleByDefault to set
	 */
	public void setVisibleByDefault(boolean visibleByDefault) {
		this.visibleByDefault = visibleByDefault;
	}

	/**
	 * @return the withExamination
	 */
	public boolean isWithExamination() {
		return withExamination;
	}

	/**
	 * @param withExamination
	 *            the withExamination to set
	 */
	public void setWithExamination(boolean withExamination) {
		this.withExamination = withExamination;
	}

}
