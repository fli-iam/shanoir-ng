package org.shanoir.ng.subject.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.groupofsubjects.GroupOfSubjects;
import org.shanoir.ng.shared.core.model.AbstractEntity;

/**
 * Relation between the subjects and the groups of subjects.
 * 
 * @author msimon
 *
 */
@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class SubjectGroupOfSubjects extends AbstractEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = 6524573549404145633L;

	/** Subject. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "subject_id")
	private Subject subject;

	/** Experimental Group. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "group_of_subjects_id")
	private GroupOfSubjects groupOfSubjects;

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
	 * @return the groupOfSubjects
	 */
	public GroupOfSubjects getGroupOfSubjects() {
		return groupOfSubjects;
	}

	/**
	 * @param groupOfSubjects
	 *            the groupOfSubjects to set
	 */
	public void setGroupOfSubjects(GroupOfSubjects groupOfSubjects) {
		this.groupOfSubjects = groupOfSubjects;
	}

}
