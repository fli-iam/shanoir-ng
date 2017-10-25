package org.shanoir.ng.processing;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import org.joda.time.LocalDate;
import org.shanoir.ng.dataset.Dataset;
import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * Dataset Processing.
 * 
 * @author msimon
 */
@Entity
public class DatasetProcessing extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 9196056506956939617L;

	/**
	 * A comment on the dataset processing . Could be the command line of the
	 * processing.
	 */
	private String comment;

	/** Dataset Processing Type. */
	private Integer datasetProcessingType;

	/** Relations between the dataset porcessings and the datasets. */
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "datasetProcessing")
	private List<InputOfDatasetProcessing> inputOfDatasetProcessings;

	/** Output Dataset List. */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "datasetProcessing")
	private List<Dataset> outputDatasets;

	/** Date of the dataset processing. */
	private LocalDate processingDate;

	/** The study for which this dataset is a result. */
	@NotNull
	private Long studyId;

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
	 * @return the inputOfDatasetProcessings
	 */
	public List<InputOfDatasetProcessing> getInputOfDatasetProcessings() {
		return inputOfDatasetProcessings;
	}

	/**
	 * @param inputOfDatasetProcessings
	 *            the inputOfDatasetProcessings to set
	 */
	public void setInputOfDatasetProcessings(List<InputOfDatasetProcessing> inputOfDatasetProcessings) {
		this.inputOfDatasetProcessings = inputOfDatasetProcessings;
	}

	/**
	 * @return the outputDatasets
	 */
	public List<Dataset> getOutputDatasets() {
		return outputDatasets;
	}

	/**
	 * @param outputDatasets
	 *            the outputDatasets to set
	 */
	public void setOutputDatasets(List<Dataset> outputDatasets) {
		this.outputDatasets = outputDatasets;
	}

	/**
	 * @return the processingDate
	 */
	public LocalDate getProcessingDate() {
		return processingDate;
	}

	/**
	 * @param processingDate
	 *            the processingDate to set
	 */
	public void setProcessingDate(LocalDate processingDate) {
		this.processingDate = processingDate;
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

}
