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

package org.shanoir.ng.subjectstudy.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.shanoir.ng.tag.model.Tag;

/**
 * Relation between the subjects and the studies.
 * 
 * @author msimon
 *
 */
@Entity
@Table
@IdClass(SubjectStudyTagPk.class)
public class SubjectStudyTag {

	@Id
	@ManyToOne
	@JoinColumn(name = "tag_id", insertable = false, updatable = false, nullable = false)
	private Tag tag;
	
	@Id
	@ManyToOne
	@JoinColumn(name = "subject_study_id", insertable = false, updatable = false, nullable = false)
	private SubjectStudy subjectStudy;


	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

	public SubjectStudy getSubjectStudy() {
		return subjectStudy;
	}

	public void setSubjectStudy(SubjectStudy subjectStudy) {
		this.subjectStudy = subjectStudy;
	}

}
