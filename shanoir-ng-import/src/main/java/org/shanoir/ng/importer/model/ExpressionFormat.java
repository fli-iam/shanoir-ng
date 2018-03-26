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
