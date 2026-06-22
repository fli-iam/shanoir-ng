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

package org.shanoir.ng.importer.dto;

import java.util.List;

import org.shanoir.ng.dataset.modality.EegDatasetDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Communication object to carry EEG data to be loaded in Shanoir.
 * @author JcomeD
 *
 */
public class EegImportJob extends ImportJob {

    private static final long serialVersionUID = 2425683448060201704L;

    /** List of associated datasets. */
    @JsonProperty("subjectId")
    private Long subjectId;

    /** List of associated datasets. */
    @JsonProperty("datasets")
    private List<EegDatasetDTO> datasets;

    public List<EegDatasetDTO> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<EegDatasetDTO> datasets) {
        this.datasets = datasets;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }
}
