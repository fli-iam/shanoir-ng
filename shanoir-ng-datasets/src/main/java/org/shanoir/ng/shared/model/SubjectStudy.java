package org.shanoir.ng.shared.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "study_id", "subject_id" }, name = "study_subject_idx") })
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubjectStudy {

	@Id
	private Long id;

	/** Study. */
	@ManyToOne
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
    @JoinTable( name = "subject_study_tag",
                joinColumns = @JoinColumn( name = "subject_study_id" ))
	private List<Tag> tags;

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

}
