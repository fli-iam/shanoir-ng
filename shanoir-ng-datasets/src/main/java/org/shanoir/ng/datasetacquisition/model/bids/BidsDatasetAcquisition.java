package org.shanoir.ng.datasetacquisition.model.bids;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.Entity;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;

@Entity
@JsonTypeName("BIDS")
public class BidsDatasetAcquisition extends DatasetAcquisition {

	public static final String datasetAcquisitionType = "BIDS";
	private static final long serialVersionUID = -4654922391836952469L;

	public BidsDatasetAcquisition() {

	}

	public BidsDatasetAcquisition(DatasetAcquisition other) {
		super(other);
	}

	@Override
	public String getType() {
		return "BIDS";
	}

}
