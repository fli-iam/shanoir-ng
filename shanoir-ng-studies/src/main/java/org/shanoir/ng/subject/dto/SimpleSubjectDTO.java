/**
 * 
 */
package org.shanoir.ng.subject.dto;

import org.shanoir.ng.subjectstudy.SubjectStudyDTO;

/**
 * Simple DTO for Subject.
 * This class is used as response for findSubjectsByStudyId request.
 * @author yyao
 *
 */
public class SimpleSubjectDTO {
	
	private Long id;
    
    private String name;
    
    private String identifier;
    
    private SubjectStudyDTO subjectStudy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public SubjectStudyDTO getSubjectStudy() {
		return subjectStudy;
	}

	public void setSubjectStudy(SubjectStudyDTO subjectStudy) {
		this.subjectStudy = subjectStudy;
	}

}
