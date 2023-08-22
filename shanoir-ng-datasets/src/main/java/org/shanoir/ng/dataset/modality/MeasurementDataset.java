package org.shanoir.ng.dataset.modality;

import jakarta.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;

@Entity
public class MeasurementDataset extends Dataset {

	private static final long serialVersionUID = 7476089535424634218L;

	public static final String datasetType = "Measurement";
	
	@Override
	public String getType() {
		return datasetType;
	}

}
