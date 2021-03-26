package org.shanoir.ng.datasetacquisition.model.eeg;

import javax.persistence.Entity;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;

import com.fasterxml.jackson.annotation.JsonTypeName;

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
