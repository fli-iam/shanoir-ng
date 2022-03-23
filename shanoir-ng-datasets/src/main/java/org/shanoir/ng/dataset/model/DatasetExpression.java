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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.processing.model.DatasetProcessingType;
import org.shanoir.ng.shared.core.model.AbstractEntity;

/**
 * Dataset expression.
 * 
 * @author msimon
 */
@Entity
public class DatasetExpression extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -3269594490145201917L;

	/** List of dataset expressions coming from this dataset expression. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "originalDatasetExpression", cascade = { CascadeType.MERGE,
			CascadeType.REFRESH, CascadeType.PERSIST })
	private List<DatasetExpression> comingFromDatasetExpressions;

	/** Creation date of the dataset expression. */
	private LocalDateTime creationDate;

	/** Expressed dataset. */
	@ManyToOne
	@JoinColumn(name = "dataset_id")
	private Dataset dataset;

	/** Dataset expression format. */
	private Integer datasetExpressionFormat;

	/** Set of files. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "datasetExpression", cascade = { CascadeType.MERGE,
			CascadeType.PERSIST, CascadeType.REMOVE })
	private List<DatasetFile> datasetFiles;

	/** Dataset processing type. */
	private Integer datasetProcessingType;

	/** Frame count. Only filled if multiframe. */
	private Integer frameCount;

	/** Whether the pacs file is multiframe. */
	@NotNull
	private boolean multiFrame;

	/**
	 * Original dataset expression from which this dataset expression comes
	 * from.
	 */
	@ManyToOne
	@JoinColumn(name = "original_dataset_expression_id")
	private DatasetExpression originalDatasetExpression;

	/** Nifti Converter. */
	private Long niftiConverterId;
 
	/** Store temporarily the first image acquisition time until all images are processed*/
	@Transient
	private  LocalDateTime firstImageAcquisitionTime;

	/** Store temporarily the last image acquisition time until all images are processed */
	@Transient
	private LocalDateTime lastImageAcquisitionTime;
	
	/**
	 * The converter version used for NIFTI conversion Must keep here so even if
	 * the converter is modified We keep the real version
	 */
	
	private String niftiConverterVersion;

	/**
	 * Indicates if the dataset corresponds to the original Nifti conversion The
	 * conversion made during the import process
	 */
	private Boolean originalNiftiConversion;

	/**
	 * @return the comingFromDatasetExpressions
	 */
	public List<DatasetExpression> getComingFromDatasetExpressions() {
		return comingFromDatasetExpressions;
	}

	/**
	 * @param comingFromDatasetExpressions
	 *            the comingFromDatasetExpressions to set
	 */
	public void setComingFromDatasetExpressions(List<DatasetExpression> comingFromDatasetExpressions) {
		this.comingFromDatasetExpressions = comingFromDatasetExpressions;
	}

	/**
	 * @return the creationDate
	 */
	public LocalDateTime getExpressionCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate
	 *            the creationDate to set
	 */
	public void setCreationDate(LocalDateTime creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the dataset
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * @param dataset
	 *            the dataset to set
	 */
	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	/**
	 * @return the datasetExpressionFormat
	 */
	public DatasetExpressionFormat getDatasetExpressionFormat() {
		return DatasetExpressionFormat.getFormat(datasetExpressionFormat);
	}

	/**
	 * @param datasetExpressionFormat
	 *            the datasetExpressionFormat to set
	 */
	public void setDatasetExpressionFormat(DatasetExpressionFormat datasetExpressionFormat) {
		if (datasetExpressionFormat == null) {
			this.datasetExpressionFormat = null;
		} else {
			this.datasetExpressionFormat = datasetExpressionFormat.getId();
		}
	}

	/**
	 * @return the datasetFiles
	 */
	public List<DatasetFile> getDatasetFiles() {
        if (datasetFiles == null) {
    		datasetFiles = new ArrayList<>();
        }
		return datasetFiles;
	}

	/**
	 * @param datasetFiles
	 *            the datasetFiles to set
	 */
	public void setDatasetFiles(List<DatasetFile> datasetFiles) {
		this.datasetFiles = datasetFiles;
	}

	/**
	 * @return the datasetProcessingType
	 */
	public DatasetProcessingType getDatasetProcessingType() {
		return DatasetProcessingType.getType(datasetProcessingType);
	}

	/**
	 * @param datasetProcessingType
	 *            the datasetProcessingType to set
	 */
	public void setDatasetProcessingType(DatasetProcessingType datasetProcessingType) {
		if (datasetProcessingType == null) {
			this.datasetProcessingType = null;
		} else {
			this.datasetProcessingType = datasetProcessingType.getId();
		}
	}

	/**
	 * @return the frameCount
	 */
	public Integer getFrameCount() {
		return frameCount;
	}

	/**
	 * @param frameCount
	 *            the frameCount to set
	 */
	public void setFrameCount(Integer frameCount) {
		this.frameCount = frameCount;
	}

	/**
	 * @return the multiFrame
	 */
	public boolean isMultiFrame() {
		return multiFrame;
	}

	/**
	 * @param multiFrame
	 *            the multiFrame to set
	 */
	public void setMultiFrame(boolean multiFrame) {
		this.multiFrame = multiFrame;
	}

	/**
	 * @return the originalDatasetExpression
	 */
	public DatasetExpression getOriginalDatasetExpression() {
		return originalDatasetExpression;
	}

	/**
	 * @param originalDatasetExpression
	 *            the originalDatasetExpression to set
	 */
	public void setOriginalDatasetExpression(DatasetExpression originalDatasetExpression) {
		this.originalDatasetExpression = originalDatasetExpression;
	}

	/**
	 * @return the niftiConverterId
	 */
	public Long getNiftiConverterId() {
		return niftiConverterId;
	}

	/**
	 * @param niftiConverterId
	 *            the niftiConverterId to set
	 */
	public void setNiftiConverterId(Long niftiConverterId) {
		this.niftiConverterId = niftiConverterId;
	}

	/**
	 * @return the niftiConverterVersion
	 */
	public String getNiftiConverterVersion() {
		return niftiConverterVersion;
	}

	/**
	 * @param niftiConverterVersion
	 *            the niftiConverterVersion to set
	 */
	public void setNiftiConverterVersion(String niftiConverterVersion) {
		this.niftiConverterVersion = niftiConverterVersion;
	}

	/**
	 * @return the originalNiftiConversion
	 */
	public Boolean getOriginalNiftiConversion() {
		return originalNiftiConversion;
	}

	/**
	 * @param originalNiftiConversion
	 *            the originalNiftiConversion to set
	 */
	public void setOriginalNiftiConversion(Boolean originalNiftiConversion) {
		this.originalNiftiConversion = originalNiftiConversion;
	}


	public LocalDateTime getFirstImageAcquisitionTime() {
		return firstImageAcquisitionTime;
	}

	public void setFirstImageAcquisitionTime(LocalDateTime firstImageAcquisitionTime) {
		this.firstImageAcquisitionTime = firstImageAcquisitionTime;
	}

	public LocalDateTime getLastImageAcquisitionTime() {
		return lastImageAcquisitionTime;
	}

	public void setLastImageAcquisitionTime(LocalDateTime lastImageAcquisitionTime) {
		this.lastImageAcquisitionTime = lastImageAcquisitionTime;
	}
}
