/**
 * 
 */
package org.shanoir.ng.importer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author yyao
 *
 */
public class InstitutionDicom {
	
	@JsonProperty("institutionName")
	private String institutionName;

	@JsonProperty("institutionAddress")
	private String institutionAddress;
	
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
