package org.shanoir.ng.subject;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.ShanoirSubjectException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Subject service.
 *
 * @author msimon
 *
 */
public interface SubjectService extends UniqueCheckableService<Subject> {

	/**
	 * Delete a subject.
	 * 
	 * @param id
	 *            subject id.
	 * @throws ShanoirSubjectException
	 */
	void deleteById(Long id) throws ShanoirSubjectException;

	/**
	 * Get all the subjects.
	 * 
	 * @return a list of subjects.
	 */
	List<Subject> findAll();

	/**
	 * Find subject by data.
	 *
	 * @param data
	 *            data.
	 * @return a subject.
	 */
	Optional<Subject> findByData(String data);

	/**
	 * Find subject by its id.
	 *
	 * @param id
	 *            template id.
	 * @return a template or null.
	 */
	Subject findById(Long id);

	/**
	 * Save a subject.
	 *
	 * @param subject
	 *            subject to create.
	 * @return created subject.
	 * @throws ShanoirSubjectException
	 */
	Subject save(Subject subject) throws ShanoirSubjectException;

	/**
	 * Update a subject.
	 *
	 * @param subject
	 *            subject to update.
	 * @return updated subject.
	 * @throws ShanoirSubjectException
	 */
	Subject update(Subject subject) throws ShanoirSubjectException;

	/**
	 * Update a subject from the old Shanoir
	 * 
	 * @param subject
	 *            subject.
	 * @throws ShanoirSubjectException
	 */
	void updateFromShanoirOld(Subject subject) throws ShanoirSubjectException;
	
	/**
	 * Get all the subjects of a study
	 * 
	 * @param studyId
	 * @return list of subjects
	 */
	public List<Subject> findAllSubjectsOfStudy(final Long studyId);
	
	/**
	 * Find subject by its identifier.
	 *
	 * @param indentifier
	 *            data.
	 * @return a indentifier.
	 */
	Subject findByIdentifier(String indentifier);
	

}
