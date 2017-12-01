package org.shanoir.ng.study;

import java.sql.Date;
import java.util.List;

import org.shanoir.ng.center.CenterDTO;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.subjectstudy.SubjectStudyDTO;

/**
 * DTO for study.
 * 
 * @author msimon
 *
 */
public class StudyDTO {

	private List<CenterDTO> centers;

	private boolean clinical;

	private boolean downloadableByDefault;

	private Date endDate;

	private List<Long> examinationIds;

	private List<IdNameDTO> experimentalGroupsOfSubjects;

	private Long id;

	private List<MembersCategoryDTO> membersCategories;

	private boolean monoCenter;

	private String name;

	private int nbSujects;

	private List<String> protocolFilePaths;

	private Date startDate;

	private List<IdNameDTO> studyCards;

	private StudyStatus studyStatus;

	private StudyType studyType;

	private List<SubjectStudyDTO> subjects;

	private boolean visibleByDefault;

	private boolean withExamination;

	/**
	 * Default constructor.
	 */
	public StudyDTO() {
	}

	/**
	 * @return the centers
	 */
	public List<CenterDTO> getCenters() {
		return centers;
	}

	/**
	 * @param centers
	 *            the centers to set
	 */
	public void setCenters(List<CenterDTO> centers) {
		this.centers = centers;
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
	public List<IdNameDTO> getExperimentalGroupsOfSubjects() {
		return experimentalGroupsOfSubjects;
	}

	/**
	 * @param experimentalGroupsOfSubjects
	 *            the experimentalGroupsOfSubjects to set
	 */
	public void setExperimentalGroupsOfSubjects(List<IdNameDTO> experimentalGroupsOfSubjects) {
		this.experimentalGroupsOfSubjects = experimentalGroupsOfSubjects;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the membersCategories
	 */
	public List<MembersCategoryDTO> getMembersCategories() {
		return membersCategories;
	}

	/**
	 * @param membersCategories
	 *            the membersCategories to set
	 */
	public void setMembersCategories(List<MembersCategoryDTO> membersCategories) {
		this.membersCategories = membersCategories;
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
	 * @return the nbSujects
	 */
	public int getNbSujects() {
		return nbSujects;
	}

	/**
	 * @param nbSujects
	 *            the nbSujects to set
	 */
	public void setNbSujects(int nbSujects) {
		this.nbSujects = nbSujects;
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
	 * @return the studyCards
	 */
	public List<IdNameDTO> getStudyCards() {
		return studyCards;
	}

	/**
	 * @param studyCards
	 *            the studyCards to set
	 */
	public void setStudyCards(List<IdNameDTO> studyCards) {
		this.studyCards = studyCards;
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
	 * @return the subjects
	 */
	public List<SubjectStudyDTO> getSubjects() {
		return subjects;
	}

	/**
	 * @param subjects
	 *            the subjects to set
	 */
	public void setSubjects(List<SubjectStudyDTO> subjects) {
		this.subjects = subjects;
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
