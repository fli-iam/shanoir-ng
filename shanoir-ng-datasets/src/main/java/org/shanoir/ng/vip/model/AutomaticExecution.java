package org.shanoir.ng.vip.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.shanoir.ng.shared.model.Study;

/**
 * This class represents the associated criterias for an automatic execution realized after an import in shanoir.
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutomaticExecution {

    @Id
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "study_id")
    private Study study;

    /*
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "pipeline_parameter")
    private List<PipelineParameter> parameters;
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
