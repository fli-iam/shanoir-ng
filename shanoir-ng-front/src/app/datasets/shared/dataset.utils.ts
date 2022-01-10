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

import { EegDataset } from '../dataset/eeg/dataset.eeg.model';
import { MrDataset } from '../dataset/mr/dataset.mr.model';
import { DatasetType } from './dataset-type.model';
import { Dataset } from './dataset.model';


export abstract class DatasetUtils{

    static getDatasetInstance(type: DatasetType): Dataset {
        if (type == 'Mr') return new MrDataset();
        if (type == 'Eeg') return new EegDataset();
        else return new MrDataset(); 
    }

    static getEntityInstance(entity: Dataset) { 
        return DatasetUtils.getDatasetInstance(entity.type);
    }

}