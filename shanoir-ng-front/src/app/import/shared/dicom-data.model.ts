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

export class ImportJob {
    fromDicomZip: boolean;
    fromShanoirUploader: boolean;
    fromPacs: boolean;
    workFolder: string;
    patients: PatientDicom[];
    examinationId: number;
    studyId: number;
    studyCardId: number;
    acquisitionEquipmentId: number;
    converterId: number;
    archive: string;
    subjectName: String;
    studyName: String;
}

export class PatientDicom {
    subject: SimpleSubject;
    patientID: string;
    patientName: string;
    patientBirthDate: Date;
    patientSex: Sex;
    studies: StudyDicom[];
}

export class StudyDicom {
    studyInstanceUID: string;
    studyDescription: string;
    studyDate: Date;
    series: SerieDicom[];
}

export class SerieDicom {
    selected: boolean;
    seriesInstanceUID: string;
    modality: string;
    protocolName: string;
    seriesDescription: string;
    sequenceName:string;
    seriesDate: Date;
    seriesNumber: number;
    numberOfSeriesRelatedInstances: number;
    sopClassUID: string;
    equipment: EquipmentDicom; 
    institution: InstitutionDicom;
    isCompressed: boolean;
    isSpectroscopy: boolean;
    isEnhancedMR: boolean;
    isMultiFrame: boolean;
    multiFrameCount: number;
    nonImages: any[];
    nonImagesNumber: number;
    images: ImageDicom[];
    imagesNumber: number;
    datasets: any;
}

export class EquipmentDicom {
    manufacturer: string;
    manufacturerModelName: string;
    deviceSerialNumber: string;
}

export class ImageDicom {
    path: string;
    acquisitionNumber: number;
    echoNumbers: number[];
    imageOrientationPatient: number[];
}

export class InstitutionDicom {
    institutionName: string;
    institutionAddress: string;
}

export class DicomQuery {
    patientName: string = "";
    patientID: string = "";
    patientBirthDate: string = "";
    studyDescription: string = "";
    studyDate: string = "";
}