package org.shanoir.ng.dataset;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.processing.DatasetProcessingType;
import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * Dataset expression.
 * 
 * @author msimon
 */
@Entity
public class DatasetExpression extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -3269594490145201917L;

	/** List of dataset expressions coming from this dataset expression. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "originalDatasetExpression", cascade = { CascadeType.MERGE,
			CascadeType.REFRESH, CascadeType.PERSIST })
	private List<DatasetExpression> comingFromDatasetExpressions;

	/** Creation date of the dataset expression. */
	private LocalDate creationDate;

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
	public LocalDate getExpressionCreationDate() {
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
    		datasetFiles = new ArrayList<DatasetFile>();
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

}
