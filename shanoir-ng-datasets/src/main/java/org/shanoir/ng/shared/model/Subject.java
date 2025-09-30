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
package org.shanoir.ng.shared.model;

import java.util.HashSet;
import java.util.Set;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.shared.subjectstudy.SubjectType;
import org.shanoir.ng.tag.model.Tag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

/**
 * @author yyao
 *
 */
@Entity
@Table(name = "subject")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subject extends IdName {

	@Id
	protected Long id;

	protected String name;

	@ManyToOne
	@JoinColumn(name = "study_id")
	@NotNull
	private Study study;

	@ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
			name = "subject_tag",
			joinColumns = { @JoinColumn(name = "subject_id") },
			inverseJoinColumns = { @JoinColumn(name = "tag_id") }
    )
	private Set<Tag> tags = new HashSet<Tag>();

	private Integer qualityTag;

	private Integer subjectType;

	public Subject() { }

	/**
	 * @param id
	 * @param name
	 */
	public Subject(Long id, String name) {
		this.setId(id);
		this.setName(name);
	}

	public Study getStudy() {
		return study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the subjectType
	 */
	public SubjectType getSubjectType() {
		return SubjectType.getType(subjectType);
	}

	/**
	 * @param subjectType
	 *            the subjectType to set
	 */
	public void setSubjectType(SubjectType subjectType) {
		if (subjectType == null) {
			this.subjectType = null;
		} else {
			this.subjectType = subjectType.getId();
		}
	}

	public QualityTag getQualityTag() {
        return QualityTag.get(qualityTag);
    }

    public void setQualityTag(QualityTag tag) {
        this.qualityTag = tag != null ? tag.getId() : null;
    }

	@JsonProperty("studyId")
	public void setStudyId(Long studyId) {
		if (studyId != null) {
			Study s = new Study();
			s.setId(studyId);
			this.study = s;
		}
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

}
