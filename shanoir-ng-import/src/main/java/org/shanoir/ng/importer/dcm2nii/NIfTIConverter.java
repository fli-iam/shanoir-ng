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

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.security.EditableOnlyBy;
import org.shanoir.ng.shared.validation.Unique;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The class NIfTIConverter.
 * 
 * @author mkain
 *
 */
@Entity
@JsonPropertyOrder({ "_links", "id", "name" })
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class NIfTIConverter extends HalEntity {

	private static final long serialVersionUID = 4092928692466545261L;

	@NotBlank
	@Column(unique = true)
	@Unique
	@EditableOnlyBy(roles = { "ROLE_ADMIN", "ROLE_EXPERT" })
	private String name;

	private Integer niftiConverterType;

	private Boolean isActive;

	private String comment;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NIfTIConverterType getNIfTIConverterType() {
		return NIfTIConverterType.getType(niftiConverterType);
	}

	public void setNIfTIConverterType(NIfTIConverterType niftiConverterType) {
		if (niftiConverterType == null) {
			this.niftiConverterType = null;
		} else {
			this.niftiConverterType = niftiConverterType.getId();
		}
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NIfTIConverter other = (NIfTIConverter) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the String.
	 *
	 * @return the String
	 */
	@Override
	public String toString() {
		return this.name;
	}

	/**
	 * Compare to.
	 *
	 * @param other
	 *            the other
	 *
	 * @return the int
	 */
	public int compareTo(final NIfTIConverter other) {
		return this.toString().toUpperCase().compareTo(other.toString().toUpperCase());
	}

	/**
	 * Gets the display String.
	 *
	 * @return the String
	 */
	public String getDisplayString() {
		return toString();
	}

	public boolean isDcm2Nii() {
		return NIfTIConverterType.DCM2NII.equals(this.getNIfTIConverterType());
	}

	public boolean isMcverter() {
		return NIfTIConverterType.MCVERTER.equals(this.getNIfTIConverterType());
	}

	public boolean isClidcm() {
		return NIfTIConverterType.CLIDCM.equals(this.getNIfTIConverterType());
	}

	public boolean isDicom2Nifti() {
		return NIfTIConverterType.DICOM2NIFTI.equals(this.getNIfTIConverterType());
	}

	public boolean isDicomifier() {
		return NIfTIConverterType.DICOMIFIER.equals(this.getNIfTIConverterType());
	}

	public boolean isMriConverter() {
		return NIfTIConverterType.DICOMIFIER.equals(this.getNIfTIConverterType());
	}

}
