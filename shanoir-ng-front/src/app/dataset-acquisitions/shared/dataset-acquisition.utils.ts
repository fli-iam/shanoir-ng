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
 * anumber with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */
import { CtDatasetAcquisition } from '../modality/ct/ct-dataset-acquisition.model';
import { EegDatasetAcquisition } from '../modality/eeg/eeg-dataset-acquisition.model';
import { MrDatasetAcquisition } from '../modality/mr/mr-dataset-acquisition.model';
import { PetDatasetAcquisition } from '../modality/pet/pet-dataset-acquisition.model';
import { GenericDatasetAcquisition } from '../modality/generic-dataset-acquisition.model';
import { ProcessedDatasetAcquisition } from '../modality/processed/processed-dataset-acquisition.model';

import { DatasetAcquisition } from './dataset-acquisition.model';


export abstract class DatasetAcquisitionUtils {

    static getNewDAInstance(type: string): DatasetAcquisition {
        switch(type) {
            case 'Mr': return new MrDatasetAcquisition();
            case 'Pet': return new PetDatasetAcquisition();
            case 'Ct': return new CtDatasetAcquisition();
            case 'Generic': return new GenericDatasetAcquisition();
            case 'Eeg': return new EegDatasetAcquisition();
 	    case 'Processed': return new ProcessedDatasetAcquisition();
            default: throw new Error('Received dataset acquisition has no valid "type" property');
        }
    }
}