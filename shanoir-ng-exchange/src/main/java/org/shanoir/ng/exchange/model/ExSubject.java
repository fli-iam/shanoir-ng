package org.shanoir.ng.exchange.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * If the subject is already existing in Shanoir, the subjectName or subjectId (or both) is set,
 * if not the subjectName == null and the subjectId == null. So we have to create a new subject in sh-ng.
 * 
 * @author mkain
 *
 */
public class ExSubject {
	
	/**
	 * If the subject name is set, an existing subject must be used for data exchange.
	 * In case of an import, the subject with the name must be used.
	 * So e.g. with ShUp I would set this name and this.subject == null.
	 * The subject name is unique within sh-ng.
	 */
	@JsonProperty("subjectName")
	private String subjectName;
	
	/**
	 * If subject id is set, an existing subject must be used for data exchange.
	 * In case of an import, the subject with the name must be used.
	 * So e.g. with ShUp I would set this name and this.subject == null.
	 */
	@JsonProperty("subjectId")
	private Long subjectId;
	
	/**
	 * If the id == null a complete subject object as used within MS Studies
	 * shall be added here. This can be used by an export to write a subject
	 * object into and to transfer it to another Shanoir server. The id is
	 * null, as the new server will generate a new id for this subject during
	 * the import.
	 */
//	@JsonProperty("subject")
//	private Subject subject; // or data are in referenced participants.tsv here? to discuss
	
	/**
	 * We use the same object here as within MS Studies.
	 * The subjectStudy has to be present in case:
	 * 1) We add an existing subject (id=5) into a new study
	 * 2) We export a study and export the study subject code
	 * Not present:
	 * 1) The subject id is set, we use an existing subject and
	 * the subject is already in the study, so it can be null here.
	 */
//	@JsonProperty("subjectStudy")
//	private SubjectStudy subjectStudy;
	
	/**
	 * At least one ExExamination needs to be present to exchange data.
	 */
	@JsonProperty("exExaminations")
	private List<ExExamination> exExaminations;

	public List<ExExamination> getExExaminations() {
		return exExaminations;
	}

	public void setExExaminations(List<ExExamination> exExaminations) {
		this.exExaminations = exExaminations;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

}
