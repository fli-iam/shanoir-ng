package org.shanoir.ng.importer.dto;

import java.util.List;

import org.shanoir.ng.importer.dto.Subject;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 *
 */
public class ImportJob {

    @JsonProperty("subjects")
    private List<Subject> subjects;

    @JsonProperty("fromDicomZip")
    private boolean fromDicomZip;

    @JsonProperty("fromShanoirUploader")
    private boolean fromShanoirUploader;

    @JsonProperty("fromPacs")
    private boolean fromPacs;

    @JsonProperty("patients")
    private List<Patient> patients;
    
    @JsonProperty("examinationId")
    private Long examinationId;
    
    @JsonProperty("frontStudyId")
    private Long frontStudyId;

    @JsonProperty("frontStudyCardId")
    private Long frontStudyCardId;
    
	public List<Subject> getSubjects() {
		return subjects;
	}

	public void setSubjects(List<Subject> subjects) {
		this.subjects = subjects;
	}

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

	public Long getFrontStudyId() {
		return frontStudyId;
	}

	public void setFrontStudyId(Long frontStudyId) {
		this.frontStudyId = frontStudyId;
	}

	public Long getFrontStudyCardId() {
		return frontStudyCardId;
	}

	public void setFrontStudyCardId(Long frontStudyCardId) {
		this.frontStudyCardId = frontStudyCardId;
	}

}
