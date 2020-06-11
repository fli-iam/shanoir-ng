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

import { allOfEnum } from "../../utils/app.utils";

export enum DatasetModalityType {
    PET_DATASET = 'PET',
    MR_DATASET = 'MR',
    CT_DATASET = 'CT'
}

export namespace DatasetModalityType {
    const allDatasetModalityTypes: any[] = [
        { value: DatasetModalityType.PET_DATASET, label: "PET" },
        { value: DatasetModalityType.MR_DATASET, label: "MR" },
        { value: DatasetModalityType.CT_DATASET, label: "CT" }
    ];
    
    export function getLabel(nature: DatasetModalityType) {
        let founded = allDatasetModalityTypes.find(entry => entry.value == nature);
        return founded ? founded.label : undefined;
    }

    export function getValueLabelJsonArray() {
        return allDatasetModalityTypes;
    }
    export function all(): Array<DatasetModalityType> {
        return allOfEnum<DatasetModalityType>(DatasetModalityType);
    }
}