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

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.studycard.model.StudyCard;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

public interface StudyCardService {

	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudyCard(#id, 'CAN_ADMINISTRATE'))")
	void deleteById(Long id) throws EntityNotFoundException, MicroServiceCommunicationException;

	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasRightOnStudy(#studyCard.getStudyId(), 'CAN_ADMINISTRATE'))")
	StudyCard save(StudyCard studyCard) throws MicroServiceCommunicationException;
	
	@PreAuthorize("hasRole('ADMIN') or (hasRole('EXPERT') and @datasetSecurityService.hasUpdateRightOnStudyCard(#studyCard, 'CAN_ADMINISTRATE'))")
	StudyCard update(StudyCard studyCard) throws EntityNotFoundException, MicroServiceCommunicationException;
	
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterStudyCardList(returnObject, 'CAN_SEE_ALL')")
	List<StudyCard> findAll();
	
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("returnObject == null || @datasetSecurityService.hasRightOnStudy(returnObject.getStudyId(), 'CAN_SEE_ALL')")
	StudyCard findById(Long id);

	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterStudyCardList(returnObject, 'CAN_SEE_ALL')")
	List<StudyCard> search(List<Long> studyIdList);
	
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterStudyCardList(returnObject, 'CAN_SEE_ALL')")
	List<StudyCard> findStudyCardsOfStudy (Long studyId);
	
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @datasetSecurityService.filterStudyCardList(returnObject, 'CAN_SEE_ALL')")
	List<StudyCard> findStudyCardsByAcqEq (Long acqEqId);

}
