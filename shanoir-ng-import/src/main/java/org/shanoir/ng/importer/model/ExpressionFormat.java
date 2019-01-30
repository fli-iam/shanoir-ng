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

package org.shanoir.ng.importer.model;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.importer.dcm2nii.NIfTIConverter;

public class ExpressionFormat {

	private String type;
	
	private List<DatasetFile> datasetFiles = new ArrayList<DatasetFile>();
	
	private NIfTIConverter niftiConverter;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<DatasetFile> getDatasetFiles() {
		if (datasetFiles == null) {
			datasetFiles = new ArrayList<DatasetFile>();
		}
		return datasetFiles;
	}

	public void setDatasetFiles(List<DatasetFile> datasetFiles) {
		this.datasetFiles = datasetFiles;
	}

	public NIfTIConverter getNiftiConverter() {
		return niftiConverter;
	}

	public void setNiftiConverter(NIfTIConverter niftiConverter) {
		this.niftiConverter = niftiConverter;
	}
	
}
