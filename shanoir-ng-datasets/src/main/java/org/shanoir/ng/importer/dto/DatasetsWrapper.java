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

package org.shanoir.ng.importer.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;

/**
 * This class wraps logic, like firstImage- and lastImageAcquisitionTime,
 * that concerns a set of specific datasets to transfer common information.
 * 
 * @author atouboul, mkain
 *
 * @param <T>
 */
public class DatasetsWrapper<T> {
	
	private List<T> datasets;

	@LocalDateAnnotations
	private LocalDateTime firstImageAcquisitionTime;
	
	@LocalDateAnnotations
	private LocalDateTime lastImageAcquisitionTime;
	
	public List<T> getDatasets() {
		if (datasets == null) {
			this.datasets = new ArrayList<T>();
		}
		return datasets;
	}

	public void setDataset(List<T> datasetList) {
		this.datasets = datasetList;
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
