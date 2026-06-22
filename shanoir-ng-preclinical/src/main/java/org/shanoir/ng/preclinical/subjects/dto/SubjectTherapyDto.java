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

import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.therapies.Therapy;
import org.shanoir.ng.shared.hateoas.HalEntity;


public class SubjectTherapyDto extends HalEntity {

    private AnimalSubjectDto animalSubject = null;

    private Therapy therapy = null;

    private Date startDate = null;

    private Date endDate = null;

    private Double dose;

    private Reference doseUnit = null;

    private String frequency;

    private String molecule;

    public AnimalSubjectDto getAnimalSubject() {
        return animalSubject;
    }

    public void setAnimalSubject(AnimalSubjectDto animalSubject) {
        this.animalSubject = animalSubject;
    }

    public Therapy getTherapy() {
        return therapy;
    }

    public void setTherapy(Therapy therapy) {
        this.therapy = therapy;
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

    public Double getDose() {
        return dose;
    }

    public void setDose(Double dose) {
        this.dose = dose;
    }

    public Reference getDoseUnit() {
        return doseUnit;
    }

    public void setDoseUnit(Reference doseUnit) {
        this.doseUnit = doseUnit;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getMolecule() {
        return molecule;
    }

    public void setMolecule(String molecule) {
        this.molecule = molecule;
    }


}
