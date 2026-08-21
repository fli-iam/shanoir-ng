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

package org.shanoir.ng.tag.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subjectstudy.model.SubjectStudyTag;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class Tag extends HalEntity {

    private String name;

    private String color;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "study_id")
    private Study study;

    // to be removed later
    @JsonIgnore
    @OneToMany(mappedBy = "tag", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<SubjectStudyTag> subjectStudyTags;

    @JsonIgnore
    @ManyToMany(mappedBy = "tags")
    private Set<Subject> subjects = new HashSet<>();

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * @return the study
     */
    public Study getStudy() {
        return study;
    }

    /**
     * @param study the study to set
     */
    public void setStudy(Study study) {
        this.study = study;
    }

    public List<SubjectStudyTag> getSubjectStudyTags() {
        return subjectStudyTags;
    }

    public void setSubjectStudyTags(List<SubjectStudyTag> subjectStudyTags) {
        if (this.subjectStudyTags != null) {
            this.subjectStudyTags.clear();
            if (subjectStudyTags != null) {
                this.subjectStudyTags.addAll(subjectStudyTags);
            }
        } else {
            this.subjectStudyTags = subjectStudyTags;
        }
    }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(Set<Subject> subjects) {
        this.subjects = subjects;
    }

}
