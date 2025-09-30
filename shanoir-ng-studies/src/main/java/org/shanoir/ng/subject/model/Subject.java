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

package org.shanoir.ng.subject.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.shared.subjectstudy.SubjectType;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.tag.model.Tag;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(indexes = @Index(name = "subject_name_study_id_idx", columnList = "name, study_id", unique = true))
@JsonPropertyOrder({ "_links", "id", "name", "identifier", "sex", "birthDate", "imagedObjectCategory",
    "preclinical", "pseudonymusHashValues", "subjectStudyList", "languageHemisphericDominance", "manualHemisphericDominance",
    "userPersonalCommentList" })
@SqlResultSetMapping(name = "subjectNameResult", classes = { @ConstructorResult(targetClass = IdName.class, columns = {
        @ColumnResult(name = "id", type = Long.class), @ColumnResult(name = "name") }) })
public class Subject extends HalEntity {

    private static final long serialVersionUID = 6844259659282875507L;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotNull
    private String name;

    private Integer sex;

    @ManyToOne
    @JoinColumn(name = "study_id")
    @NotNull
    private Study study;

    /** Relations beetween the subjects and the studies. */
    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubjectStudy> subjectStudyList;

    private String identifier;

    @OneToOne(cascade = CascadeType.ALL)
    private PseudonymusHashValues pseudonymusHashValues;

    /** Language Hemispheric dominance. */
    private Integer languageHemisphericDominance;

    /** Manual Hemispheric dominance. */
    private Integer manualHemisphericDominance;

    /** Flag to set the subject as pre-clinical subject */
    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean preclinical;

    /**
     * The category of the subject (phantom, human alive, human cadaver, etc.).
     */
    private Integer imagedObjectCategory;

    /** Personal Comments on this subject. */
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private List<UserPersonalCommentSubject> userPersonalCommentList = new ArrayList<>(0);

    private String studyIdentifier;

    @Column(name = "physically_involved", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private boolean physicallyInvolved;

    private Integer subjectType;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name = "subject_tag",
            joinColumns = { @JoinColumn(name = "subject_id") },
            inverseJoinColumns = { @JoinColumn(name = "tag_id") }
    )
    private Set<Tag> tags = new HashSet<Tag>();

    private Integer qualityTag;

    /**
     * Init HATEOAS links
     */
    @PostLoad
    public void initLinks() {
        this.addLink(Links.REL_SELF, "subject/" + getId());
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SubjectStudy> getSubjectStudyList() {
        return subjectStudyList;
    }

    public void setSubjectStudyList(List<SubjectStudy> subjectStudyList) {
        this.subjectStudyList = subjectStudyList;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public PseudonymusHashValues getPseudonymusHashValues() {
        return pseudonymusHashValues;
    }

    public void setPseudonymusHashValues(PseudonymusHashValues pseudonymusHashValues) {
        this.pseudonymusHashValues = pseudonymusHashValues;
    }

    public List<UserPersonalCommentSubject> getUserPersonalCommentList() {
        return userPersonalCommentList;
    }

    public void setUserPersonalCommentList(List<UserPersonalCommentSubject> userPersonalCommentList) {
        this.userPersonalCommentList = userPersonalCommentList;
    }

    public Sex getSex() {
        return Sex.getSex(sex);
    }

    public void setSex(Sex sex) {
        if (sex == null) {
            this.sex = null;
        } else {
            this.sex = sex.getId();
        }
    }

    public HemisphericDominance getLanguageHemisphericDominance() {
        return HemisphericDominance.getDominance(languageHemisphericDominance);
    }

    public void setLanguageHemisphericDominance(HemisphericDominance languageHemisphericDominance) {
        if (languageHemisphericDominance == null) {
            this.languageHemisphericDominance = null;
        } else {
            this.languageHemisphericDominance = languageHemisphericDominance.getId();
        }
    }

    public HemisphericDominance getManualHemisphericDominance() {
        return HemisphericDominance.getDominance(manualHemisphericDominance);
    }

    public void setManualHemisphericDominance(HemisphericDominance manualHemisphericDominance) {
        if (manualHemisphericDominance == null) {
            this.manualHemisphericDominance = null;
        } else {
            this.manualHemisphericDominance = manualHemisphericDominance.getId();
        }
    }

    public ImagedObjectCategory getImagedObjectCategory() {
        return ImagedObjectCategory.getCategory(imagedObjectCategory);
    }

    public void setImagedObjectCategory(ImagedObjectCategory imagedObjectCategory) {
        if (imagedObjectCategory == null) {
            this.imagedObjectCategory = null;
        } else {
            this.imagedObjectCategory = imagedObjectCategory.getId();
        }
    }

    public boolean isPreclinical() {
        return preclinical;
    }

    public void setPreclinical(boolean preclinical) {
        this.preclinical = preclinical;
    }

    public String getStudyIdentifier() {
        return studyIdentifier;
    }

    public void setStudyIdentifier(String studyIdentifier) {
        this.studyIdentifier = studyIdentifier;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public QualityTag getQualityTag() {
        return QualityTag.get(qualityTag);
    }

    public void setQualityTag(QualityTag tag) {
        this.qualityTag = tag != null ? tag.getId() : null;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public boolean isPhysicallyInvolved() {
        return physicallyInvolved;
    }

    public void setPhysicallyInvolved(boolean physicallyInvolved) {
        this.physicallyInvolved = physicallyInvolved;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public SubjectType getSubjectType() {
        return SubjectType.getType(subjectType);
    }

    public void setSubjectType(SubjectType subjectType) {
        if (subjectType == null) {
            this.subjectType = null;
        } else {
            this.subjectType = subjectType.getId();
        }
    }

}
