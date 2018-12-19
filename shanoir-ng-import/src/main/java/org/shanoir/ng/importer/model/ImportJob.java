package org.shanoir.ng.importer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 * @author mkain
 */
public class ImportJob {

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
    
    @JsonProperty("frontStudyCardId")
    private Long frontStudyCardId;
    
    @JsonProperty("frontConverterId")
    private Long frontConverterId;
    
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

	public Long getFrontConverterId() {
		return frontConverterId;
	}

	public void setFrontConverterId(Long frontConverterId) {
		this.frontConverterId = frontConverterId;
	}

    public String getWorkFolder() {
		return workFolder;
	}

	public void setWorkFolder(String workFolder) {
		this.workFolder = workFolder;
	}
	
}

