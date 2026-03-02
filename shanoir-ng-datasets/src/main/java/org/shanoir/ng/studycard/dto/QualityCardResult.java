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

package org.shanoir.ng.studycard.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.shared.quality.QualityTag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class contains the result of an application of a quality card
 * on an entire study. For each examination, when the result is wrong
 * on the examination level already, we will not check deeper on the
 * acquisition level. When everything is clear on the examination level,
 * we check deeper on the acquisition level, if there is an error, we stop.
 * Same for the dataset level.
 *
 * In the idea, that it does not make sense to display an error on a dataset,
 * when there is already a sequence missing on the exam. Display, that at first
 * and then display the dataset result, when that problem solved?
 *
 * @author mkain
 *
 */
public class QualityCardResult extends CopyOnWriteArrayList<QualityCardResultEntry> {

    private List<DatasetAcquisition> updatedDatasetAcquisitions = new CopyOnWriteArrayList<>();

    public List<DatasetAcquisition> getUpdatedDatasetAcquisitions() {
        return updatedDatasetAcquisitions;
    }

    private void setUpdatedDatasetAcquisitions(List<DatasetAcquisition> updatedDatasetAcquisitions) {
        this.updatedDatasetAcquisitions = updatedDatasetAcquisitions;
    }

    public void addUpdatedDatasetAcquisition(DatasetAcquisition datasetAcquisition) {
        if (getUpdatedDatasetAcquisitions() == null) setUpdatedDatasetAcquisitions(new ArrayList<>());
        if (datasetAcquisition == null || datasetAcquisition.getId() == null) return;
        for (DatasetAcquisition presentDatasetAcq : getUpdatedDatasetAcquisitions()) {
            if (datasetAcquisition.getId().equals(presentDatasetAcq.getId())
                    && presentDatasetAcq.getQualityTag().getId() >= datasetAcquisition.getQualityTag().getId()) {
                return;
            }
        }
        getUpdatedDatasetAcquisitions().add(datasetAcquisition);
    }

    // /***
    //  * Remove unchanged subject-studies
    //  * @param study the study containing the original subject-studies
    //  */
    // public void removeUnchanged(Study study) {
    //     if (getUpdatedSubjects() == null) return;
    //     for (Subject original : study.getSubjectList()) {
    //         getUpdatedSubjects().removeIf(updated ->
    //                 updated.getId().equals(original.getId())
    //                 && updated.getQualityTag() != null
    //                 && updated.getQualityTag().equals(original.getQualityTag())
    //         );
    //     }
    // }

    public void merge(QualityCardResult result) {
        this.addAll(result);
        if (result.getUpdatedDatasetAcquisitions() != null) {
            for (DatasetAcquisition datasetAcquisition : result.getUpdatedDatasetAcquisitions()) {
                this.addUpdatedDatasetAcquisition(datasetAcquisition);
            }
        }
    }

    public boolean hasError() {
        for (QualityCardResultEntry entry : this) {
            if (QualityTag.ERROR.equals(entry.getTagSet())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasWarning() {
        for (QualityCardResultEntry entry : this) {
            if (QualityTag.WARNING.equals(entry.getTagSet())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFailedValid() {
        for (QualityCardResultEntry entry : this) {
            if (entry.isFailedValid()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "json error";
        }
    }

}
