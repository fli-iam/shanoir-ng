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

import { allOfEnum } from '../../utils/app.utils';

export enum StudyUserRight {

    CAN_SEE_ALL = "CAN_SEE_ALL",
    CAN_DOWNLOAD = "CAN_DOWNLOAD",
    CAN_IMPORT = "CAN_IMPORT",
    CAN_ADMINISTRATE = "CAN_ADMINISTRATE"

} export namespace StudyUserRight {
    
    const allStudyUserRights: any[] = [
        { value: StudyUserRight.CAN_SEE_ALL, label: "Can see all data in this study" },
        { value: StudyUserRight.CAN_DOWNLOAD, label: "Can download datasets from this study" },
        { value: StudyUserRight.CAN_IMPORT, label: "Can import datasets in this study" },
        { value: StudyUserRight.CAN_ADMINISTRATE, label: "Can edit the study parameters" },
    ];
    
    export function all(): Array<StudyUserRight> {
        return allOfEnum<StudyUserRight>(StudyUserRight);
    }

    export function getLabel(type: StudyUserRight) {
        let founded = allStudyUserRights.find(entry => entry.value == type);
        return founded ? founded.label : undefined;
    }

    export function getValueLabelJsonArray() {
        return allStudyUserRights;
    }
}