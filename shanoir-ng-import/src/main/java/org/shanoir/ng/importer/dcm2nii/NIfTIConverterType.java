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

package org.shanoir.ng.importer.dcm2nii;

/**
 * NIfTIConverterType
 * 
 * @author mkain
 *
 */
public enum NIfTIConverterType {

	DCM2NII(1),

	MCVERTER(2),

	CLIDCM(3),
	
	DICOM2NIFTI(4),

	DICOMIFIER(5);
	
	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private NIfTIConverterType(final int id) {
		this.id = id;
	}

	/**
	 * Get a type by its id.
	 * 
	 * @param id
	 *            format id.
	 * @return type of converter
	 */
	public static NIfTIConverterType getType(final Integer id) {
		if (id == null) {
			return null;
		}
		for (NIfTIConverterType type : NIfTIConverterType.values()) {
			if (id.equals(type.getId())) {
				return type;
			}
		}
		throw new IllegalArgumentException("No matching type for id " + id);
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}