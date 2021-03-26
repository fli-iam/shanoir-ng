package org.shanoir.uploader.model.dto;

public class SubjectRemoteResultDTO {
	
	private SubjectDTO subject;
	
	private String result;

	public SubjectRemoteResultDTO(SubjectDTO subject, String result) {
		super();
		this.subject = subject;
		this.result = result;
	}

	public SubjectDTO getSubject() {
		return subject;
	}

	public void setSubject(SubjectDTO subject) {
		this.subject = subject;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
	

}
