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

package org.shanoir.uploader.model.rest;

import java.util.List;

import org.shanoir.uploader.ShUpConfig;
/**
 * Light implementation of Study object from ms studies.
 * ShUp has no dependency to ms studies, what is wanted
 * so far, to keep the coupling more lose. ShUp is a tool
 * for imports: dependency to ms import and ms datasets.
 */
public class Study implements Comparable<Study> {

    private static final Integer SC_MANDATORY = Integer.valueOf(1);

    private static final Integer SC_DISABLED = Integer.valueOf(2);

    private Long id;

    private String name;

    private String studyStatus;

    private List<StudyCard> studyCards;

    private List<StudyCenter> studyCenterList;

    private Boolean compatible;

    private Integer studyCardPolicy;

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

    public String getStudyStatus() {
        return studyStatus;
    }

    public void setStudyStatus(String studyStatus) {
        this.studyStatus = studyStatus;
    }

    public List<StudyCard> getStudyCards() {
        return studyCards;
    }

    public void setStudyCards(List<StudyCard> studyCards) {
        this.studyCards = studyCards;
    }

    public List<StudyCenter> getStudyCenterList() {
        return studyCenterList;
    }

    public void setStudyCenterList(List<StudyCenter> studyCenterList) {
        this.studyCenterList = studyCenterList;
    }

    public Boolean getCompatible() {
        return compatible;
    }

    public void setCompatible(Boolean compatible) {
        this.compatible = compatible;
    }

    public Integer getStudyCardPolicy() {
        return studyCardPolicy;
    }

    public void setStudyCardPolicy(Integer studyCardPolicy) {
        this.studyCardPolicy = studyCardPolicy;
    }

    public String toString() {
        if (this.getStudyCards() != null && !this.getStudyCards().isEmpty()) {
            if (compatible) {
                return ShUpConfig.resourceBundle.getString("shanoir.uploader.import.compatible") + " " + this.getName();
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

    public boolean isWithStudyCards() {
        if (SC_MANDATORY.equals(studyCardPolicy)) {
            return true;
        } else {
            return SC_DISABLED.equals(studyCardPolicy);
        }
    }

}
