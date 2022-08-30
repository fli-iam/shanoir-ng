package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;

@Entity
public class MeasurementDataset extends Dataset {

	private static final long serialVersionUID = 7476089535424634218L;

	public static final String datasetType = "Measurement";
	
	private String trackingIdentifier;
	
	private String numericValue;
	
	private String graphicData;

	@Override
	public String getType() {
		return datasetType;
	}

	public String getTrackingIdentifier() {
		return trackingIdentifier;
	}

	public void setTrackingIdentifier(String trackingIdentifier) {
		this.trackingIdentifier = trackingIdentifier;
	}

	public String getNumericValue() {
		return numericValue;
	}

	public void setNumericValue(String numericValue) {
		this.numericValue = numericValue;
	}

	public String getGraphicData() {
		return graphicData;
	}

	public void setGraphicData(String graphicData) {
		this.graphicData = graphicData;
	}

}
