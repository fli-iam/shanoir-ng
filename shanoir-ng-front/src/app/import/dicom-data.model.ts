export class PatientsDicom {
    patients: PatientDicom[];
}

export class PatientDicom {
    patientID: string;
    patientName: string;
    patientBirthDate: Date;
    patientSex: string;
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
    seriesDate: Date;
    seriesNumber: number;
    numberOfSeriesRelatedInstances: number;
    sopClassUID: string;
    equipment: EquipmentDicom;
    isCompressed: boolean;
    nonImages: any[];
    nonImagesNumber: number;
    images: string[];
    imagesNumber: number;
}

export class EquipmentDicom {
    manufacturer: string;
    manufacturerModelName: string;
    deviceSerialNumber: string;
}
