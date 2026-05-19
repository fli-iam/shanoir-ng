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

package org.shanoir.ng.dataset.dto;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.shared.core.model.IdName;

import com.fasterxml.jackson.annotation.JsonFormat;


public class DatasetLight {

    private Long id;

    private String name;

    private String type;

    private boolean hasProcessings;

    private IdName study;

    private IdName subject;

    private Long centerId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Europe/Paris")
    private LocalDate creationDate;


    public DatasetLight(
            Long id,
            String name,
            Class<? extends Dataset> type,
            Long studyId, String studyName,
            Long subjectId, String subjectName,
            LocalDate creationDate,
            boolean hasProcessings,
            Long centerId
    ) throws NoSuchMethodException, InstantiationException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        this.hasProcessings = hasProcessings;
        this.id = id;
        this.name = name;
        this.type = type.getDeclaredConstructor().newInstance().getType().name();
        this.study = new IdName(studyId, studyName);
        this.subject = new IdName(subjectId, subjectName);
        this.centerId = centerId;
        this.creationDate = creationDate;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isHasProcessings() {
        return hasProcessings;
    }

    public void setHasProcessings(boolean hasProcessings) {
        this.hasProcessings = hasProcessings;
    }

    public IdName getStudy() {
        return study;
    }

    public void setStudy(IdName study) {
        this.study = study;
    }

    public IdName getSubject() {
        return subject;
    }

    public void setSubject(IdName subject) {
        this.subject = subject;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public Long getCenterId() {
        return centerId;
    }

    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }
}
