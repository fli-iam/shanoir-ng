package org.shanoir.ng.dataset.modality;

import jakarta.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetType;

@Entity
public class MeasurementDataset extends Dataset {

	private static final long serialVersionUID = 7476089535424634218L;

	public MeasurementDataset() { }

	public MeasurementDataset(Dataset other) {
		super(other);
	}

	@Override
	public DatasetType getType() {
		return DatasetType.Measurement;
	}

}
