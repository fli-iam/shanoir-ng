package org.shanoir.uploader.dicom;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "id", "name" })
public class StudyCard {

    private Long id;

    private String name;

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

    public String toString() {
        return name;
    }

}