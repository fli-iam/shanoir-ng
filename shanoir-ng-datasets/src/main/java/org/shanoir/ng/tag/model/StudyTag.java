package org.shanoir.ng.tag.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.model.Study;

import java.util.Objects;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudyTag extends IdName {

    @Id
    private Long id;

    private String name;

    private String color;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * @return the study
     */
    public Study getStudy() {
        return study;
    }

    /**
     * @param study the study to set
     */
    public void setStudy(Study study) {
        this.study = study;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudyTag studyTag = (StudyTag) o;
        return Objects.equals(id, studyTag.id) && Objects.equals(name, studyTag.name) && Objects.equals(color, studyTag.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, color);
    }
}
