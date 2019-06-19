package org.shanoir.ng.datasetacquisition.dto;

import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;

/**
 * Simple dataset acquisition DTO with information for examination.
 * 
 * @author msimon
 *
 */
public class ExaminationDatasetAcquisitionDTO extends IdName {

	private List<IdName> datasets;

	/**
	 * @return the datasets
	 */
	public List<IdName> getDatasets() {
		return datasets;
	}

	/**
	 * @param datasets
	 *            the datasets to set
	 */
	public void setDatasets(List<IdName> datasets) {
		this.datasets = datasets;
	}

}
