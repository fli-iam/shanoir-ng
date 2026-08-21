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

package org.shanoir.ng.subjectstudy.model;

import java.io.Serializable;
import java.util.Objects;

import org.shanoir.ng.tag.model.Tag;

public class SubjectStudyTagPrimaryKey implements Serializable {

    private SubjectStudy subjectStudy;

    private Tag tag;

    public SubjectStudyTagPrimaryKey() { }

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
        return Objects.equals(subjectStudy, primaryKey.subjectStudy)
            && Objects.equals(tag, primaryKey.tag);
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
