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

export enum DatasetExpressionFormat {
    ANALYZE = <any> "Analyze",
    NIFTI_SINGLE_FILE = <any> "Nifti-single-file",
    NIFTI_TWO_FILES = <any> "Nifti-two-files",
    GIS = <any> "GIS",
    INRIMAGE = <any> "INRIMAGE",
    DICOM = <any> "DICOM",
    MR_IMAGE_STORAGE_SOP_CLASS = <any> "MR IMAGE STORAGE SOP CLASS",
    CT_IMAGE_STORAGE_SOP_CLASS = <any> "CT IMAGE STORAGE SOP CLASS",
    PET_IMAGE_STORAGE_SOP_CLASS = <any> "PET IMAGE STORAGE SOP CLASS",
    ENHANCED_MR_IMAGE_STORAGE_SOP_CLASS = <any> "ENHANCED MR IMAGE STORAGE SOP CLASS",
    MR_SPECTROSCOPY_IMAGE_STORAGE_SOP_CLASS = <any> "MR SPECTROSCOPY IMAGE STORAGE SOP CLASS",
    ENHANCED_CT_IMAGE_STORAGE_SOP_CLASS = <any> "ENHANCED CT IMAGE STORAGE SOP CLASS",
    ENHANCED_PET_IMAGE_STORAGE_SOP_CLASS = <any> "ENHANCED PET IMAGE STORAGE SOP CLASS",
    SEGMENTATION_STORAGE_SOP_CLASS = <any> "SEGMENTATION STORAGE SOP CLASS",
    DEFORMABLE_SPATIAL_REGISTRATION_STORAGE_SOP_CLASS = <any> "DEFORMABLE SPATIAL REGISTRATION STORAGE SOP CLASS",
    SPATIAL_REGISTRATION_STORAGE_SOP_CLASS = <any> "SPATIAL REGISTRATION STORAGE SOP CLASS",
    SIEMENS_SPECTROSCOPY = <any> "SIEMENS SPECTROSCOPY",
    PHILIPS_SPECTROSCOPY = <any> "PHILIPS SPECTROSCOPY"
}