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
			
		switch (type) {
			case CtDatasetAcquisition.DATASET_ACQUISITION_TYPE:
				acq = new CtDatasetAcquisition(other);
				break;
			case MrDatasetAcquisition.DATASET_ACQUISITION_TYPE:
				acq = new MrDatasetAcquisition();
				break;
			case PetDatasetAcquisition.DATASET_ACQUISITION_TYPE:
				acq = new PetDatasetAcquisition(other);
				break;
			case EegDatasetAcquisition.DATASET_ACQUISITION_TYPE:
				acq = new EegDatasetAcquisition(other);
				break;
			case BidsDatasetAcquisition.DATASET_ACQUISITION_TYPE:
				acq = new BidsDatasetAcquisition(other);
				break;
			case XaDatasetAcquisition.DATASET_ACQUISITION_TYPE:
				acq = new XaDatasetAcquisition(other);
				break;
			default:
				acq = new GenericDatasetAcquisition(other);
				break;
		}
		return acq;
	}
	
}
