package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;

@Entity
public class GenericDataset extends Dataset {

	/**
	 * Serial version UUID
	 */
	private static final long serialVersionUID = -5363216669486303309L;

	@Override
	public String getType() {
		return "Generic";
	}

}
