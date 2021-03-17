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

package org.shanoir.ng.datasetacquisition.dto;

import java.util.List;

import org.shanoir.ng.dataset.dto.DatasetAndProcessingsDTO;
import org.shanoir.ng.shared.core.model.IdName;

/**
 * Simple dataset acquisition DTO with information for examination.
 * 
 * @author msimon
 *
 */
public class ExaminationDatasetAcquisitionDTO extends IdName {

	private String type;

	private List<DatasetAndProcessingsDTO> datasets;
	
	private Long studyId;
	
	private Integer sortingIndex;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the datasets
	 */
	public List<DatasetAndProcessingsDTO> getDatasets() {
		return datasets;
	}

	/**
	 * @param datasets the datasets to set
	 */
	public void setDatasets(List<DatasetAndProcessingsDTO> datasets) {
		this.datasets = datasets;
	}

	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}
	
	public Integer getSortingIndex() {
		return sortingIndex;
	}

	public void setSortingIndex(Integer sortingIndex) {
		this.sortingIndex = sortingIndex;
	}
}
