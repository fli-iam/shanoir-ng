package org.shanoir.ng.subjectstudy.model;

import java.io.Serializable;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.shanoir.ng.tag.model.SubjectTag;


public class SubjectStudyTagPk implements Serializable {
	
	private static final long serialVersionUID = -7148798110941644158L;
	
	@ManyToOne
	@JoinColumn(name = "tag_id", insertable = false, updatable = false, nullable = false)
	private SubjectTag subjectTag;
	
	@ManyToOne
	@JoinColumn(name = "subject_study_id", insertable = false, updatable = false, nullable = false)
	private SubjectStudy subjectStudy;

    public SubjectStudyTagPk() {} 

    public SubjectStudyTagPk(SubjectTag subjectTag, SubjectStudy subjectStudy) {
		super();
		this.subjectTag = subjectTag;
		this.subjectStudy = subjectStudy;
	}


	@Override
    public boolean equals(Object obj) {
    	return obj != null 
    			&& obj instanceof SubjectStudyTagPk 
    			&& this.getSubjectStudy() != null
    			&& this.getTag() != null
    			&& ((SubjectStudyTagPk) obj).getTag() != null
				&& ((SubjectStudyTagPk) obj).getTag().getId() != null
    			&& ((SubjectStudyTagPk) obj).getSubjectStudy() != null
				&& ((SubjectStudyTagPk) obj).getSubjectStudy().getId() != null
    			&& this.getTag().getId().equals(((SubjectStudyTagPk) obj).getTag().getId())
    			&& this.getSubjectStudy().getId().equals(((SubjectStudyTagPk) obj).getSubjectStudy().getId());
    }
    
    @Override
    public int hashCode() {
    	return (int) getTag().hashCode() * (getSubjectStudy() != null ? getSubjectStudy().hashCode() : null);
    }

	public SubjectTag getTag() {
		return subjectTag;
	}

	public void setTag(SubjectTag subjectTag) {
		this.subjectTag = subjectTag;
	}

	public SubjectStudy getSubjectStudy() {
		return subjectStudy;
	}

	public void setSubjectStudy(SubjectStudy subjectStudy) {
		this.subjectStudy = subjectStudy;
	}


}