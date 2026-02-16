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

package org.shanoir.ng.preclinical.subjects.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathology;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapy;
import org.shanoir.ng.shared.core.model.AbstractEntityInterface;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Animal Subject
 */

@Entity
@Table(name = "animal_subject")
@JsonPropertyOrder({ "_links", "id", "specie", "strain", "biotype", "provider", "stabulation" })
public class AnimalSubject implements AbstractEntityInterface {

    @Id
    private Long id;

    @ManyToOne
    private Reference specie = null;

    @ManyToOne
    private Reference strain = null;

    @ManyToOne
    private Reference biotype = null;

    @ManyToOne
    private Reference provider = null;

    @ManyToOne
    private Reference stabulation = null;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "animalSubject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubjectPathology> subjectPathologies = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "animalSubject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubjectTherapy> subjectTherapies = new ArrayList<>();

    /**
     * none
     *
     * @return specie
     **/
    @Schema(name = "none")
    public Reference getSpecie() {
        return specie;
    }

    public void setSpecie(Reference specie) {
        this.specie = specie;
    }

    /**
     * none
     *
     * @return strain
     **/
    @Schema(name = "none")
    public Reference getStrain() {
        return strain;
    }

    public void setStrain(Reference strain) {
        this.strain = strain;
    }

    /**
     * none
     *
     * @return biotype
     **/
    @Schema(name = "none")
    public Reference getBiotype() {
        return biotype;
    }

    public void setBiotype(Reference biotype) {
        this.biotype = biotype;
    }

    /**
     * none
     *
     * @return provider
     **/
    @Schema(name = "none")
    public Reference getProvider() {
        return provider;
    }

    public void setProvider(Reference provider) {
        this.provider = provider;
    }

    /**
     * none
     *
     * @return stabulation
     **/
    @Schema(name = "none")
    public Reference getStabulation() {
        return stabulation;
    }

    public void setStabulation(Reference stabulation) {
        this.stabulation = stabulation;
    }

    public List<SubjectPathology> getSubjectPathologies() {
        return subjectPathologies;
    }

    public void setSubjectPathologies(List<SubjectPathology> subjectPathologies) {
        this.subjectPathologies = subjectPathologies;
        for (SubjectPathology subjectPathology : subjectPathologies) {
            subjectPathology.setAnimalSubject(this);
        }
    }

    public List<SubjectTherapy> getSubjectTherapies() {
        return subjectTherapies;
    }

    public void setSubjectTherapies(List<SubjectTherapy> subjectTherapies) {
        this.subjectTherapies = subjectTherapies;
        for (SubjectTherapy subjectTherapy : subjectTherapies) {
            subjectTherapy.setAnimalSubject(this);
        }
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AnimalSubject subject = (AnimalSubject) o;
        return Objects.equals(this.getId(), subject.getId())
                && Objects.equals(this.id, subject.id)
                && Objects.equals(this.specie, subject.specie)
                && Objects.equals(this.strain, subject.strain) && Objects.equals(this.biotype, subject.biotype)
                && Objects.equals(this.provider, subject.provider)
                && Objects.equals(this.stabulation, subject.stabulation)
                && Objects.equals(this.subjectTherapies, subject.subjectTherapies)
                && Objects.equals(this.subjectPathologies, subject.subjectPathologies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), specie, strain, biotype, provider, stabulation);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AnimalSubject {\n");
        sb.append("    id: ").append(toIndentedString(this.getId())).append("\n");
        sb.append("    specie: ").append(toIndentedString(specie)).append("\n");
        sb.append("    strain: ").append(toIndentedString(strain)).append("\n");
        sb.append("    biotype: ").append(toIndentedString(biotype)).append("\n");
        sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
        sb.append("    stabulation: ").append(toIndentedString(stabulation)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
