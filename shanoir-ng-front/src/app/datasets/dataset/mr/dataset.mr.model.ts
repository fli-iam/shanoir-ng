import { Dataset } from "../../shared/dataset.model";

declare type MrQualityProcedureType = 'MAGNETIC_FIELD_QUALITY_DATASET_LONG_ECHO_TIME' | 'MAGNETIC_FIELD_QUALITY_DATASET_SHORT_ECHO_TIME';

export class MrDataset extends Dataset {
    echoTime: EchoTime[];
    flipAngle: FlipAngle[];
    inversionTime: InversionTime[];
    repetitionTime: RepetitionTime[];
    mrQualityProcedureType: MrQualityProcedureType;
}

export class EchoTime {
    id: number;
    echoNumber: number;
    echoTimeValue: number;
}

export class FlipAngle {
    id: number;
    flipAngleValue: number;
}

export class InversionTime {
    id: number;
    inversionTimeValue: number;
}

export class RepetitionTime {
    id: number;
    repetitionTimeValue: number;
}