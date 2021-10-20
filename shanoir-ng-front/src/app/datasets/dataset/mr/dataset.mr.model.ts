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
import { Option } from '../../../shared/select/select.component';
import { allOfEnum, capitalsAndUnderscoresToDisplayable } from '../../../utils/app.utils';
import { Dataset } from '../../shared/dataset.model';
import { DiffusionGradient } from '../../../dataset-acquisitions/modality/mr/mr-protocol.model';
import { DatasetType } from 'bin/src/app/datasets/shared/dataset-type.model';

export class MrDataset extends Dataset {
    diffusionGradients: DiffusionGradient[];
    echoTime: EchoTime[];
    flipAngle: FlipAngle[];
    inversionTime: InversionTime[];
    repetitionTime: RepetitionTime[];
    mrQualityProcedureType: MrQualityProcedureType;
    originMrMetadata: MrDatasetMetadata;
    updatedMrMetadata: MrDatasetMetadata = new MrDatasetMetadata();
	firstImageAcquisitionTime: string;
    lastImageAcquisitionTime: string;
    constructor() {
        super();
        this.type = DatasetType.Mr;
    }
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

export class MrDatasetMetadata {
    mrDatasetNature: MrDatasetNature;
}

export enum MrDatasetNature {

    T1_WEIGHTED_MR_DATASET = "T1_WEIGHTED_MR_DATASET",
    T2_WEIGHTED_MR_DATASET = 'T2_WEIGHTED_MR_DATASET',
    T2_STAR_WEIGHTED_MR_DATASET = 'T2_STAR_WEIGHTED_MR_DATASET',
    PROTON_DENSITY_WEIGHTED_MR_DATASET = 'PROTON_DENSITY_WEIGHTED_MR_DATASET',
    DIFFUSION_WEIGHTED_MR_DATASET = 'DIFFUSION_WEIGHTED_MR_DATASET',
    VELOCITY_ENCODED_ANGIO_MR_DATASET = 'VELOCITY_ENCODED_ANGIO_MR_DATASET',
    TIME_OF_FLIGHT_MR_DATASET = 'TIME_OF_FLIGHT_MR_DATASET',
    CONTRAST_AGENT_USED_ANGIO_MR_DATASET = 'CONTRAST_AGENT_USED_ANGIO_MR_DATASET',
    SPIN_TAGGING_PERFUSION_MR_DATASET = 'SPIN_TAGGING_PERFUSION_MR_DATASET',
    T1_WEIGHTED_DCE_MR_DATASET = 'T1_WEIGHTED_DCE_MR_DATASET',
    T2_WEIGHTED_DCE_MR_DATASET = 'T2_WEIGHTED_DCE_MR_DATASET',
    T2_STAR_WEIGHTED_DCE_MR_DATASET = 'T2_STAR_WEIGHTED_DCE_MR_DATASET',
    FIELD_MAP_DATASET_SHORT_ECHO_TIME = 'FIELD_MAP_DATASET_SHORT_ECHO_TIME',
    FIELD_MAP_DATASET_LONG_ECHO_TIME = 'FIELD_MAP_DATASET_LONG_ECHO_TIME',
    H1_SINGLE_VOXEL_SPECTROSCOPY_DATASET = 'H1_SINGLE_VOXEL_SPECTROSCOPY_DATASET',
    H1_SPECTROSCOPIC_IMAGING_DATASET = 'H1_SPECTROSCOPIC_IMAGING_DATASET'

} export namespace MrDatasetNature {
    
    export function all(): Array<MrDatasetNature> {
        return allOfEnum<MrDatasetNature>(MrDatasetNature);
    }

    export function getLabel(type: MrDatasetNature): string {
        return capitalsAndUnderscoresToDisplayable(type);
    }

    export var options: Option<MrDatasetNature>[] = all().map(prop => new Option<MrDatasetNature>(prop, getLabel(prop)));
}

export enum MrQualityProcedureType {

    MAGNETIC_FIELD_QUALITY_DATASET_LONG_ECHO_TIME = "MAGNETIC_FIELD_QUALITY_DATASET_LONG_ECHO_TIME",
    MAGNETIC_FIELD_QUALITY_DATASET_SHORT_ECHO_TIME = 'MAGNETIC_FIELD_QUALITY_DATASET_SHORT_ECHO_TIME'

} export namespace MrQualityProcedureType {
    
    export function all(): Array<MrQualityProcedureType> {
        return allOfEnum<MrQualityProcedureType>(MrQualityProcedureType);
    }

    export function getLabel(type: MrQualityProcedureType): string {
        return capitalsAndUnderscoresToDisplayable(type);
    }

    export var options: Option<MrQualityProcedureType>[] = all().map(prop => new Option<MrQualityProcedureType>(prop, getLabel(prop)));
}