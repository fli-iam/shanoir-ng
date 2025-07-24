package org.shanoir.ng.vip.executionTemplate.model;

import jakarta.persistence.Entity;
import org.shanoir.ng.shared.core.model.AbstractEntity;

@Entity
public class PlannedExecution extends AbstractEntity {

    Long acquisitionId;

    Long templateId;

    public PlannedExecution(Long templateId, Long acquisitionId) {
        this.acquisitionId = acquisitionId;
        this.templateId = templateId;
    }

    public PlannedExecution() {}

    public Long getAcquisitionId() {
        return acquisitionId;
    }

    public void setAcquisitionId(Long acquisitionId) {
        this.acquisitionId = acquisitionId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }
}
