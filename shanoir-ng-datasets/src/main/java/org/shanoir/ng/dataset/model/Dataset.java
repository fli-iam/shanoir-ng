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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.shanoir.ng.dataset.modality.CalibrationDataset;
import org.shanoir.ng.dataset.modality.CtDataset;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.MegDataset;
import org.shanoir.ng.dataset.modality.MeshDataset;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.ParameterQuantificationDataset;
import org.shanoir.ng.dataset.modality.PetDataset;
import org.shanoir.ng.dataset.modality.RegistrationDataset;
import org.shanoir.ng.dataset.modality.SegmentationDataset;
import org.shanoir.ng.dataset.modality.SpectDataset;
import org.shanoir.ng.dataset.modality.StatisticalDataset;
import org.shanoir.ng.dataset.modality.TemplateDataset;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * Dataset.
 * 
 * @author msimon
 * @author jlouis
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = CalibrationDataset.class, name = CalibrationDataset.datasetType),
		@JsonSubTypes.Type(value = CtDataset.class, name = CtDataset.datasetType),
		@JsonSubTypes.Type(value = EegDataset.class, name = EegDataset.datasetType),
		@JsonSubTypes.Type(value = MegDataset.class, name = MegDataset.datasetType),
		@JsonSubTypes.Type(value = MeshDataset.class, name = MeshDataset.datasetType),
		@JsonSubTypes.Type(value = MrDataset.class, name = MrDataset.datasetType),
		@JsonSubTypes.Type(value = ParameterQuantificationDataset.class, name = ParameterQuantificationDataset.datasetType),
		@JsonSubTypes.Type(value = PetDataset.class, name = PetDataset.datasetType),
		@JsonSubTypes.Type(value = RegistrationDataset.class, name = RegistrationDataset.datasetType),
		@JsonSubTypes.Type(value = SegmentationDataset.class, name = SegmentationDataset.datasetType),
		@JsonSubTypes.Type(value = SpectDataset.class, name = SpectDataset.datasetType),
		@JsonSubTypes.Type(value = StatisticalDataset.class, name = StatisticalDataset.datasetType),
		@JsonSubTypes.Type(value = TemplateDataset.class, name = TemplateDataset.datasetType) })
public abstract class Dataset extends AbstractEntity {
	
	/**
	 * UID
	 */
	private static final long serialVersionUID = -6712556010816448026L;

	/** Creation date of the dataset. */
	@LocalDateAnnotations
	private LocalDate creationDate;

	/** Dataset Acquisition. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dataset_acquisition_id")
	private DatasetAcquisition datasetAcquisition;

	/** Dataset expression list. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "dataset", cascade = CascadeType.ALL)
	private List<DatasetExpression> datasetExpressions;

	/** Dataset Processing. */
	@ManyToOne(cascade = CascadeType.REMOVE)
	@JoinColumn(name = "dataset_processing_id")
	private DatasetProcessing datasetProcessing;

	/**
	 * Group of subjects. Constraint: not null if dataset.subject == null and
	 * null if dataset.subject != null.
	 */
	private Long groupOfSubjectsId;

	
	/** Processings for which this dataset is an input. */
	@ManyToMany(mappedBy="inputDatasets")
	private List<DatasetProcessing> processings;

	/** Origin metadata. */
	@OneToOne(cascade = CascadeType.ALL)
	private DatasetMetadata originMetadata;

	/**
	 * Parent dataset with the same sampling grid, ie that can be superimposed
	 * with this dataset.
	 */
	@ManyToOne
	@JoinColumn(name = "referenced_dataset_for_superimposition_id")
	private Dataset referencedDatasetForSuperimposition;

	/**
	 * List of children datasets with the same sampling grid, ie that can be
	 * superimposed with this dataset.
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "referencedDatasetForSuperimposition", cascade = CascadeType.ALL)
	private List<Dataset> referencedDatasetForSuperimpositionChildrenList;

	/** The study for which this dataset has been imported. Don't use it, use getStudyId() instead. */
	private Long importedStudyId;
	
	/** Subject. */
	private Long studyId;

	/** Subject. */
	private Long subjectId;

	/** Metadata updated by study card. */
	@OneToOne(cascade = CascadeType.ALL)
	private DatasetMetadata updatedMetadata;

