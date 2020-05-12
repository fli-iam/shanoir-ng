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
package org.shanoir.ng.study.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author yyao
 *
 */
public class DatasetDescription {
	
	/** REQUIRED. Name of the dataset. */
	@JsonProperty("Name")
	private String name;

	/** REQUIRED. The version of the BIDS standard that was used. */
	@JsonProperty("BIDSVersion")
	private String bidsVersion = "1.1.2-dev";

	/** RECOMMENDED. What license is this dataset distributed under? The use of license name abbreviations is suggested for specifying a license. A list of common licenses with suggested abbreviations can be found in Appendix II. */
	@JsonProperty("License")
	private String license;

	/** OPTIONAL. List of individuals who contributed to the creation/curation of the dataset. */
	@JsonProperty("Authors")
	private List<String> authors;
	
	/** OPTIONAL. Text acknowledging contributions of individuals or institutions beyond those listed in Authors or Funding. */
	@JsonProperty("Acknowledgements")
	private String acknowledgements;
	
	/** OPTIONAL. Text containing instructions on how researchers using this dataset should acknowledge the original authors. This field can also be used to define a publication that should be cited in publications that use the dataset. */
	@JsonProperty("HowToAcknowledge")
	private String howToAcknowledge;
	
	/** OPTIONAL. List of sources of funding (grant numbers) */
	@JsonProperty("Funding")
	private String funding;
	
	/** OPTIONAL. List of references to publication that contain information on the dataset, or links. */
	@JsonProperty("ReferencesAndLinks")
	private String referencesAndLinks;
	
	/** OPTIONAL. The Document Object Identifier of the dataset (not the corresponding paper). */
	@JsonProperty("datasetDOI")
	private String datasetDOI;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the bidsVersion
	 */
	public String getBidsVersion() {
		return bidsVersion;
	}

	/**
	 * @param bidsVersion the bidsVersion to set
	 */
	public void setBidsVersion(String bidsVersion) {
		this.bidsVersion = bidsVersion;
	}

	/**
	 * @return the license
	 */
	public String getLicense() {
		return license;
	}

	/**
	 * @param license the license to set
	 */
	public void setLicense(String license) {
		this.license = license;
	}

	/**
	 * @return the authors
	 */
	public List<String> getAuthors() {
		return authors;
	}

	/**
	 * @param authors the authors to set
	 */
	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}

	/**
	 * @return the acknowledgements
	 */
	public String getAcknowledgements() {
		return acknowledgements;
	}

	/**
	 * @param acknowledgements the acknowledgements to set
	 */
	public void setAcknowledgements(String acknowledgements) {
		this.acknowledgements = acknowledgements;
	}

	/**
	 * @return the howToAcknowledge
	 */
	public String getHowToAcknowledge() {
		return howToAcknowledge;
	}

	/**
	 * @param howToAcknowledge the howToAcknowledge to set
	 */
	public void setHowToAcknowledge(String howToAcknowledge) {
		this.howToAcknowledge = howToAcknowledge;
	}

	/**
	 * @return the funding
	 */
	public String getFunding() {
		return funding;
	}

	/**
	 * @param funding the funding to set
	 */
	public void setFunding(String funding) {
		this.funding = funding;
	}

	/**
	 * @return the referencesAndLinks
	 */
	public String getReferencesAndLinks() {
		return referencesAndLinks;
	}

	/**
	 * @param referencesAndLinks the referencesAndLinks to set
	 */
	public void setReferencesAndLinks(String referencesAndLinks) {
		this.referencesAndLinks = referencesAndLinks;
	}

	/**
	 * @return the datasetDOI
	 */
	public String getDatasetDOI() {
		return datasetDOI;
	}

	/**
	 * @param datasetDOI the datasetDOI to set
	 */
	public void setDatasetDOI(String datasetDOI) {
		this.datasetDOI = datasetDOI;
	}

}
