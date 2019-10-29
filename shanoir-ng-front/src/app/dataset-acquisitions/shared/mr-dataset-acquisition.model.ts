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
 * anumber with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */
import { DatasetAcquisition } from './dataset-acquisition.model';


export class MrDatasetAcquisition extends DatasetAcquisition {

    protocol: MrProtocol;
}


export class MrProtocol {

    acquisitionDuration: number;
    acquisitionResolutionX: number;
    acquisitionResolutionY: number;
    diffusionGradients: DiffusionGradient[];
    echoTrainLength: number;
    filters: string;
    fovX: number;
    fovY: number;
    imagedNucleus: number;
    imagingFrequency: number;
    mrDatasetAcquisition: MrDatasetAcquisition;
    numberOfAverages: number;
    numberOfPhaseEncodingSteps: number;
    numberOfTemporalPositions: number;
    patientPosition: number;
    percentPhaseFov: number;
    percentSampling: number;
    pixelBandwidth: number;
    pixelSpacingX: number;
    pixelSpacingY: number;
    sliceSpacing: number;
    sliceThickness: number;
    temporalResolution: number;
    originMetadata: MrProtocolMetadata;
    updatedMetadata: MrProtocolMetadata;
}


export class DiffusionGradient {

    diffusionGradientBValue: number;
    diffusionGradientOrientationX: number;
    diffusionGradientOrientationY: number;
    diffusionGradientOrientationZ: number;
}


export class MrProtocolMetadata {

    serialVersionUID: number;
    acquisitionContrast: number;
    contrastAgentConcentration: number;
    contrastAgentUsed: number;
    injectedVolume: number;
    magnetizationTransfer: boolean;
    mrSequenceKSpaceFill: number;
    mrSequenceName: string;
    mrScanningSequence: number[];
    mrSequenceVariant: number[];
    name: string;
    parallelAcquisition: boolean;
    parallelAcquisitionTechnique: number;
    receivingCoilId: number;
    sliceOrder: number;
    sliceOrientationAtAcquisition: number;
    timeReductionFactorForTheInPlaneDirection: number;
    timeReductionFactorForTheOutOfPlaneDirection: number;
    transmittingCoilId: number;
}
