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