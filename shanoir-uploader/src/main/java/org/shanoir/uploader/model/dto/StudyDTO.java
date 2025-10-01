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

package org.shanoir.uploader.model.dto;

import java.util.List;

public class StudyDTO {

    private Long id;

    private String name;

    private List<StudyCardDTO> studyCards;

    private List<CenterDTO> centers;

    public StudyDTO(Long id, String name, List<StudyCardDTO> studyCards, List<CenterDTO> centers) {
        super();
        this.id = id;
        this.name = name;
        this.studyCards = studyCards;
        this.centers = centers;
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

    public List<StudyCardDTO> getStudyCards() {
        return studyCards;
    }

    public void setStudyCards(List<StudyCardDTO> studyCards) {
        this.studyCards = studyCards;
    }

    public List<CenterDTO> getCenters() {
        return centers;
    }

    public void setCenters(List<CenterDTO> centers) {
        this.centers = centers;
    }

}
