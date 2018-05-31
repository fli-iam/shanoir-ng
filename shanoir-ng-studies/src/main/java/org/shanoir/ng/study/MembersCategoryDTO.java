package org.shanoir.ng.study;

import java.util.List;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.studyuser.StudyUserType;

/**
 * DTO of list of members by right type.
 * 
 * @author msimon
 *
 */
public class MembersCategoryDTO {

	private List<IdNameDTO> members;

	private StudyUserType studyUserType;

	/**
	 * Default constructor.
	 */
	public MembersCategoryDTO() {
	}

	/**
	 * Constructor with category and members.
	 * 
	 * @param studyUserType
	 *            category.
	 * @param members
	 *            members.
	 */
	public MembersCategoryDTO(final StudyUserType studyUserType, final List<IdNameDTO> members) {
		this.studyUserType = studyUserType;
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
	 * @return the studyUserType
	 */
	public StudyUserType getStudyUserType() {
		return studyUserType;
	}

	/**
	 * @param studyUserType
	 *            the studyUserType to set
	 */
	public void setStudyUserType(StudyUserType studyUserType) {
		this.studyUserType = studyUserType;
	}

}
