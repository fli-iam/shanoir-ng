package org.shanoir.ng.preclinical.subjects.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(
        ignoreUnknown = true
)
public class PreclinicalSubjectDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("animalSubject")
    private AnimalSubjectDto animalSubject;

    @JsonProperty("subject")
    private SubjectDto subject;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AnimalSubjectDto getAnimalSubject() {
        return animalSubject;
    }

    public void setAnimalSubject(AnimalSubjectDto animalSubject) {
        this.animalSubject = animalSubject;
    }

    public SubjectDto getSubject() {
        return subject;
    }

    public void setSubject(SubjectDto subject) {
        this.subject = subject;
    }
}
