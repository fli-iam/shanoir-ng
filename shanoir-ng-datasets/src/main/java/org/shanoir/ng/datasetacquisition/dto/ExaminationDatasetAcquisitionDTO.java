package org.shanoir.ng.datasetacquisition.dto;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * Simple dataset acquisition DTO with information for examination.
 * 
 * @author msimon
 *
 */
public class ExaminationDatasetAcquisitionDTO extends IdNameDTO {

	private List<IdNameDTO> datasets;

	/**
	 * @return the datasets
	 */
	public List<IdNameDTO> getDatasets() {
		return datasets;
	}

	/**
	 * @param datasets
	 *            the datasets to set
	 */
	public void setDatasets(List<IdNameDTO> datasets) {
		this.datasets = datasets;
	}

}
