package org.shanoir.ng.exchange.model;
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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class describes an exchange of data with sh-ng.
 * An exchange of data can be an import or an export.
 * 
 * For the moment we consider even multiple-patient-multi-study(Dicom)
 * imports as using the same study and study card (== same machine).
 * 
 * @author mkain
 */
public class Exchange {
	
	/**
	 * Study, used for data exchange. Mandatory element.
	 */
	@JsonProperty("exStudy")
	private ExStudy exStudy;
	
	/**
	 * null or empty: images in this exchange have already be anonymised, so do nothing
	 * not empty: images shall be anonymised when doing the import with this profile
	 */
	@JsonProperty("anonymisationProfileToUse")
	private String anonymisationProfileToUse;

	// let's see if this is still necessary here
	@JsonProperty("workFolder")
	private String workFolder;

	public ExStudy getExStudy() {
		return exStudy;
	}

	public String getAnonymisationProfileToUse() {
		return anonymisationProfileToUse;
	}

	public String getWorkFolder() {
		return workFolder;
	}

	public void setExStudy(ExStudy exStudy) {
		this.exStudy = exStudy;
	}

	public void setAnonymisationProfileToUse(String anonymisationProfileToUse) {
		this.anonymisationProfileToUse = anonymisationProfileToUse;
	}

	public void setWorkFolder(String workFolder) {
		this.workFolder = workFolder;
	}

}
