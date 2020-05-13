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

package org.shanoir.ng.dataset.model;

/**
 * Dataset expression format.
 * 
 * @author msimon
 *
 */
public enum DatasetExpressionFormat {
	
	// Analyze
	ANALYZE(1),

	// Nifti-single-file
	NIFTI_SINGLE_FILE(2),

	// Nifti-two-files
	NIFTI_TWO_FILES(3),

	// GIS
	GIS(4),

	// INRIMAGE
	INRIMAGE(5),

	// DICOM
	DICOM(6),

	// MR IMAGE STORAGE SOP CLASS
	MR_IMAGE_STORAGE_SOP_CLASS(7),

	// CT IMAGE STORAGE SOP CLASS
	CT_IMAGE_STORAGE_SOP_CLASS(8),

	// PET IMAGE STORAGE SOP CLASS
	PET_IMAGE_STORAGE_SOP_CLASS(9),

	// ENHANCED MR IMAGE STORAGE SOP CLASS
	ENHANCED_MR_IMAGE_STORAGE_SOP_CLASS(10),

	// MR SPECTROSCOPY IMAGE STORAGE SOP CLASS
	MR_SPECTROSCOPY_IMAGE_STORAGE_SOP_CLASS(11),

	// ENHANCED CT IMAGE STORAGE SOP CLASS
	ENHANCED_CT_IMAGE_STORAGE_SOP_CLASS(12),

	// ENHANCED PET IMAGE STORAGE SOP CLASS
	ENHANCED_PET_IMAGE_STORAGE_SOP_CLASS(13),

	// SEGMENTATION STORAGE SOP CLASS
	SEGMENTATION_STORAGE_SOP_CLASS(14),

	// DEFORMABLE SPATIAL REGISTRATION STORAGE SOP CLASS
	DEFORMABLE_SPATIAL_REGISTRATION_STORAGE_SOP_CLASS(15),

	// SPATIAL REGISTRATION STORAGE SOP CLASS
	SPATIAL_REGISTRATION_STORAGE_SOP_CLASS(16),

	// SIEMENS SPECTROSCOPY
	SIEMENS_SPECTROSCOPY(17),

	// PHILIPS SPECTROSCOPY
	PHILIPS_SPECTROSCOPY(18),

	// EEG
	EEG(19);
	
	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private DatasetExpressionFormat(final int id) {
		this.id = id;
	}

	/**
	 * Get a dataset expression format by its id.
	 * 
	 * @param id
	 *            format id.
	 * @return dataset expression format.
	 */
	public static DatasetExpressionFormat getFormat(final Integer id) {
		if (id == null) {
			return null;
		}
		for (DatasetExpressionFormat format : DatasetExpressionFormat.values()) {
			if (id.equals(format.getId())) {
				return format;
			}
		}
		throw new IllegalArgumentException("No matching dataset expression format for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
