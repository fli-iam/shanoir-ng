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

package org.shanoir.ng.neurobagel.repository;

/**
 * One acquired dataset of a study, with what the Neurobagel imaging table needs.
 */
public interface NeurobagelImagingRow {

    Long getSubjectId();

    Long getExaminationId();

    Long getDatasetId();

    /** MR, PET, EEG, MEG or OTHER: see NeurobagelSuffix.Modality. */
    String getModality();

    /** MrDatasetNature id: study-card value, else import value. Null when unknown or not MR. */
    Integer getMrDatasetNature();

    /** MrSequenceApplication id: study-card value, else import value. Null when unknown or not MR. */
    Integer getMrSequenceApplication();

}
