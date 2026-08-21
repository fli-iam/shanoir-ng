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
package org.shanoir.ng.preclinical.subjects.dto;

import java.util.Date;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModel;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.shared.hateoas.HalEntity;


public class SubjectPathologyDto extends HalEntity {

    private AnimalSubjectDto animalSubject = null;

    private PathologyModel pathologyModel = null;

    private Pathology pathology = null;

    private Reference location = null;

    private Date startDate = null;

    private Date endDate = null;

    public AnimalSubjectDto getAnimalSubject() {
        return animalSubject;
    }

    public void setAnimalSubject(AnimalSubjectDto animalSubject) {
        this.animalSubject = animalSubject;
    }

    public PathologyModel getPathologyModel() {
        return pathologyModel;
    }

    public void setPathologyModel(PathologyModel pathologyModel) {
        this.pathologyModel = pathologyModel;
    }

    public Pathology getPathology() {
        return pathology;
    }

    public void setPathology(Pathology pathology) {
        this.pathology = pathology;
    }

    public Reference getLocation() {
        return location;
    }

    public void setLocation(Reference location) {
        this.location = location;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
