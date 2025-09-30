package org.shanoir.ng.subject.model;

import java.io.Serializable;
import java.util.Objects;

import org.shanoir.ng.tag.model.Tag;

public class SubjectTagPrimaryKey implements Serializable {

    private Subject subject;

    private Tag tag;

    public SubjectTagPrimaryKey() { }

    public SubjectTagPrimaryKey(Subject subject, Tag tag) {
        super();
        this.subject = subject;
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
        SubjectTagPrimaryKey primaryKey = (SubjectTagPrimaryKey) o;
        return Objects.equals(subject, primaryKey.subject)
            && Objects.equals(tag, primaryKey.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, tag);
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

}
