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

package org.shanoir.ng.importer.model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 * @author mkain
 */
public class Subject {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("identifier")
    private String identifier;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @JsonProperty("sex")
    private String sex;

    @JsonProperty("imagedObjectCategory")
    private Integer imagedObjectCategory;

    @JsonProperty("subjectStudyList")
    private List<SubjectStudy> subjectStudyList;

    private PseudonymusHashValues pseudonymusHashValues;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * @return the sex
     */
    public String getSex() {
        return sex;
    }

    /**
     * @param sex the sex to set
     */
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     * @return the imagedObjectCategory
     */
    public Integer getImagedObjectCategory() {
        return imagedObjectCategory;
    }

    /**
     * @param imagedObjectCategory the imagedObjectCategory to set
     */
    public void setImagedObjectCategory(Integer imagedObjectCategory) {
        this.imagedObjectCategory = imagedObjectCategory;
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

}