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

import { allOfEnum, capitalsAndUnderscoresToDisplayable } from '../utils/app.utils';
import { Option } from '../shared/select/select.component';

export enum DatasetModalityType {

    PET = 'PET_DATASET',
    MR = 'MR_DATASET',
    CT = 'CT_DATASET',
    // MEG = 'MG',
    // SPECT = 'SPECT',
    EEG = 'EEG_DATASET'

} export namespace DatasetModalityType {
    
    export function all(): Array<DatasetModalityType> {
        return allOfEnum<DatasetModalityType>(DatasetModalityType);
    }

    export function getLabel(type: DatasetModalityType): string {
        return capitalsAndUnderscoresToDisplayable(type.split('_')[0]);
    }

    export var options: Option<DatasetModalityType>[] = all().map(prop => new Option<DatasetModalityType>(prop, getLabel(prop)));
}