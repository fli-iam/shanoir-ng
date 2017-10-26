package org.shanoir.ng.processing;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.shanoir.ng.dataset.Dataset;
import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * Input of dataset processing.
 * 
 * @author msimon
 */
@Entity
public class InputOfDatasetProcessing extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 816081606253085305L;

	/** Dataset. */
	@ManyToOne
	@JoinColumn(name = "dataset_id")
	private Dataset dataset;

	/** Dataset Processing. */
	@ManyToOne
	@JoinColumn(name = "dataset_processing_id")
	private DatasetProcessing datasetProcessing;

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

}
