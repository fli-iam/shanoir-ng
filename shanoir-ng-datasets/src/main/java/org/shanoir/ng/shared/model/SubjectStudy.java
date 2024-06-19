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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.shanoir.ng.shared.subjectstudy.SubjectType;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.tag.model.Tag;

import java.util.List;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "study_id", "subject_id" }, name = "study_subject_idx") })
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubjectStudy {

	@Id
	private Long id;

	/** Study. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_id")
	@NotNull
	private Study study;

	/** Subject. */
	@ManyToOne
	@JoinColumn(name = "subject_id", updatable = true, insertable = true)
	@NotNull
	private Subject subject;
	
	/** Tags associated to the subject. */
    @ManyToMany
    @JoinTable( name = "subject_study_tag", joinColumns = @JoinColumn( name = "subject_study_id" ))
	private List<Tag> tags;
    
    private Integer qualityTag;

	/** Subject type. */
	private Integer subjectType;

    
	/**
	 * @return the study
	 */
	public Study getStudy() {
		return study;
	}

	/**
	 * @param study
	 *            the study to set
	 */
	public void setStudy(Study study) {
		this.study = study;
	}

	/**
	 * @return the subject
	 */
	public Subject getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(Subject subject) {
		this.subject = subject;
	}

	/**
	 * @return the tags
	 */
	public List<Tag> getTags() {
		return tags;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	public QualityTag getQualityTag() {
        return QualityTag.get(qualityTag);
    }
    
    public void setQualityTag(QualityTag tag) {
        this.qualityTag = tag != null ? tag.getId() : null;
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
}
