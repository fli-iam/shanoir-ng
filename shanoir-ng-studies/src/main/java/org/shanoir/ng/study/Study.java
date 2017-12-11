package org.shanoir.ng.study;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.SqlResultSetMapping;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.shanoir.ng.groupofsubjects.ExperimentalGroupOfSubjects;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.EditableOnlyBy;
import org.shanoir.ng.shared.validation.Unique;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.subjectstudy.SubjectStudy;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Study.
 * 
 * @author msimon
 *
 */
@Entity
@JsonPropertyOrder({ "_links", "id", "name" })
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
@SqlResultSetMapping(name = "studyNameResult", classes = { @ConstructorResult(targetClass = IdNameDTO.class, columns = {
		@ColumnResult(name = "id", type = Long.class), @ColumnResult(name = "name") }) })
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

	/** List of the examinations related to this study. */
	@ElementCollection
	@CollectionTable(name = "study_examination")
	@Column(name = "examination_id")
	private List<Long> examinationIds;

	/** Associated experimental groups of subjects. */
	@OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade = { CascadeType.ALL }, orphanRemoval = true)
	private List<ExperimentalGroupOfSubjects> experimentalGroupsOfSubjects;

	/** The is mono center. */
	@NotNull
	private boolean monoCenter;

	@NotBlank
	@Column(unique = true)
	@Unique
	@EditableOnlyBy(roles = { "ROLE_ADMIN", "ROLE_EXPERT" })
	private String name;

	/** List of protocol files directly attached to the study. */
	@ElementCollection
	@CollectionTable(name = "protocole_file_path")
	@Column(name = "path")
	private List<String> protocolFilePaths;

	/** Start date. */
	private Date startDate;

	/** Associated study card lists. */
	@ElementCollection
	@CollectionTable(name = "study_study_card")
	@Column(name = "study_card_id")
	private List<Long> studyCardIds;

	@NotNull
	private Integer studyStatus;

	/** Relations between the investigators, the centers and the studies. */
	@NotEmpty
	@OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<StudyCenter> studyCenterList;

	private Integer studyType;

	/** Users associated to the research study. */
	@OneToMany(mappedBy = "studyId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<StudyUser> studyUserList;

	/** Relations between the subjects and the studies. */
	@OneToMany(mappedBy = "study", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<SubjectStudy> subjectStudyList;

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
	 * @return the examinationIds
	 */
	public List<Long> getExaminationIds() {
		return examinationIds;
	}

	/**
	 * @param examinationIds
	 *            the examinationIds to set
	 */
	public void setExaminationIds(List<Long> examinationIds) {
		this.examinationIds = examinationIds;
	}

	/**
	 * @return the experimentalGroupsOfSubjects
	 */
	public List<ExperimentalGroupOfSubjects> getExperimentalGroupsOfSubjects() {
		return experimentalGroupsOfSubjects;
	}

	/**
	 * @param experimentalGroupsOfSubjects
	 *            the experimentalGroupsOfSubjects to set
	 */
	public void setExperimentalGroupsOfSubjects(List<ExperimentalGroupOfSubjects> experimentalGroupsOfSubjects) {
		this.experimentalGroupsOfSubjects = experimentalGroupsOfSubjects;
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
	 * @return the protocolFilePaths
	 */
	public List<String> getProtocolFilePaths() {
		return protocolFilePaths;
	}

	/**
	 * @param protocolFilePaths
	 *            the protocolFilePaths to set
	 */
	public void setProtocolFilePaths(List<String> protocolFilePaths) {
		this.protocolFilePaths = protocolFilePaths;
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
		return StudyStatus.getStatus(studyStatus);
	}

	/**
	 * @param studyStatus
	 *            the studyStatus to set
	 */
	public void setStudyStatus(StudyStatus studyStatus) {
		if (studyStatus == null) {
			this.studyStatus = null;
		} else {
			this.studyStatus = studyStatus.getId();
		}
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
	 * @return the studyCenterList
	 */
	public List<StudyCenter> getStudyCenterList() {
		return studyCenterList;
	}

	/**
	 * @param studyCenterList
	 *            the studyCenterList to set
	 */
	public void setStudyCenterList(List<StudyCenter> studyCenterList) {
		this.studyCenterList = studyCenterList;
	}

	/**
	 * @return the studyType
	 */
	public StudyType getStudyType() {
		return StudyType.getType(studyType);
	}

	/**
	 * @param studyType
	 *            the studyType to set
	 */
	public void setStudyType(StudyType studyType) {
		if (studyType == null) {
			this.studyType = null;
		} else {
			this.studyType = studyType.getId();
		}
	}

	/**
	 * @return the studyUserList
	 */
	public List<StudyUser> getStudyUserList() {
		return studyUserList;
	}

	/**
	 * @param studyUserList
	 *            the studyUserList to set
	 */
	public void setStudyUserList(List<StudyUser> studyUserList) {
		this.studyUserList = studyUserList;
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
