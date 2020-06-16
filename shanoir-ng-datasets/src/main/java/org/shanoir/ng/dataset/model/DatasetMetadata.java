/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.dataset.model;

import javax.persistence.Entity;

import org.shanoir.ng.shared.core.model.AbstractEntity;

/**
 * Dataset metadata that could be updated by study card.
 * 
 * @author msimon
 *
 */
@Entity
public class DatasetMetadata extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -8189893217666417587L;

	/**
	 * Flag to indicate whether this dataset is related to a single subject or
	 * to multiple subjects.
	 */
	private Integer cardinalityOfRelatedSubjects;

	/**
	 * A comment on the dataset. In case of importing from dicom files, it could
	 * be the series description for instance.
	 */
	private String comment;

	/** Dataset Modality Type. */
	private Integer datasetModalityType;

	/** Explored entity. */
	private Integer exploredEntity;

	/**
	 * The name of this dataset. For instance, it could be 'BrainWeb',
	 * 'ICBM152', 'T1-weighted High resolution image without injection' etc.
	 */
	private String name;

	/** Processed dataset type. */
	private Integer processedDatasetType;

	/**
	 * @return the cardinalityOfRelatedSubjects
	 */
	public CardinalityOfRelatedSubjects getCardinalityOfRelatedSubjects() {
		return CardinalityOfRelatedSubjects.getCardinality(cardinalityOfRelatedSubjects);
	}

	/**
	 * @param cardinalityOfRelatedSubjects
	 *            the cardinalityOfRelatedSubjects to set
	 */
	public void setCardinalityOfRelatedSubjects(CardinalityOfRelatedSubjects cardinalityOfRelatedSubjects) {
		if (cardinalityOfRelatedSubjects != null) {
			this.cardinalityOfRelatedSubjects = cardinalityOfRelatedSubjects.getId();
		}
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
		return DatasetModalityType.getType(datasetModalityType);
	}

	/**
	 * @param datasetModalityType
	 *            the datasetModalityType to set
	 */
	public void setDatasetModalityType(DatasetModalityType datasetModalityType) {
		if (datasetModalityType == null) {
			this.datasetModalityType = null;
		} else {
			this.datasetModalityType = datasetModalityType.getId();
		}
	}

	/**
	 * @return the exploredEntity
	 */
	public ExploredEntity getExploredEntity() {
		return ExploredEntity.getEntity(exploredEntity);
	}

	/**
	 * @param exploredEntity
	 *            the exploredEntity to set
	 */
	public void setExploredEntity(ExploredEntity exploredEntity) {
		if (exploredEntity == null) {
			this.exploredEntity = null;
		} else {
			this.exploredEntity = exploredEntity.getId();
		}
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
		return ProcessedDatasetType.getType(processedDatasetType);
	}

	/**
	 * @param processedDatasetType
	 *            the processedDatasetType to set
	 */
	public void setProcessedDatasetType(ProcessedDatasetType processedDatasetType) {
		if (processedDatasetType == null) {
			this.processedDatasetType = null;
		} else {
			this.processedDatasetType = processedDatasetType.getId();
		}
	}

}
