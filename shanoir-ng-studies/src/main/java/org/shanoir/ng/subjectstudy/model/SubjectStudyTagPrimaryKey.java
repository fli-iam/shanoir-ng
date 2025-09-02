package org.shanoir.ng.subjectstudy.model;

import java.io.Serializable;
import java.util.Objects;

import org.shanoir.ng.tag.model.Tag;

public class SubjectStudyTagPrimaryKey implements Serializable {
	
	private SubjectStudy subjectStudy;

	private Tag tag;

    public SubjectStudyTagPrimaryKey() {} 

    public SubjectStudyTagPrimaryKey(SubjectStudy subjectStudy, Tag tag) {
		super();
		this.subjectStudy = subjectStudy;
		this.tag = tag;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SubjectStudyTagPrimaryKey primaryKey = (SubjectStudyTagPrimaryKey) o;
		return Objects.equals(subjectStudy, primaryKey.subjectStudy) &&
			Objects.equals(tag, primaryKey.tag);
	}

	@Override
	public int hashCode() {
		return Objects.hash(subjectStudy, tag);
	}

	public SubjectStudy getSubjectStudy() {
		return subjectStudy;
	}

	public void setSubjectStudy(SubjectStudy subjectStudy) {
		this.subjectStudy = subjectStudy;
	}

	public Tag getTag() {
		return tag;
	}

	public void setTag(Tag tag) {
		this.tag = tag;
	}

}