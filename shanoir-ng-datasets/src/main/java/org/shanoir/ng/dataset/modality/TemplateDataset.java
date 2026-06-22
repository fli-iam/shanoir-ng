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

package org.shanoir.ng.dataset.modality;

import jakarta.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetType;

/**
 * Template dataset.
 *
 * @author msimon
 *
 */
@Entity
public class TemplateDataset extends Dataset {

    /**
     * UID
     */
    private static final long serialVersionUID = -3399415257911069266L;

    public TemplateDataset() {

    }

    public TemplateDataset(Dataset other) {
        super(other);
        if (((TemplateDataset) other).getTemplateDatasetNature() != null) {
            this.templateDatasetNature = ((TemplateDataset) other).getTemplateDatasetNature().getId();
        } else {
            this.templateDatasetNature = null;
        }
    }

    /** Template Dataset Nature. */
    private Integer templateDatasetNature;

    /**
     * @return the templateDatasetNature
     */
    public TemplateDatasetNature getTemplateDatasetNature() {
        return TemplateDatasetNature.getNature(templateDatasetNature);
    }

    /**
     * @param templateDatasetNature
     *            the templateDatasetNature to set
     */
    public void setTemplateDatasetNature(TemplateDatasetNature templateDatasetNature) {
        if (templateDatasetNature == null) {
            this.templateDatasetNature = null;
        } else {
            this.templateDatasetNature = templateDatasetNature.getId();
        }
    }

    @Override
    public DatasetType getType() {
        return DatasetType.TEMPLATE;
    }

}
