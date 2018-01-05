package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.Dataset;

/**
 * Parameter quantification dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class ParameterQuantificationDataset extends Dataset {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 7649321017486497468L;
	
	/** Parameter Quantification Dataset Nature. */
	private Integer parameterQuantificationDatasetNature;

	/**
	 * @return the parameterQuantificationDatasetNature
	 */
	public ParameterQuantificationDatasetNature getParameterQuantificationDatasetNature() {
		return ParameterQuantificationDatasetNature.getNature(parameterQuantificationDatasetNature);
	}

	/**
	 * @param parameterQuantificationDatasetNature
	 *            the parameterQuantificationDatasetNature to set
	 */
	public void setParameterQuantificationDatasetNature(
			ParameterQuantificationDatasetNature parameterQuantificationDatasetNature) {
		if (parameterQuantificationDatasetNature == null) {
			this.parameterQuantificationDatasetNature = null;
		} else {
			this.parameterQuantificationDatasetNature = parameterQuantificationDatasetNature.getId();
		}
	}

}
