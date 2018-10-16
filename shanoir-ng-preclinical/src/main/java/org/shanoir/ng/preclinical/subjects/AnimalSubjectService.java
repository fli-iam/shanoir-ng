package org.shanoir.ng.preclinical.subjects;

import java.util.List;

import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Refs service.
 *
 * @author sloury
 *
 */
public interface AnimalSubjectService extends UniqueCheckableService<AnimalSubject> {

	/**
	 * Delete a animalSubject value.
	 * 
	 * @param id
	 *            animalSubject id.
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * Get all the AnimalSubject.
	 * 
	 * @return a list of AnimalSubject.
	 */
	List<AnimalSubject> findAll();

	/**
	 * Find AnimalSubject by its id.
	 *
	 * @param id
	 *            AnimalSubject id.
	 * @return a AnimalSubject or null.
	 */
	AnimalSubject findById(Long id);

	/**
	 * Save a AnimalSubject.
	 *
	 * @param AnimalSubject
	 *            AnimalSubject to create.
	 * @return created AnimalSubject.
	 * @throws ShanoirException
	 */
	AnimalSubject save(AnimalSubject subject) throws ShanoirException;

	/**
	 * Update a AnimalSubject.
	 *
	 * @param AnimalSubject
	 *            AnimalSubject to update.
	 * @return updated AnimalSubject.
	 * @throws ShanoirException
	 */
	AnimalSubject update(AnimalSubject subject) throws ShanoirException;

	List<AnimalSubject> findByReference(Reference reference);

}
