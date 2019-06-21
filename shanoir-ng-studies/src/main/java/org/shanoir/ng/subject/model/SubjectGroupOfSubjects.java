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

<<<<<<< HEAD:shanoir-ng-studies/src/main/java/org/shanoir/ng/subject/model/SubjectGroupOfSubjects.java
package org.shanoir.ng.subject.model;
=======
package org.shanoir.ng.subject;
>>>>>>> upstream/develop:shanoir-ng-studies/src/main/java/org/shanoir/ng/subject/SubjectGroupOfSubjects.java

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
