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
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.model.QualityCard;
import org.springframework.validation.BindingResult;

public interface QualityCardService {

    /**
     * Validate a quality card
     *
     * @param qualityCard
     * @param result
     * @throws RestServiceException
     */
    void validate(QualityCard qualityCard, BindingResult result) throws RestServiceException;

    /**
     * Update an existing quality card
     *
     * @param qualityCard
     */
    void update(QualityCard qualityCard) throws EntityNotFoundException;

    /**
     * Nulls out the ids of the quality card's rules and their nested conditions, so that saving it
     * (creation, or duplication of an existing quality card) inserts fresh rows instead of failing on
     * stale/foreign ids carried over from the client payload.
     *
     * @param qualityCard
     */
    void resetIdsForFreshInsert(QualityCard qualityCard);

    /**
     * Quality cards for quality control: apply on entire study.
     *
     * @param qualityCard
     * @param updateTags for testing or for real apply
     */
    QualityCardResult applyQualityCardOnStudy(QualityCard qualityCard, boolean updateTags) throws PacsException;

    /**
     * Quality cards for quality control: apply on a sample of the study's examinations only
     * (used by the "test on sample" option for studies with too many examinations to test in full).
     *
     * @param qualityCard
     * @param updateTags for testing or for real apply
     * @param from index of the first examination to process, or null to process all
     * @param to index of the last examination to process, or null to process all
     */
    QualityCardResult applyQualityCardOnStudy(QualityCard qualityCard, boolean updateTags, Integer from, Integer to) throws PacsException;

    QualityCardResult checkQuality(DatasetAcquisition datasetAcquisition, AcquisitionAttributes<?> acquisitionAttributes, List<QualityCard> qualityCards) throws ShanoirException;
}
