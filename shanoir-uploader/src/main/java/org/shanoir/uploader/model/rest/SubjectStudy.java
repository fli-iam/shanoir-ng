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

package org.shanoir.uploader.model.rest;

import java.util.List;

/**
 * DTO for subject of a study.
 * 
 * There is a conflict in the DTO mapping. ShUp receives a SubjectDTO
 * from the server in findByIdentifier, but to call updateSubject on
 * the server a Subject type is required. E.g. the difference is, that
 * in the SubjectDTO tags are the subject-study-tags and in the Subject
 * class the list is called subjectStudyTags. This causes problems on the
 * server, when updateSubject is called as the subjectStudyTags are missing.
 * The work-around is: copy when receiving the SubjectDTO directly into the
 * parallel list subjectStudyTags, that the mapping back on the server for
 * the update fits.
 * 
 * @author msimon
 *
 */
public class SubjectStudy {

	private Long id;

	private IdName subject;

	private IdName study;

	private String subjectStudyIdentifier;

	private SubjectType subjectType;

	private boolean physicallyInvolved;

	private List<Tag> tags;

	private List<Tag> subjectStudyTags;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public IdName getSubject() {
		return subject;
	}

	public void setSubject(IdName subject) {
		this.subject = subject;
	}

	public IdName getStudy() {
		return study;
	}

	public void setStudy(IdName study) {
		this.study = study;
	}

	/**
	 * @return the subjectStudyIdentifier
	 */
	public String getSubjectStudyIdentifier() {
		return subjectStudyIdentifier;
	}

	/**
	 * @param subjectStudyIdentifier
	 *            the subjectStudyIdentifier to set
	 */
	public void setSubjectStudyIdentifier(String subjectStudyIdentifier) {
		this.subjectStudyIdentifier = subjectStudyIdentifier;
	}

	/**
	 * @return the subjectType
	 */
	public SubjectType getSubjectType() {
		return subjectType;
	}

	/**
	 * @param subjectType
	 *            the subjectType to set
	 */
	public void setSubjectType(SubjectType subjectType) {
		this.subjectType = subjectType;
	}

	/**
	 * @return the physicallyInvolved
	 */
	public boolean isPhysicallyInvolved() {
		return physicallyInvolved;
	}

	/**
	 * @param physicallyInvolved
	 *            the physicallyInvolved to set
	 */
	public void setPhysicallyInvolved(boolean physicallyInvolved) {
		this.physicallyInvolved = physicallyInvolved;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
		this.subjectStudyTags = tags;
	}

	public List<Tag> getSubjectStudyTags() {
		return subjectStudyTags;
	}

	public void setSubjectStudyTags(List<Tag> subjectStudyTags) {
		this.subjectStudyTags = subjectStudyTags;
	}

}
