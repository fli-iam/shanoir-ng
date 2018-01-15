package org.shanoir.ng.importer.dto;

import java.util.List;
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
    private Patients patients;
    
    @JsonProperty("examinationId")
    private Long examinationId;

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

	public Patients getPatients() {
		return patients;
	}

	public void setPatients(Patients patients) {
		this.patients = patients;
	}

	public Long getExaminationId() {
		return examinationId;
	}

	public void setExaminationId(Long examinationId) {
		this.examinationId = examinationId;
	}

}
