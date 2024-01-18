package org.shanoir.ng.datasetacquisition.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.Entity;
import org.shanoir.ng.dataset.modality.GenericDataset;

@Entity
@JsonTypeName("Generic")
public class GenericDatasetAcquisition extends DatasetAcquisition {

	public static final String datasetAcquisitionType = "Generic";
	/**
	 * Serial version UUID
	 */
	private static final long serialVersionUID = -8826440216825057112L;

	public GenericDatasetAcquisition() {

	}

	public GenericDatasetAcquisition(DatasetAcquisition other) {
		super(other);
	}

	@Override
	public String getType() {
		return "Generic";
	}

}
