/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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
