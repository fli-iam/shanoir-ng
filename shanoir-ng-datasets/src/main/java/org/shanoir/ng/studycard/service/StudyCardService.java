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

/**
 * Template service.
 *
 * @author msimon
 *
 */
public interface StudyCardService {

	/**
	 * Delete a template.
	 *
	 * @param id template id.
	 * @throws EntityNotFoundException
	 * @throws MicroServiceCommunicationException 
	 */
	void deleteById(Long id) throws EntityNotFoundException, MicroServiceCommunicationException;

	/**
	 * Get all the template.
	 *
	 * @return a list of templates.
	 */
	List<StudyCard> findAll();

	/**
	 * Find template by its id.
	 *
	 * @param id template id.
	 * @return a template or null.
	 */
	StudyCard findById(Long id);
	
	/**
	 * Find template by its id.
	 *
	 * @param id template id.
	 * @return a template or null.
	 */
	StudyCard findByName(String name);

	/**
	 * Save a template.
	 *
	 * @param template template to create.
	 * @return created template.
	 * @throws MicroServiceCommunicationException 
	 */
	StudyCard save(StudyCard template) throws MicroServiceCommunicationException;

	/**
	 * Search study cards by their study id.
	 * 
	 * @param studyIdList list of study ids.
	 * @return list of study cards.
	 */
	List<StudyCard> search(List<Long> studyIdList);
	
	/**
	 * Search center id of a study card.
	 * 
	 * @param studyCardId the id of the study card.
	 * @return the id of the center.
	 */
	Long searchCenterId(Long studyCardId);

	/**
	 * Update a template.
	 *
	 * @param template template to update.
	 * @return updated template.
	 * @throws EntityNotFoundException
	 * @throws MicroServiceCommunicationException 
	 */
	StudyCard update(StudyCard template) throws EntityNotFoundException, MicroServiceCommunicationException;

	/**
	 * Update a template from the old Shanoir
	 *
	 * @param template template.
	 * @throws EntityNotFoundException
	 */
	List<StudyCard> findStudyCardsOfStudy (Long studyId) throws EntityNotFoundException;

}
