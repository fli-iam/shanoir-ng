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

package org.shanoir.ng.shared.dicom;


import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.shanoir.ng.utils.Utils;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author yyao
 *
 */
public class InstitutionDicom {

	private static final String UNKNOWN = "unknown";
	
	@JsonProperty("institutionName")
	private String institutionName;

	@JsonProperty("institutionAddress")
	private String institutionAddress;

	public InstitutionDicom() {}

	public InstitutionDicom(Attributes attributes) {
		this.institutionName = Utils.getOrSetToDefault(attributes, Tag.InstitutionName, UNKNOWN);
		this.institutionAddress = Utils.getOrSetToDefault(attributes, Tag.InstitutionAddress, UNKNOWN);
	}
	
	public String getInstitutionName() {
		return institutionName;
	}

	public void setInstitutionName(String institutionName) {
		this.institutionName = institutionName;
	}

	public String getInstitutionAddress() {
		return institutionAddress;
	}

	public void setInstitutionAddress(String institutionAddress) {
		this.institutionAddress = institutionAddress;
	}

}
