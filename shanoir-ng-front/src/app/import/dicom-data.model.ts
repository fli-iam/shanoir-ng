export class ImportJob {
    fromDicomZip: boolean;
    fromShanoirUploader: boolean;
    fromPacs: boolean;
    workFolder: string;
    patients: PatientDicom[];
    frontSubjectId: number;
    frontExperimentalGroupOfSubjectId: number;
    examinationId: number;
    frontStudyId: number;
    frontStudyCardId: number;
    frontConverterId: number;
}

export class PatientDicom {
    subject: SubjectDicom;
    patientID: string;
    patientName: string;
    patientBirthDate: Date;
    patientSex: 'male' | 'female';
    studies: StudyDicom[];
}

export class SubjectDicom {
    id: string;
    name: string;
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