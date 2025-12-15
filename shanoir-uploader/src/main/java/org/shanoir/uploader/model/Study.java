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

package org.shanoir.uploader.model;

import java.util.Collections;
import java.util.List;

import org.shanoir.uploader.ShUpConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Study implements Comparable<Study> {

    private Long id;

    private String name;

    private List<StudyCard> studyCards;

    @JsonProperty("studyCenterList")
    private List<Center> centers;

    private Boolean compatible;

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

    public List<StudyCard> getStudyCards() {
        return studyCards;
    }

    public void setStudyCards(List<StudyCard> studyCards) {
        this.studyCards = studyCards;
    }

    public List<Center> getCenters() {
        return centers;
    }

    public void setCenters(List<Center> centers) {
        this.centers = centers;
        Collections.sort(this.centers);
    }

    public Boolean getCompatible() {
        return compatible;
    }

    public void setCompatible(Boolean compatible) {
        this.compatible = compatible;
    }

    public String toString() {
        if (this.getStudyCards() != null) {
            if (compatible) {
                return ShUpConfig.resourceBundle.getString("shanoir.uploader.import.compatible") + this.getName();
            } else {
                return this.getName();
            }
        } else {
            return this.getName() + ", 0 study card.";
        }
    }

    public int compareTo(Study o) {
        return Long.compare(this.getId(), o.getId());
    }

}
