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

package org.shanoir.ng.importer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Communication object to carry EEG data to be loaded in Shanoir.
 * @author JcomeD
 *
 */
public class EegImportJob extends ImportJob {

    private static final long serialVersionUID = -5482473150099609081L;

    @JsonProperty("datasets")
    private List<EegDataset> datasets;

    @JsonProperty("subjectId")
    private Long subjectId;

    public List<EegDataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<EegDataset> datasets) {
        this.datasets = datasets;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

}
