export class ImportJob {
    fromDicomZip: boolean;
    fromShanoirUploader: boolean;
    fromPacs: boolean;
    patients: PatientDicom[];
}

export class PatientDicom {
    subjects: SubjectDicom;
    patientID: string;
    patientName: string;
    patientBirthDate: Date;
    patientSex: string;
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
    seriesDate: Date;
    seriesNumber: number;
    numberOfSeriesRelatedInstances: number;
    sopClassUID: string;
    equipment: EquipmentDicom; 
    isCompressed: boolean;
    isSpectroscopy: boolean;
    isMultiFrame: boolean;
    multiFrameCount: number;
    nonImages: any[];
    nonImagesNumber: number;
    images: ImageDicom[];
    imagesNumber: number;
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