package org.shanoir.ng.study;

import java.sql.Date;
import java.util.List;

/**
 * DTO for study.
 * 
 * @author msimon
 *
 */
public class StudyDTO {

	private boolean clinical;

	private boolean downloadableByDefault;

	private Date endDate;

	private List<Long> examinationIds;

	// private List<ExperimentalGroupOfSubjects>
	// experimentalGroupOfSubjectsList;

	private Long id;

	private boolean monoCenter;

	private String name;

	private List<String> protocolFilePathList;

	private Date startDate;

	private List<Long> studyCardIds;

	private StudyStatus studyStatus;

	// private List<StudyCenter> studyCenterList;

	private StudyType studyType;

	private List<String> subjectNames;

	// private List<SubjectStudy> subjectStudyList;

	private boolean visibleByDefault;

	private boolean withExamination;

	/**
	 * Default constructor.
	 */
	public StudyDTO() {
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
	 * @return the protocolFilePathList
	 */
	public List<String> getProtocolFilePathList() {
		return protocolFilePathList;
	}

	/**
	 * @param protocolFilePathList
	 *            the protocolFilePathList to set
	 */
	public void setProtocolFilePathList(List<String> protocolFilePathList) {
		this.protocolFilePathList = protocolFilePathList;
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
	 * @return the subjectNames
	 */
	public List<String> getSubjectNames() {
		return subjectNames;
	}

	/**
	 * @param subjectNames
	 *            the subjectNames to set
	 */
	public void setSubjectNames(List<String> subjectNames) {
		this.subjectNames = subjectNames;
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
