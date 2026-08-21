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

package org.shanoir.ng.shared.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Simpler replication of study_center in MS Studies.
 * Updated by MS Studies via RabbitMQ. Used by Dicom-
 * ImporterService to avoid a RabbitMQ call to MS Studies
 * per DICOM image imported. Lookup in local database of
 * MS Datasets, instead of sending thousands of messages
 * for one single DICOM study. We need to know, if the
 * center for which we currently import is already in the
 * study, we are importing in.
 *
 */
@Entity
@Table(name = "study_center")
public class StudyCenter {

    @Id
    private Long id;

    /** Center. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private Center center;

    /** The study. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Center getCenter() {
        return center;
    }

    public void setCenter(Center center) {
        this.center = center;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

}
