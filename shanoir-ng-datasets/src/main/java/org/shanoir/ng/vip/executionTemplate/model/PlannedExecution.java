/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.vip.executionTemplate.model;

import jakarta.persistence.Entity;
import org.shanoir.ng.shared.core.model.AbstractEntity;

@Entity
public class PlannedExecution extends AbstractEntity {

    private Long acquisitionId;
    private Long templateId;

    public PlannedExecution(Long templateId, Long acquisitionId) {
        this.acquisitionId = acquisitionId;
        this.templateId = templateId;
    }

    public PlannedExecution() { }

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
