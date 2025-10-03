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

package org.shanoir.ng.exchange.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * If the study is already existing in Shanoir, the studyName or studyId (or both) is set,
 * if not the studyName == null and the studyId == null. So we have to create a new study in sh-ng.
 *
 * @author mkain
 *
 */
public class ExStudy {

    /**
     * If the study name is set, an existing study must be used for data exchange.
     * In case of an import, the study with the name must be used.
     * So e.g. with ShUp I would set this name and this.study == null.
     * The study name is unique within sh-ng.
     */
    @JsonProperty("studyName")
    private String studyName;

    /**
     * If study id is set, an existing study must be used for data exchange.
     * In case of an import, the study with the name must be used.
     * So e.g. with ShUp I would set this name and this.study == null.
     */
    @JsonProperty("studyId")
    private Long studyId;

    /**
     * If the id == null a complete study object as used within MS Studies
     * shall be added here. This can be used by an export to write a study
     * object into and to transfer it to another Shanoir server. The id is
     * null, as the new server will generate a new id for this study during
     * the import.
     */
//    @JsonProperty("study")
//    private Study study;

    /**
     * At least one ExStudyCard needs to be present to exchange data.
     * In case of an import, for the moment always the first study card
     * object is used for all subjects and studies(dicom)==examinations.
     */
    @JsonProperty("exStudyCards")
    private List<ExStudyCard> exStudyCards;

    /**
     * At least one ExSubject needs to be present to exchange data.
     */
    @JsonProperty("exSubjects")
    private List<ExSubject> exSubjects;

    public List<ExStudyCard> getExStudyCards() {
        return exStudyCards;
    }

    public List<ExSubject> getExSubjects() {
        return exSubjects;
    }

    public void setExStudyCards(List<ExStudyCard> exStudyCards) {
        this.exStudyCards = exStudyCards;
    }

    public void setExSubjects(List<ExSubject> exSubjects) {
        this.exSubjects = exSubjects;
    }

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public Long getStudyId() {
        return studyId;
    }

    public void setStudyId(Long studyId) {
        this.studyId = studyId;
    }

}
