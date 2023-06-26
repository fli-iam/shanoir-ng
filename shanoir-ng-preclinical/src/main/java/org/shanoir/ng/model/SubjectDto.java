package org.shanoir.ng.model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author atouboul
 * @author mkain
 */
public class SubjectDto {

    @JsonProperty("id")
    private long id;

    @JsonProperty("name")
    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @JsonProperty("sex")
    private String sex;

    @JsonProperty("imagedObjectCategory")
    private Integer imagedObjectCategory;

    @JsonProperty("subjectStudyList")
    private List<SubjectStudyDto> subjectStudyList;

    @JsonProperty("preclinical")
    private boolean preclinical;

    public SubjectDto(Long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
    public Integer getImagedObjectCategory() {
        return imagedObjectCategory;
    }

    /**
     * @param imagedObjectCategory the imagedObjectCategory to set
     */
    public void setImagedObjectCategory(Integer imagedObjectCategory) {
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
}