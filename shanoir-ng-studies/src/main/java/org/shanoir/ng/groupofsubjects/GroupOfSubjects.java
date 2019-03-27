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
