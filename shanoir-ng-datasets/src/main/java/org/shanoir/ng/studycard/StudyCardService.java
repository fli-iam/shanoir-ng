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

package org.shanoir.ng.studycard;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirDatasetsException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Template service.
 *
 * @author msimon
 *
 */
public interface StudyCardService extends UniqueCheckableService<StudyCard> {

	/**
	 * Delete a template.
	 *
	 * @param id
	 *            template id.
	 * @throws ShanoirTemplateException
	 */
	void deleteById(Long id) throws ShanoirDatasetsException;

	/**
	 * Get all the template.
	 *
	 * @return a list of templates.
	 */
	List<StudyCard> findAll();

	/**
	 * Find template by its id.
	 *
	 * @param id
	 *            template id.
	 * @return a template or null.
	 */
	StudyCard findById(Long id);

	/**
	 * Save a template.
	 *
	 * @param template
	 *            template to create.
	 * @return created template.
	 * @throws ShanoirTemplateException
	 */
	StudyCard save(StudyCard template) throws ShanoirDatasetsException;

	/**
	 * Search study cards by their study id.
	 * 
	 * @param studyIdList
	 *            list of study ids.
	 * @return list of study cards.
	 */
	List<StudyCard> search(List<Long> studyIdList);
	
	/**
	 * Search center id of a study card.
	 * 
	 * @param studyCardId
	 *            the id of the study card.
	 * @return the id of the center.
	 */
	Long searchCenterId(Long studyCardId);

	/**
	 * Update a template.
	 *
	 * @param template
	 *            template to update.
	 * @return updated template.
	 * @throws ShanoirTemplateException
	 */
	StudyCard update(StudyCard template) throws ShanoirDatasetsException;

	/**
	 * Update a template from the old Shanoir
	 *
	 * @param template
	 *            template.
	 * @throws ShanoirTemplateException
	 */

	List<StudyCard> findStudyCardsOfStudy (Long studyId) throws ShanoirDatasetsException;

	/**
	 * delete study cardfrom Shanoir Old.
	 *
	 * @param StudyCard studyCard.
	 *
	 * @throws ShanoirDatasetsException
	 */
	void deleteFromShanoirOld(StudyCard studyCard) throws ShanoirDatasetsException;

	/**
	 * update study cardfrom Shanoir Old.
	 *
	 * @param StudyCard studyCard.
	 *
 	 * @throws ShanoirDatasetsException
	 */
	void updateFromShanoirOld(StudyCard studyCard) throws ShanoirDatasetsException;

}
