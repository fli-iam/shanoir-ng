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

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Attention: still used and necessary for ImportFromTableRunner, where
 * we need to work with multiple-patients coming back from DICOM server.
 *
 * (Legacy) import job model, kept alive to operate ongoing legacy imports
 * while new code is migrated to {@link ImportJobBase}. In addition to
 * everything in {@link ImportJobBase}, this class still carries:
 * <ul>
 * <li>{@code patients}: legacy list, kept only because some old code
 * paths still read the DICOM hierarchy through it (@todo: remove
 * once those callers are migrated to {@code series}/{@code study})</li>
 * <li>{@code selectedSeries}: still populated by the legacy import
 * flow; equivalent in purpose to {@link ImportJobBase#getSeries()}
 * but kept separate until legacy callers are migrated</li>
 * </ul>
 *
 * Once all legacy imports have migrated to the {@code ImportJobBase}
 * shape, this class can be deleted and {@code ImportJobBase} renamed
 * to {@code ImportJob}.
 *
 * @author mkain
 */
public class ImportJob extends ImportJobBase implements Serializable {

    private static final long serialVersionUID = 1L;

    // @todo: remove this list here later
    private List<Patient> patients;

    // series selected for import with this (legacy) import job
    private List<Serie> selectedSeries;

    public List<Patient> getPatients() {
        return patients;
    }

    public void setPatients(final List<Patient> patients) {
        this.patients = patients;
    }

    public List<Serie> getSelectedSeries() {
        return selectedSeries;
    }

    public void setSelectedSeries(List<Serie> selectedSeries) {
        this.selectedSeries = selectedSeries;
    }

    @Override
    public String toString() {
        String importType;
        if (isFromDicomZip()) {
            importType = "ZIP";
        } else if (isFromShanoirUploader()) {
            importType = "SHUP";
        } else if (isFromPacs()) {
            importType = "PACS";
        } else {
            importType = "UNSUPPORTED";
        }
        int numberOfSeries = 0;
        StringBuffer seriesNames = new StringBuffer();
        seriesNames.append("[");
        String modality = "unknown";
        boolean enhanced = false;
        if (CollectionUtils.isNotEmpty(patients)) {
            Patient patient = patients.get(0);
            if (CollectionUtils.isNotEmpty(patient.getStudies())) {
                Study study = patient.getStudies().get(0);
                List<Serie> series = study.getSeries();
                if (CollectionUtils.isNotEmpty(series)) {
                    numberOfSeries = series.size(); // only selected series remain at the stage of the logging call
                    Serie serie = study.getSeries().get(0);
                    modality = serie.getModality();
                    enhanced = serie.getIsEnhanced();
                    for (Iterator<Serie> iterator = series.iterator(); iterator.hasNext();) {
                        serie = (Serie) iterator.next();
                        if (iterator.hasNext()) {
                            seriesNames.append(serie.getSequenceName() + ",");
                        } else {
                            seriesNames.append(serie.getSequenceName() + "]");
                        }
                    }
                }
            }
        }
        return "userId=" + getUserId() + ",studyName=" + getStudyName() + ",studyCardId=" + getStudyCardId() + ",type="
                + importType
                + ",workFolder=" + getWorkFolder() + ",pseudoProfile=" + getAnonymisationProfileToUse() + ",modality="
                + modality + ",enhanced=" + enhanced
                + ",subjectName=" + getSubjectName() + ",examinationId=" + getExaminationId() + ",StudyInstanceUID="
                + getStudyInstanceUID() + ",numberOfSeries=" + numberOfSeries
                + ",seriesNames=" + seriesNames.toString();
    }

    @Override
    @JsonIgnore
    public Serie getFirstSerie() {
        if (CollectionUtils.isNotEmpty(selectedSeries)) {
            return selectedSeries.getFirst();
        }
        return null;
    }

    /**
     * Legacy variant: looks up the first serie with institution and
     * equipment set within {@code selectedSeries} rather than the
     * {@code series} field used by {@link ImportJobBase}.
     *
     * @return the first selected Serie with both institution and equipment
     *         set, or null if none of the selected series contains both.
     */
    @Override
    @JsonIgnore
    public Serie getFirstSerieWithInstitutionAndEquipment() {
        if (CollectionUtils.isNotEmpty(selectedSeries)) {
            for (Serie serie : selectedSeries) {
                if (serie.getInstitution().isKnown() && serie.getEquipment().isKnown()) {
                    return serie;
                }
            }
            // In case all are unknown, return first
            return selectedSeries.getFirst();
        }
        return null;
    }

}
