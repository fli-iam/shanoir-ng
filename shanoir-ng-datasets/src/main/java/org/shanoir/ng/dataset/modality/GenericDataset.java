package org.shanoir.ng.dataset.modality;

import jakarta.persistence.Entity;
import net.bytebuddy.description.type.TypeList;
import org.shanoir.ng.dataset.model.Dataset;

@Entity
public class GenericDataset extends Dataset {

	public static final String datasetType = "Generic";


	/**
	 * Serial version UUID
	 */
	private static final long serialVersionUID = -5363216669486303309L;

	public GenericDataset() {

	}

	public GenericDataset(Dataset other) {
		super(other);
	}

	@Override
	public String getType() {
		return datasetType;
	}

}
