package org.shanoir.ng.preclinical.subjects.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.shanoir.ng.preclinical.references.Reference;

import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
