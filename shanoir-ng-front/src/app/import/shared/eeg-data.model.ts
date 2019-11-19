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

import { SimpleSubject } from "../../subjects/shared/subject.model";
import { Sex } from "../../subjects/shared/subject.types";

export class EegImportJob {
    workFolder: string;
    examinationId: number;
    frontStudyId: number;
    subjectId: number;
    frontAcquisitionEquipmentId: number;
    channels: Channel[];
    events: Event[];
    name: string;
    files: string[];
}

/** Represents a brainvision EEG channel */
export class Channel {
    name: string;
    resolution: number;
    referenceUnits: string;
    lowCutoff: number;
    highCutoff: number;
    notch: number;
    x: number;
    y: number;
    z: number;
}

/** Represents a brainvision event */
export class Event {
    type: string;
    description: string;
    position: string;
    points: number;
    channelNumber: number;
    date: Date;
}

export class EquipmentEeg {
    manufacturer: string;
    manufacturerModelName: string;
    deviceSerialNumber: string;
}

export class InstitutionEeg {
    institutionName: string;
    institutionAddress: string;
}