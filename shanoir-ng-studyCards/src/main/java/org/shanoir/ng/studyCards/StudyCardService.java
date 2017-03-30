package org.shanoir.ng.studyCards;

import java.util.List;
import java.util.Optional;

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
	 * Find template by data.
	 *
	 * @param data
	 *            data.
	 * @return a template.
	 */
	Optional<StudyCard> findByData(String data);

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
	void updateFromShanoirOld(StudyCard template) throws ShanoirStudyCardsException;

}
