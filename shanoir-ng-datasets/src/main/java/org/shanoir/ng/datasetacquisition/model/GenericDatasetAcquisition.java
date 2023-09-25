package org.shanoir.ng.datasetacquisition.model;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.Entity;

@Entity
@JsonTypeName("Generic")
public class GenericDatasetAcquisition extends DatasetAcquisition {

	/**
	 * Serial version UUID
	 */
	private static final long serialVersionUID = -8826440216825057112L;

	@Override
	public String getType() {
		return "Generic";
	}

}
