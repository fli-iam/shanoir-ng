package org.shanoir.ng.tag.model;

import org.shanoir.ng.dicom.web.dto.StudyDTO;

public class StudyTagDTO {

    private Long id;

    private String name;

    private String color;

    private StudyDTO study;

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public StudyDTO getStudy() {
        return study;
    }

    public void setStudy(StudyDTO study) {
        this.study = study;
    }

}
