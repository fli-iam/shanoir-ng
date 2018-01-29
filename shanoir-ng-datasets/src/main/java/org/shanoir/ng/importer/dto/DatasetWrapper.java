package org.shanoir.ng.importer.dto;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import java.util.Map;


import org.shanoir.ng.shared.model.EchoTime;
import org.shanoir.ng.shared.model.FlipAngle;
import org.shanoir.ng.shared.model.InversionTime;
import org.shanoir.ng.shared.model.RepetitionTime;


public class DatasetWrapper<T> {
	
	private List<T> datasets;
	private Date firstImageAcquisitionTime;
	private Date lastImageAcquisitionTime;
	
	 
	private Map<Integer,EchoTime> echoTimes;

	
	private Map<Double,FlipAngle> flipAngles;
	
	
	private Map<Double,InversionTime> inversionTimes;

	
	private Map<Double,RepetitionTime> repetitionTimes;
	
	
	public List<T> getDataset() {
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
	
//	public Map<Integer, EchoTime> getEchoTimes() {
//		if (echoTimes == null) {
//				return new HashMap<Integer,EchoTime>();
//		}
//		return echoTimes;
//	}
//
//	public Map<Double, FlipAngle> getFlipAngles() {
//		if (flipAngles == null) {
//			return new HashMap<Double,FlipAngle>();
//		}
//		return flipAngles;
//	}
//
//	public Map<Double, InversionTime> getInversionTimes() {
//		if (inversionTimes == null) {
//			return new HashMap<Double,InversionTime>();
//		}		
//		return inversionTimes;
//	}
//
//	public Map<Double, RepetitionTime> getRepetitionTimes() {
//		if (repetitionTimes == null) {
//			return new HashMap<Double,RepetitionTime>();
//		}
//		return repetitionTimes;
//	}
//	

}
