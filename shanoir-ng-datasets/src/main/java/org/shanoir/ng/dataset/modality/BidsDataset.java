package org.shanoir.ng.dataset.modality;

import jakarta.persistence.Entity;
import org.shanoir.ng.dataset.model.Dataset;

@Entity
public class BidsDataset extends Dataset {

	private static final long serialVersionUID = 7476089535424633518L;

	public static final String datasetType = "BIDS";

	/** BIDS data type. */
	private String bidsDataType;

	public BidsDataset() {

	}

	public BidsDataset(Dataset other) {
		super(other);
		this.bidsDataType = ((BidsDataset) other).getBidsDataType();
	}

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
