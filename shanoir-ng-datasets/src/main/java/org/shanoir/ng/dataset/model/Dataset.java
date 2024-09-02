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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import jakarta.persistence.*;
import org.apache.commons.lang3.StringUtils;
import org.shanoir.ng.dataset.modality.*;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;
import org.shanoir.ng.tag.model.StudyTag;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
		@JsonSubTypes.Type(value = CalibrationDataset.class, name = "Calibration"),
		@JsonSubTypes.Type(value = CtDataset.class, name = "Ct"),
		@JsonSubTypes.Type(value = EegDataset.class, name = "Eeg"),
		@JsonSubTypes.Type(value = MegDataset.class, name = "Meg"),
		@JsonSubTypes.Type(value = MeshDataset.class, name = "Mesh"),
		@JsonSubTypes.Type(value = MrDataset.class, name = "Mr"),
		@JsonSubTypes.Type(value = GenericDataset.class, name = "Generic"),
		@JsonSubTypes.Type(value = ParameterQuantificationDataset.class, name = "ParameterQuantification"),
		@JsonSubTypes.Type(value = PetDataset.class, name = "Pet"),
		@JsonSubTypes.Type(value = RegistrationDataset.class, name = "Registration"),
		@JsonSubTypes.Type(value = SegmentationDataset.class, name = "Segmentation"),
		@JsonSubTypes.Type(value = SpectDataset.class, name = "Spect"),
		@JsonSubTypes.Type(value = StatisticalDataset.class, name = "Statistical"),
		@JsonSubTypes.Type(value = TemplateDataset.class, name = "Template"),
		@JsonSubTypes.Type(value = BidsDataset.class, name = "BIDS"),
		@JsonSubTypes.Type(value = MeasurementDataset.class, name = "Measurement"),
		@JsonSubTypes.Type(value = XaDataset.class, name = "Xa") })
public abstract class Dataset extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -6712556010816448026L;

	/** Creation date of the dataset. */
	@LocalDateAnnotations
	private LocalDate creationDate;

	/** Dataset Acquisition. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "dataset_acquisition_id")
	private DatasetAcquisition datasetAcquisition;

	/** Dataset expression list. */
	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "dataset", cascade = CascadeType.ALL)
	private List<DatasetExpression> datasetExpressions;

	/** Dataset Processing. */
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "dataset_processing_id")
	private DatasetProcessing datasetProcessing;

	/**
	 * Group of subjects. Constraint: not null if dataset.subject == null and
	 * null if dataset.subject != null.
	 */
	private Long groupOfSubjectsId;

	/** Processings for which this dataset is an input. */
	@JsonIgnore
	@ManyToMany(mappedBy="inputDatasets")
	private List<DatasetProcessing> processings;

	/** Origin metadata. */
	@OneToOne(cascade = CascadeType.ALL)
	private DatasetMetadata originMetadata;

	/**
	 * Parent dataset with the same sampling grid, ie that can be superimposed
	 * with this dataset.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
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
	
	/** Study. */
	private Long studyId;

	/** Subject. */
	private Long subjectId;

	/** Can we download the subject */
	private boolean downloadable = true;

	/** Metadata updated by study card. */
	@OneToOne(cascade = CascadeType.ALL)
	private DatasetMetadata updatedMetadata;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "DATASET_TAG", joinColumns = @JoinColumn(name = "DATASET_ID"), inverseJoinColumns = @JoinColumn(name = "STUDY_TAG_ID"))
	private List<StudyTag> tags;

	private Long sourceId;

	@JsonIgnore
	@Transient
	public String SOPInstanceUID;

	public Dataset() {
	}

	public Dataset(Dataset d) {
		this.creationDate = d.getCreationDate();
		this.datasetAcquisition = d.getDatasetAcquisition();
		this.datasetExpressions = new ArrayList<>(d.getDatasetExpressions().size());
		for (DatasetExpression ds : d.getDatasetExpressions()) {
			this.datasetExpressions.add(new DatasetExpression(ds, d));
		}

		this.datasetProcessing = d.getDatasetProcessing();
		this.groupOfSubjectsId = d.getGroupOfSubjectsId();

		this.processings = new ArrayList<>(d.getProcessings().size());
		for (DatasetProcessing dproc : d.getProcessings()) {
			this.processings.add(new DatasetProcessing(dproc));
		}

		this.originMetadata = new DatasetMetadata(d.getOriginMetadata());
		this.referencedDatasetForSuperimposition = d.getReferencedDatasetForSuperimposition();

		this.referencedDatasetForSuperimpositionChildrenList = new ArrayList<>(d.getReferencedDatasetForSuperimpositionChildrenList().size());
		for (Dataset ds : d.getReferencedDatasetForSuperimpositionChildrenList()) {
			this.referencedDatasetForSuperimpositionChildrenList.add(ds);
		}

		this.importedStudyId = d.getImportedStudyId();
		this.subjectId = d.getSubjectId();
		this.downloadable = d.downloadable;
		this.updatedMetadata = new DatasetMetadata(d.getUpdatedMetadata());
		this.sourceId = d.getSourceId();
	}

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
		if (originMetadata == null) {
			originMetadata = new DatasetMetadata();
		}
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
		if (getDatasetAcquisition() == null || getDatasetAcquisition().getExamination() == null) {
			return studyId;
		}
		return getDatasetAcquisition().getExamination().getStudyId();
	}
	
	/**
	 * @return the studyId
	 */
	@Transient
	public Long getCenterId() {
		if (getDatasetAcquisition() == null || getDatasetAcquisition().getExamination() == null) {
			return studyId;
		}
		return getDatasetAcquisition().getExamination().getCenterId();
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
		if (updatedMetadata == null) {
			updatedMetadata = new DatasetMetadata();
		}
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

	public boolean isDownloadable() {
		return downloadable;
	}

	public void setDownloadable(boolean downloadable) {
		this.downloadable = downloadable;
	}

	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
	}

	public String getSOPInstanceUID() {
		return SOPInstanceUID;
	}

	public void setSOPInstanceUID(String sOPInstanceUID) {
		SOPInstanceUID = sOPInstanceUID;
	}

	public boolean getInPacs() {
		return getDatasetExpressions() != null && getDatasetExpressions().size() > 0;
	}

	public List<StudyTag> getTags() {
		return tags;
	}

	public void setTags(List<StudyTag> tags) {
		this.tags = tags;
	}
}
