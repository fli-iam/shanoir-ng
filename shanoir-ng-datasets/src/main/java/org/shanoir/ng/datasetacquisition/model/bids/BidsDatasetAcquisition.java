package org.shanoir.ng.datasetacquisition.model.bids;

import javax.persistence.Entity;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Dataset acquisition for BIDS (nifti only).
 * @author JCome
 *
 */
@Entity
@JsonTypeName("Bids")
public class BidsDatasetAcquisition extends DatasetAcquisition {

	@Override
	public String getType() {
		return "Bids";
	}

}
