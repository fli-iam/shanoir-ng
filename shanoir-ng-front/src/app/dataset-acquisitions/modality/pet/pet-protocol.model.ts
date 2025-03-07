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

export class PetProtocol {
    
    attenuationCorrectionMethod: string
    convolutionKernel: string
    decayCorrection: string
    decayFactor: number
    dimensionX: number
    dimensionY: number
    doseCalibrationFactor: number
    energyWindowLowerLimit: number
    energyWindowUpperLimit: number
    numberOfIterations: string
    numberOfSlices: number
    numberOfSubsets: string
    radionuclideHalfLife: number
    radionuclideTotalDose: number
    radiopharmaceuticalCode: string
    randomsCorrectionMethod: string
    reconstructionMethod: string
    rescaleSlope: number
    rescaleType: string
    scatterCorrectionMethod: string
    scatterFractionFactor: number
    units: string
    voxelSizeX: string
    voxelSizeY: string
    voxelSizeZ: string
}