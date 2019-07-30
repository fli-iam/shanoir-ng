package org.shanoir.uploader.model.dto.rest.importmodel;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Patients {
	
    @JsonProperty("patients")
    private List<Patient> patients;

	public List<Patient> getPatients() {
		return patients;
	}

	public void setPatients(List<Patient> patients) {
		this.patients = patients;
	}
    
    
	
}
