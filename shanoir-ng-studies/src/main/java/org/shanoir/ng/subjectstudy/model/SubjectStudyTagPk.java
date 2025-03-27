package org.shanoir.ng.subjectstudy.model;

import java.io.Serializable;

import org.shanoir.ng.tag.model.Tag;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


public class SubjectStudyTagPk implements Serializable {

    private static final long serialVersionUID = -7148798110941644158L;

    @ManyToOne
    @JoinColumn(name = "tag_id", insertable = false, updatable = false, nullable = false)
    private Tag tag;

    @ManyToOne
    @JoinColumn(name = "subject_study_id", insertable = false, updatable = false, nullable = false)
    private SubjectStudy subjectStudy;

    public SubjectStudyTagPk() { }

    public SubjectStudyTagPk(Tag tag, SubjectStudy subjectStudy) {
        super();
        this.tag = tag;
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