	/**
	 * @return the creationDate
	 */
	public LocalDate getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate
	 *            the creationDate to set
	 */
	public void setCreationDate(LocalDate creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the datasetAcquisition
	 */
	public DatasetAcquisition getDatasetAcquisition() {
		return datasetAcquisition;
	}

	/**
	 * @param datasetAcquisition
	 *            the datasetAcquisition to set
	 */
	public void setDatasetAcquisition(DatasetAcquisition datasetAcquisition) {
		this.datasetAcquisition = datasetAcquisition;
	}

	/**
	 * @return the datasetExpressions
	 */
	public List<DatasetExpression> getDatasetExpressions() {
		if (datasetExpressions == null) {
			datasetExpressions = new ArrayList<>();
		}
		return datasetExpressions;
	}

	/**
	 * @param datasetExpressions
	 *            the datasetExpressions to set
	 */
	public void setDatasetExpressions(List<DatasetExpression> datasetExpressions) {
		this.datasetExpressions = datasetExpressions;
	}

	/**
	 * @return the datasetProcessing
	 */
	public DatasetProcessing getDatasetProcessing() {
		return datasetProcessing;
	}

	/**
	 * @param datasetProcessing
	 *            the datasetProcessing to set
	 */
	public void setDatasetProcessing(DatasetProcessing datasetProcessing) {
		this.datasetProcessing = datasetProcessing;
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
	 * @return the processings
	 */
	public List<DatasetProcessing> getProcessings() {
		return processings;
	}

	/**
	 * @param processings the processings to set
	 */
	public void setProcessings(List<DatasetProcessing> processings) {
		this.processings = processings;
	}

	/**
	 * Get dataset name. If name is not present, returns
	 * "[id] [creation date] [type]".
	 * 
	 * @return the name
	 */
	public String getName() {
		if (updatedMetadata != null && !StringUtils.isEmpty(updatedMetadata.getName())) {
			return updatedMetadata.getName();
		} else if (!StringUtils.isEmpty(originMetadata.getName())) {
			return originMetadata.getName();
		} else {
			final StringBuilder result = new StringBuilder();
			result.append(this.getId());
			if (creationDate != null) {
				result.append(" ").append(creationDate.toString());
			}
			String modalityType = "?";
			if (updatedMetadata != null && updatedMetadata.getDatasetModalityType() != null) {
				modalityType = updatedMetadata.getDatasetModalityType().name();
			} else if (originMetadata != null && originMetadata.getDatasetModalityType() != null) {
				modalityType = originMetadata.getDatasetModalityType().name();
			}
			result.append(" ").append(modalityType.split("_")[0]);
			return result.toString();
		}
	}

	/**
	 * @return the originMetadata
	 */
	public DatasetMetadata getOriginMetadata() {
		if (originMetadata == null) originMetadata = new DatasetMetadata();
		return originMetadata;
	}

	/**
	 * @param originMetadata
	 *            the originMetadata to set
	 */
	public void setOriginMetadata(DatasetMetadata originMetadata) {
		this.originMetadata = originMetadata;
	}

	/**
	 * @return the referencedDatasetForSuperimposition
	 */
	public Dataset getReferencedDatasetForSuperimposition() {
		return referencedDatasetForSuperimposition;
	}

	/**
	 * @param referencedDatasetForSuperimposition
	 *            the referencedDatasetForSuperimposition to set
	 */
	public void setReferencedDatasetForSuperimposition(Dataset referencedDatasetForSuperimposition) {
		this.referencedDatasetForSuperimposition = referencedDatasetForSuperimposition;
	}

	/**
	 * @return the referencedDatasetForSuperimpositionChildrenList
	 */
	public List<Dataset> getReferencedDatasetForSuperimpositionChildrenList() {
		return referencedDatasetForSuperimpositionChildrenList;
	}

	/**
	 * @param referencedDatasetForSuperimpositionChildrenList
	 *            the referencedDatasetForSuperimpositionChildrenList to set
	 */
	public void setReferencedDatasetForSuperimpositionChildrenList(
			List<Dataset> referencedDatasetForSuperimpositionChildrenList) {
		this.referencedDatasetForSuperimpositionChildrenList = referencedDatasetForSuperimpositionChildrenList;
	}

	/**
	 * @return the studyId
	 */
	@Transient
	public Long getStudyId() {
		if (getDatasetAcquisition() == null || getDatasetAcquisition().getExamination() == null) return null;
		return getDatasetAcquisition().getExamination().getStudyId();
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

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	/**
	 * @return the updatedMetadata
	 */
	public DatasetMetadata getUpdatedMetadata() {
		if (updatedMetadata == null) updatedMetadata = new DatasetMetadata();
		return updatedMetadata;
	}

	/**
	 * @param updatedMetadata
	 *            the updatedMetadata to set
	 */
	public void setUpdatedMetadata(DatasetMetadata updatedMetadata) {
		this.updatedMetadata = updatedMetadata;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	@Transient
	public abstract String getType();

	/**
	 * You probably want to use getStudyId() instead.
	 * @return
	 */
	@Deprecated
	public Long getImportedStudyId() {
		return importedStudyId;
	}

	/**
	 * If you want to move the dataset to another study, change its examination.
	 * @param importedStudyId
	 */
	@Deprecated
	public void setImportedStudyId(Long importedStudyId) {
		this.importedStudyId = importedStudyId;
	}

}
