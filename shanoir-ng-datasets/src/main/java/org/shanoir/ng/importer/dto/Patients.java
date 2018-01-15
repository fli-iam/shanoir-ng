package org.shanoir.ng.importer.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Patients {
	
    @JsonProperty("patients")
    public List<Patient> patients;
	
}
