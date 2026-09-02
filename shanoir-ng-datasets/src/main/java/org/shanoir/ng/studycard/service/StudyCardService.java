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

package org.shanoir.ng.studycard.service;

import java.util.List;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.PacsException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.studycard.model.DicomTag;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.model.StudyCardApply;
import org.springframework.validation.BindingResult;

public interface StudyCardService {

    /**
     * Validate a quality card
     *
     * @param studyCard
     * @param result
     * @throws RestServiceException
     */
    void validate(StudyCard studyCard, BindingResult result) throws RestServiceException;

    /**
     * Update an existing quality card
     *
     * @param studyCard
     */
    void update(StudyCard studyCard) throws EntityNotFoundException;

    /**
     * Find all dicom tag types
     */
    List<DicomTag> findDicomTags() throws RestServiceException;

    /**
     * Apply a study card on an acquisition
     *
     * @param studyCard
     * @param studyCardApplyObject
     */
    void applyStudyCard(StudyCard studyCard, StudyCardApply studyCardApplyObject) throws PacsException, EntityNotFoundException;

    /**
     * Apply a study card's rules on a single dataset acquisition (and its datasets).
     * Used during import, when dicoms are present in tmp directory.
     *
     * @return true if the application had any effect on the acquisition
     */
    boolean applyStudyCardOnAcquisition(StudyCard studyCard, DatasetAcquisition acquisition, AcquisitionAttributes<?> dicomAttributes) throws IllegalStateException;
}
