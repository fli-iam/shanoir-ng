package org.shanoir.ng.model;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.shared.hateoas.HalEntity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

public class AnimalSubjectDto  {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("specie")
    private Reference specie;

    @JsonProperty("strain")
    private Reference strain;

    @JsonProperty("biotype")
    private Reference biotype;

    @JsonProperty("provider")
    private Reference provider;

    @JsonProperty("stabulation")
    private Reference stabulation;

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
}
