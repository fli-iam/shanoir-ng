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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    /***
     * Add a dataset acquisition to the list of updated dataset acquisitions,
     * if it is not already in the list or if it is in the list but with a better quality tag.
     * @param datasetAcquisition
     */
    public void addUpdatedDatasetAcquisition(DatasetAcquisition datasetAcquisition) {
        if (datasetAcquisition == null || datasetAcquisition.getId() == null) return;
        if (getUpdatedDatasetAcquisitions() == null) setUpdatedDatasetAcquisitions(new ArrayList<>());

        for (int i = 0; i < getUpdatedDatasetAcquisitions().size(); i++) {
            DatasetAcquisition presentDatasetAcq = getUpdatedDatasetAcquisitions().get(i);
            if (datasetAcquisition.getId().equals(presentDatasetAcq.getId())) {
                // if a same id is found, we replace if the new quality tag is worst than the previous one
                if (datasetAcquisition.getQualityTag().getId() > presentDatasetAcq.getQualityTag().getId()) {
                    getUpdatedDatasetAcquisitions().set(i, datasetAcquisition);
                }
                return;
            }
        }
        getUpdatedDatasetAcquisitions().add(datasetAcquisition);
    }

    public void merge(QualityCardResult result) {
        this.addAll(result);
        if (result.getUpdatedDatasetAcquisitions() != null) {
            for (DatasetAcquisition datasetAcquisition : result.getUpdatedDatasetAcquisitions()) {
                this.addUpdatedDatasetAcquisition(datasetAcquisition);
            }
        }
    }

    public boolean isValid() {
        for (QualityCardResultEntry entry : this) {
            if (QualityTag.VALID.equals(entry.getTagSet()) && !entry.isFailedValid()) {
                return true;
            }
        }
        return false;
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

    /*
     * Find the quality card result entry with the worst quality tag by its dataset acquisition ID.
     * @param id the dataset acquisition ID
     * @return the quality card result entry if found, empty optional otherwise
     */
    public Optional<QualityCardResultEntry> findById(Long id) {
        return stream()
            .filter(entry -> Objects.equals(entry.getDatasetAcquisitionId(), id))
            .max(Comparator.comparingInt(entry -> entry.getTagSet().getId()));
    }

}
