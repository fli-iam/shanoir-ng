import { Subject } from "../../subjects/shared/subject.model";
import { Sex } from "../../subjects/shared/subject.types";

export class ImportJob {
    fromDicomZip: boolean;
    fromShanoirUploader: boolean;
    fromPacs: boolean;
    workFolder: string;
    patients: PatientDicom[];
    examinationId: number;
    frontStudyId: number;
    frontAcquisitionEquipmentId: number;
    frontConverterId: number;
}

export class PatientDicom {
    subject: Subject;
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