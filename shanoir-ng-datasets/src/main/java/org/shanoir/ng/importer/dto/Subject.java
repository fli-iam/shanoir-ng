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

package org.shanoir.ng.importer.dto;

import java.time.LocalDate;

import org.shanoir.ng.shared.core.model.IdName;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subject {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("study")
    private IdName study;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /** Studies MS stores sex as integer id ({@link org.shanoir.ng.subject.model.Sex}). */
    @JsonProperty("sex")
    private Integer sex;

    /** Enum name for studies MS Jackson binding (integer ids are misread as ordinals). */
    @JsonProperty("imagedObjectCategory")
    private String imagedObjectCategory;

    @JsonProperty("subjectType")
    private String subjectType;

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

    public IdName getStudy() {
        return study;
    }

    public void setStudy(IdName study) {
        this.study = study;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getImagedObjectCategory() {
        return imagedObjectCategory;
    }

    public void setImagedObjectCategory(String imagedObjectCategory) {
        this.imagedObjectCategory = imagedObjectCategory;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

}
