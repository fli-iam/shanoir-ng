package org.shanoir.ng.dataset.service;

import org.shanoir.ng.dataset.modality.*;
import org.shanoir.ng.dataset.model.*;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.bids.BidsDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.eeg.EegDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.pet.PetDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.xa.XaDatasetAcquisition;


public class DatasetAcquisitionUtils {
	public static DatasetAcquisition copyDatasetAcquisitionFromDatasetAcquisition(DatasetAcquisition other) {

		String type = other.getType();
		DatasetAcquisition acq = null;
			
		switch(type) {
			case CtDatasetAcquisition.datasetAcquisitionType:
				acq = new CtDatasetAcquisition(other);
				break;
			case MrDatasetAcquisition.datasetAcquisitionType:
				acq = new MrDatasetAcquisition();
				break;
			case PetDatasetAcquisition.datasetAcquisitionType:
				acq = new PetDatasetAcquisition(other);
				break;
			case EegDatasetAcquisition.datasetAcquisitionType:
				acq = new EegDatasetAcquisition(other);
				break;
			case BidsDatasetAcquisition.datasetAcquisitionType:
				acq = new BidsDatasetAcquisition(other);
				break;
			case XaDatasetAcquisition.datasetAcquisitionType:
				acq = new XaDatasetAcquisition(other);
				break;
			default:
				acq = new GenericDatasetAcquisition(other);
				break;
		}
		return acq;
	}
	
}
