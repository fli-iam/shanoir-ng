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

package org.shanoir.ng.importer.dto;

import java.io.Serializable;
import java.util.List;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 *
 */
public class ImportJob implements Serializable {

	private static final long serialVersionUID = 8804929608059674037L;

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
    
    @JsonProperty("frontStudyId")
    private Long frontStudyId;
    
	@JsonProperty("studyCardName")
	private String studyCardName;
	
	// todo: remove this later, when front end uses StudyCards
    @JsonProperty("frontAcquisitionEquipmentId")
    private Long frontAcquisitionEquipmentId;
	
	@JsonProperty("anonymisationProfileToUse")
	private String anonymisationProfileToUse;
    
    @JsonProperty("frontConverterId")
    private Long frontConverterId;
    
    @JsonProperty("archive")
    private String archive;

	@JsonProperty("subjectName")
	private String subjectName;

	@JsonProperty("studyName")
	private String studyName;
    
	public String getArchive() {
		return archive;
	}

	public void setArchive(final String archive) {
		this.archive = archive;
	}
	
	public boolean isFromDicomZip() {
		return fromDicomZip;
	}

	public void setFromDicomZip(final boolean fromDicomZip) {
		this.fromDicomZip = fromDicomZip;
	}

	public boolean isFromShanoirUploader() {
		return fromShanoirUploader;
	}

	public void setFromShanoirUploader(final boolean fromShanoirUploader) {
		this.fromShanoirUploader = fromShanoirUploader;
	}

	public boolean isFromPacs() {
		return fromPacs;
	}

	public void setFromPacs(final boolean fromPacs) {
		this.fromPacs = fromPacs<<<<<<< HEAD

	}

	public List<Patient> getPatients() {
		return patients;
	}

	public void setPatients(final List<Patient> patients) {
		this.patients = patients;
	}

	public Long getExaminationId() {
		return examinationId;
	}

	public void setExaminationId(final Long examinationId) {
		this.examinationId = examinationId;
	}

	public Long getFrontStudyId() {
		return frontStudyId;
	}

	public void setFrontStudyId(final Long frontStudyId) {
		this.frontStudyId = frontStudyId;
	}

	public String getStudyCardName() {
		return studyCardName;
	}

	public void setStudyCardName(String studyCardName) {
		this.studyCardName = studyCardName;
	}

	public Long getFrontAcquisitionEquipmentId() {
		return frontAcquisitionEquipmentId;
	}

	public void setFrontAcquisitionEquipmentId(final Long frontAcquisitionEquipmentId) {
		this.frontAcquisitionEquipmentId = frontAcquisitionEquipmentId;
	}

	public Long getFrontConverterId() {
		return frontConverterId;
	}

	public void setFrontConverterId(Long frontConverterId) {
		this.frontConverterId = frontConverterId;
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

	public void setWorkFolder(final String workFolder) {
		this.workFolder = workFolder;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}
}
