package org.shanoir.ng.study.dto;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.study.model.security.StudyUserRight;

/**
 * DTO of list of members by right type.
 * 
 * @author msimon
 *
 */
public class MembersCategoryDTO {

	private List<IdNameDTO> members;

	private StudyUserRight studyUserRight;

	/**
	 * Default constructor.
	 */
	public MembersCategoryDTO() {
	}

	/**
	 * Constructor with category and members.
	 * 
	 * @param studyUserRight
	 *            category.
	 * @param members
	 *            members.
	 */
	public MembersCategoryDTO(final StudyUserRight studyUserRight, final List<IdNameDTO> members) {
		this.studyUserRight = studyUserRight;
		this.members = members;
	}

	/**
	 * @return the members
	 */
	public List<IdNameDTO> getMembers() {
		return members;
	}

	/**
	 * @param members
	 *            the members to set
	 */
	public void setMembers(List<IdNameDTO> members) {
		this.members = members;
	}

	/**
	 * @return the studyUserRight
	 */
	public StudyUserRight getStudyUserRight() {
		return studyUserRight;
	}

	/**
	 * @param studyUserRight
	 *            the studyUserRight to set
	 */
	public void setStudyUserRight(StudyUserRight studyUserRight) {
		this.studyUserRight = studyUserRight;
	}

}
