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

package org.shanoir.ng.groupofsubjects;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.Unique;
import org.shanoir.ng.subject.model.SubjectGroupOfSubjects;

/**
 * Group of subjects.
 * 
 * @author msimon
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE) 
@GenericGenerator(name = "IdOrGenerate", strategy = "increment")
public class GroupOfSubjects extends HalEntity {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -5939672730246920099L;

	/** Group name. */
	@Unique
	@NotNull
	private String groupName;

	/** Relations between the subjects and the experimental group*. */
	@OneToMany(mappedBy = "groupOfSubjects", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<SubjectGroupOfSubjects> subjectGroupOfSubjectsList;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "groupofsubjects/" + getId());
	}

	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @param groupName
	 *            the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * @return the subjectGroupOfSubjectsList
	 */
	public Set<SubjectGroupOfSubjects> getSubjectGroupOfSubjectsList() {
		return subjectGroupOfSubjectsList;
	}

	/**
	 * @param subjectGroupOfSubjectsList
	 *            the subjectGroupOfSubjectsList to set
	 */
	public void setSubjectGroupOfSubjectsList(Set<SubjectGroupOfSubjects> subjectGroupOfSubjectsList) {
		this.subjectGroupOfSubjectsList = subjectGroupOfSubjectsList;
	}

}
