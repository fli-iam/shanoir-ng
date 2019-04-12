package org.shanoir.ng.dataset.dto;

import org.shanoir.ng.dataset.model.CardinalityOfRelatedSubjects;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.dataset.model.ExploredEntity;
import org.shanoir.ng.dataset.model.ProcessedDatasetType;

/**
 * DTO for dataset metadata that could be updated by study card.
 * 
 * @author msimon
 *
 */
public class DatasetMetadataDTO {

	private CardinalityOfRelatedSubjects cardinalityOfRelatedSubjects;

	private String comment;

	private DatasetModalityType datasetModalityType;

	private ExploredEntity exploredEntity;

	private Long id;

	private String name;

	private ProcessedDatasetType processedDatasetType;

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

}
