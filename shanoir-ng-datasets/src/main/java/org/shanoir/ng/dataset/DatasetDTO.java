package org.shanoir.ng.dataset;

import java.util.Date;

/**
 * DTO for dataset.
 * 
 * @author msimon
 *
 */
public class DatasetDTO {

	private CardinalityOfRelatedSubjects cardinalityOfRelatedSubjects;

	private String comment;

	private Date creationDate;

	private DatasetModalityType datasetModalityType;

	private ExploredEntity exploredEntity;

	private Long groupOfSubjectsId;

	private Long id;

	private String name;

	private ProcessedDatasetType processedDatasetType;

	private Long studyId;

	private Long subjectId;

	private String type;

	/**
	 * @return the cardinalityOfRelatedSubjects
	 */
	public CardinalityOfRelatedSubjects getCardinalityOfRelatedSubjects() {
		return cardinalityOfRelatedSubjects;
	}

	/**
	 * @param cardinalityOfRelatedSubjects
	 *            the cardinalityOfRelatedSubjects to set
	 */
	public void setCardinalityOfRelatedSubjects(CardinalityOfRelatedSubjects cardinalityOfRelatedSubjects) {
		this.cardinalityOfRelatedSubjects = cardinalityOfRelatedSubjects;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate
	 *            the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the datasetModalityType
	 */
	public DatasetModalityType getDatasetModalityType() {
		return datasetModalityType;
	}

	/**
	 * @param datasetModalityType
	 *            the datasetModalityType to set
	 */
	public void setDatasetModalityType(DatasetModalityType datasetModalityType) {
		this.datasetModalityType = datasetModalityType;
	}

	/**
	 * @return the exploredEntity
	 */
	public ExploredEntity getExploredEntity() {
		return exploredEntity;
	}

	/**
	 * @param exploredEntity
	 *            the exploredEntity to set
	 */
	public void setExploredEntity(ExploredEntity exploredEntity) {
		this.exploredEntity = exploredEntity;
	}

	/**
	 * @return the groupOfSubjectsId
	 */
	public Long getGroupOfSubjectsId() {
		return groupOfSubjectsId;
	}

	/**
	 * @param groupOfSubjectsId
	 *            the groupOfSubjectsId to set
	 */
	public void setGroupOfSubjectsId(Long groupOfSubjectsId) {
		this.groupOfSubjectsId = groupOfSubjectsId;
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
	 * @return the processedDatasetType
	 */
	public ProcessedDatasetType getProcessedDatasetType() {
		return processedDatasetType;
	}

	/**
	 * @param processedDatasetType
	 *            the processedDatasetType to set
	 */
	public void setProcessedDatasetType(ProcessedDatasetType processedDatasetType) {
		this.processedDatasetType = processedDatasetType;
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
	 * @return the subjectId
	 */
	public Long getSubjectId() {
		return subjectId;
	}

	/**
	 * @param subjectId
	 *            the subjectId to set
	 */
	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

}
