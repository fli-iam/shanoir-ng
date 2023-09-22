package org.shanoir.ng.datasetacquisition.model.eeg;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.Entity;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;

/**
 * Dataset acquisition for EEG.
 * @author JCome
 *
 */
@Entity
@JsonTypeName("Eeg")
public class EegDatasetAcquisition extends DatasetAcquisition{

	@Override
	public String getType() {
		return "Eeg";
	}

	
}
