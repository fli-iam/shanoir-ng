package org.shanoir.ng.datasetfile;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.shanoir.ng.dataset.DatasetExpression;
import org.shanoir.ng.shared.model.AbstractGenericItem;

/**
 * Dataset file.
 * 
 * @author msimon
 *
 */
@Entity
public class DatasetFile extends AbstractGenericItem {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -3272282965762717831L;

	/** The dataset expression. */
	@ManyToOne
	@JoinColumn(name = "dataset_expression_id")
	private DatasetExpression datasetExpression;

	private String path;

	/**
	 * @return the datasetExpression
	 */
	public DatasetExpression getDatasetExpression() {
		return datasetExpression;
	}

	/**
	 * @param datasetExpression
	 *            the datasetExpression to set
	 */
	public void setDatasetExpression(DatasetExpression datasetExpression) {
		this.datasetExpression = datasetExpression;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

}
