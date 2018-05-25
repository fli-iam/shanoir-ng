package org.shanoir.ng.importer.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

	private Date firstImageAcquisitionTime;
	
	private Date lastImageAcquisitionTime;
	
	public List<T> getDatasets() {
		if (datasets == null) {
			this.datasets = new ArrayList<T>();
		}
		return datasets;
	}

	public void setDataset(List<T> datasetList) {
		this.datasets = datasetList;
	}
	
	public Date getFirstImageAcquisitionTime() {
		return firstImageAcquisitionTime;
	}
	
	public void setFirstImageAcquisitionTime(Date firstImageAcquisitionTime) {
		this.firstImageAcquisitionTime = firstImageAcquisitionTime;
	}
	
	public Date getLastImageAcquisitionTime() {
		return lastImageAcquisitionTime;
	}
	
	public void setLastImageAcquisitionTime(Date lastImageAcquisitionTime) {
		this.lastImageAcquisitionTime = lastImageAcquisitionTime;
	}

}
