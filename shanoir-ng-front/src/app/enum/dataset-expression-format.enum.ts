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

import { Option } from "../shared/select/select.component";
import { capitalsAndUnderscoresToDisplayable, allOfEnum } from "../utils/app.utils";

export enum DatasetExpressionFormat {

    ANALYZE = "ANALYZE",
    NIFTI_SINGLE_FILE = "NIFTI_SINGLE_FILE",
    NIFTI_TWO_FILES = "NIFTI_SINGLE_FILE",
    GIS = "GIS",
    INRIMAGE = "INRIMAGE",
    DICOM = "DICOM",
    MR_IMAGE_STORAGE_SOP_CLASS = "MR IMAGE STORAGE SOP CLASS",
    CT_IMAGE_STORAGE_SOP_CLASS = "CT IMAGE STORAGE SOP CLASS",
    PET_IMAGE_STORAGE_SOP_CLASS = "PET IMAGE STORAGE SOP CLASS",
    ENHANCED_MR_IMAGE_STORAGE_SOP_CLASS = "ENHANCED MR IMAGE STORAGE SOP CLASS",
    MR_SPECTROSCOPY_IMAGE_STORAGE_SOP_CLASS = "MR SPECTROSCOPY IMAGE STORAGE SOP CLASS",
    ENHANCED_CT_IMAGE_STORAGE_SOP_CLASS = "ENHANCED CT IMAGE STORAGE SOP CLASS",
    ENHANCED_PET_IMAGE_STORAGE_SOP_CLASS = "ENHANCED PET IMAGE STORAGE SOP CLASS",
    SEGMENTATION_STORAGE_SOP_CLASS = "SEGMENTATION STORAGE SOP CLASS",
    DEFORMABLE_SPATIAL_REGISTRATION_STORAGE_SOP_CLASS = "DEFORMABLE SPATIAL REGISTRATION STORAGE SOP CLASS",
    SPATIAL_REGISTRATION_STORAGE_SOP_CLASS = "SPATIAL REGISTRATION STORAGE SOP CLASS",
    SIEMENS_SPECTROSCOPY = "SIEMENS SPECTROSCOPY",
    PHILIPS_SPECTROSCOPY = "PHILIPS SPECTROSCOPY"

} export namespace DatasetExpressionFormat {
    
    export function all(): Array<DatasetExpressionFormat> {
        return allOfEnum<DatasetExpressionFormat>(DatasetExpressionFormat);
    }

    export function getLabel(type: DatasetExpressionFormat): string {
        return capitalsAndUnderscoresToDisplayable(type);
    }

    export function toOptions(): Option<DatasetExpressionFormat>[] {
        return all().map(prop => new Option<DatasetExpressionFormat>(prop, getLabel(prop)));
    }
}