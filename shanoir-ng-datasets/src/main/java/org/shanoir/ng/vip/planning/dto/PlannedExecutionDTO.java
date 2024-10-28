package org.shanoir.ng.vip.planning.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.shanoir.ng.shared.hateoas.HalEntity;

/**
 * This class represents the associated criterias for an automatic execution realized after an import in shanoir.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlannedExecutionDTO extends HalEntity {

    private String name;

    private long study;

    private String execution;

    public long getStudy() { return study; }

    public void setStudy(long study) { this.study = study; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExecution() {
        return execution;
    }

    public void setExecution(String execution) {
        this.execution = execution;
    }

}
