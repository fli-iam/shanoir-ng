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

package org.shanoir.uploader.model.rest.importer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author mkain
 */
public class ImportJob {
	
	public static final String IMPORT_JOB_JSON = "import-job.json";

	@JsonProperty("fromDicomZip")
    private boolean fromDicomZip;
	
    @JsonProperty("fromShanoirUploader")
    private boolean fromShanoirUploader;

    @JsonProperty("fromPacs")
    private boolean fromPacs;
    
	@JsonProperty("workFolder")
	private String workFolder;

	@JsonProperty("patients")
    private List<Patient> patients;
    
    @JsonProperty("examinationId")
    private Long examinationId;
    
    @JsonProperty("studyId")
    private Long studyId;
    
    @JsonProperty("studyName")
    private String studyName;
    
    @JsonProperty("studyCardId")
    private Long studyCardId;
    
	@JsonProperty("studyCardName")
	private String studyCardName;
	
	@JsonProperty("subjectName")
	private String subjectName;
	
    @JsonProperty("converterId")
    private Long converterId;
    
    @JsonProperty("acquisitionEquipmentId")
    private Long acquisitionEquipmentId;
	
	@JsonProperty("anonymisationProfileToUse")
	private String anonymisationProfileToUse;
	
    public boolean isFromDicomZip() {
		return fromDicomZip;
	}

	public void setFromDicomZip(boolean fromDicomZip) {
		this.fromDicomZip = fromDicomZip;
	}

	public boolean isFromShanoirUploader() {
		return fromShanoirUploader;
	}

	public void setFromShanoirUploader(boolean fromShanoirUploader) {
		this.fromShanoirUploader = fromShanoirUploader;
	}

	public boolean isFromPacs() {
		return fromPacs;
	}

	public void setFromPacs(boolean fromPacs) {
		this.fromPacs = fromPacs;
	}

	public List<Patient> getPatients() {
		return patients;
	}

	public void setPatients(List<Patient> patients) {
		this.patients = patients;
	}

	public Long getExaminationId() {
		return examinationId;
	}

	public void setExaminationId(Long examinationId) {
		this.examinationId = examinationId;
	}

	public String getStudyCardName() {
		return studyCardName;
	}

	public void setStudyCardName(String studyCardName) {
		this.studyCardName = studyCardName;
	}

    public String getAnonymisationProfileToUse() {
		return anonymisationProfileToUse;
	}

	public void setAnonymisationProfileToUse(String anonymisationProfileToUse) {
		this.anonymisationProfileToUse = anonymisationProfileToUse;
	}

	public String getWorkFolder() {
		return workFolder;
	}

	public void setWorkFolder(String workFolder) {
		this.workFolder = workFolder;
	}

	public Long getStudyId() {
		return studyId;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public Long getStudyCardId() {
		return studyCardId;
	}

	public Long getConverterId() {
		return converterId;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	public void setStudyCardId(Long studyCardId) {
		this.studyCardId = studyCardId;
	}

	public void setConverterId(Long converterId) {
		this.converterId = converterId;
	}

	public Long getAcquisitionEquipmentId() {
		return acquisitionEquipmentId;
	}

	public void setAcquisitionEquipmentId(Long acquisitionEquipmentId) {
		this.acquisitionEquipmentId = acquisitionEquipmentId;
	}
	
}