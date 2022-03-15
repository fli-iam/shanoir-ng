package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;

@Entity
public class BidsDataset extends Dataset {

	private static final long serialVersionUID = 7476089535424633518L;

	public static final String datasetType = "BIDS";

	/** BIDS data type. */
	private String bidsDataType;

	@Override
	public String getType() {
		return datasetType;
	}

	public String getBidsDataType() {
		return bidsDataType;
	}

	public void setBidsDataType(String bidsDataType) {
		this.bidsDataType = bidsDataType;
	}


}
