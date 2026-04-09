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

import java.util.ArrayList;
import java.util.List;

import org.shanoir.ng.preclinical.references.Reference;

public class AnimalSubjectDto {

    private Long id;

    private Reference specie = null;

    private Reference strain = null;

    private Reference biotype = null;

    private Reference provider = null;

    private Reference stabulation = null;

    private List<SubjectPathologyDto> subjectPathologies = new ArrayList<>();

    private List<SubjectTherapyDto> subjectTherapies = new ArrayList<>();

    private SubjectDto subject;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reference getSpecie() {
        return specie;
    }

    public void setSpecie(Reference specie) {
        this.specie = specie;
    }

    public Reference getStrain() {
        return strain;
    }

    public void setStrain(Reference strain) {
        this.strain = strain;
    }

    public Reference getBiotype() {
        return biotype;
    }

    public void setBiotype(Reference biotype) {
        this.biotype = biotype;
    }

    public Reference getProvider() {
        return provider;
    }

    public void setProvider(Reference provider) {
        this.provider = provider;
    }

    public Reference getStabulation() {
        return stabulation;
    }

    public void setStabulation(Reference stabulation) {
        this.stabulation = stabulation;
    }

    public List<SubjectPathologyDto> getSubjectPathologies() {
        return subjectPathologies;
    }

    public void setSubjectPathologies(List<SubjectPathologyDto> subjectPathologies) {
        this.subjectPathologies = subjectPathologies;
    }

    public List<SubjectTherapyDto> getSubjectTherapies() {
        return subjectTherapies;
    }

    public void setSubjectTherapies(List<SubjectTherapyDto> subjectTherapies) {
        this.subjectTherapies = subjectTherapies;
    }

    public SubjectDto getSubject() {
        return subject;
    }

    public void setSubject(SubjectDto subject) {
        this.subject = subject;
    }
}
