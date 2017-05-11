package org.shanoir.ng.studycard;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirStudyCardsException;
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
	void deleteById(Long id) throws ShanoirStudyCardsException;

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
	StudyCard save(StudyCard template) throws ShanoirStudyCardsException;

	/**
	 * Search study cards by their study id.
	 * 
	 * @param studyIdList
	 *            list of study ids.
	 * @return list of study cards.
	 */
	List<StudyCard> search(List<Long> studyIdList);

	/**
	 * Update a template.
	 *
	 * @param template
	 *            template to update.
	 * @return updated template.
	 * @throws ShanoirTemplateException
	 */
	StudyCard update(StudyCard template) throws ShanoirStudyCardsException;

	/**
	 * Update a template from the old Shanoir
	 *
	 * @param template
	 *            template.
	 * @throws ShanoirTemplateException
	 */

	List<StudyCard> findStudyCardsOfStudy (Long studyId) throws ShanoirStudyCardsException;

	/**
	 * delete study cardfrom Shanoir Old.
	 *
	 * @param StudyCard studyCard.
	 *
	 * @throws ShanoirStudyCardsException
	 */
	void deleteFromShanoirOld(StudyCard studyCard) throws ShanoirStudyCardsException;

	/**
	 * update study cardfrom Shanoir Old.
	 *
	 * @param StudyCard studyCard.
	 *
 	 * @throws ShanoirStudyCardsException
	 */
	void updateFromShanoirOld(StudyCard studyCard) throws ShanoirStudyCardsException;

}
