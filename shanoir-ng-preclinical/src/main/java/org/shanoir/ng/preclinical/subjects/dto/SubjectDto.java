package org.shanoir.ng.preclinical.subjects.dto;

import java.time.LocalDate;
import java.util.List;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.dateTime.LocalDateSerializer;
import org.shanoir.ng.shared.subjectstudy.SubjectType;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubjectDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    private String identifier;

    @JsonProperty("birthDate")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @JsonProperty("sex")
    private String sex;

    @JsonProperty("imagedObjectCategory")
    private ImagedObjectCategory imagedObjectCategory;

    @JsonProperty("subjectStudyList")
    private List<SubjectStudyDto> subjectStudyList;

    @JsonProperty("preclinical")
    private boolean preclinical;

    private IdName study;

    private boolean physicallyInvolved;

    private SubjectType subjectType;

    private List<TagDto> tags;

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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * @return the sex
     */
    public String getSex() {
        return sex;
    }

    /**
     * @param sex the sex to set
     */
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     * @return the imagedObjectCategory
     */
    public ImagedObjectCategory getImagedObjectCategory() {
        return imagedObjectCategory;
    }

    /**
     * @param imagedObjectCategory the imagedObjectCategory to set
     */
    public void setImagedObjectCategory(ImagedObjectCategory imagedObjectCategory) {
        this.imagedObjectCategory = imagedObjectCategory;
    }

    public List<SubjectStudyDto> getSubjectStudyList() {
        return subjectStudyList;
    }

    public void setSubjectStudyList(List<SubjectStudyDto> subjectStudyList) {
        this.subjectStudyList = subjectStudyList;
    }

    public boolean isPreclinical() {
        return preclinical;
    }

    public void setPreclinical(boolean preclinical) {
        this.preclinical = preclinical;
    }

    public IdName getStudy() {
        return study;
    }

    public void setStudy(IdName study) {
        this.study = study;
    }

    public boolean isPhysicallyInvolved() {
        return physicallyInvolved;
    }

    public void setPhysicallyInvolved(boolean physicallyInvolved) {
        this.physicallyInvolved = physicallyInvolved;
    }

    public SubjectType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(SubjectType subjectType) {
        this.subjectType = subjectType;
    }

    public List<TagDto> getTags() {
        return tags;
    }

    public void setTags(List<TagDto> tags) {
        this.tags = tags;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

}
