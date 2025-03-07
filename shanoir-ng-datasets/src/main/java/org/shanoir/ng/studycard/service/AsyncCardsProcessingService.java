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

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.studycard.model.QualityCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

/**
 * Allow us to call the quality control async
 */
@Service
public class AsyncCardsProcessingService {
	
	private static final Logger LOG = LoggerFactory.getLogger(AsyncCardsProcessingService.class);

	@Autowired CardsProcessingService service;

	@Autowired QualityCardService qualityCardService;

    /**
	 * Study cards for quality control: apply on entire study.
	 *
	 * @param studyCard
	 * @throws MicroServiceCommunicationException
	 */
    @Async
	@Transactional
	public void applyQualityCardOnStudy(Long qualityCardId, boolean updateTags) throws MicroServiceCommunicationException, EntityNotFoundException {

		final QualityCard qualityCard = qualityCardService.findById(qualityCardId);
        if (qualityCard == null) {
            throw new EntityNotFoundException(QualityCard.class, qualityCardId);
        } else {
			LOG.info("test quality card: name:" + qualityCard.getName() + ", studyId: " + qualityCard.getStudyId());
			service.applyQualityCardOnStudy(qualityCardService.findById(qualityCard.getId()), updateTags);
		}
	}
}
