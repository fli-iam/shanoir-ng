package org.shanoir.ng.preclinical.therapies.subject_therapies;

import java.util.List;

import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.preclinical.therapies.Therapy;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Subject Therapy service.
 *
 * @author sloury
 *
 */
public interface SubjectTherapyService extends UniqueCheckableService<SubjectTherapy> {

	/**
	 * Delete a subject therapy.
	 * 
	 * @param id
	 *            subject therapy id.
	 * @throws ShanoirPreclinicalException
	 */
	void deleteById(Long id) throws ShanoirPreclinicalException;

	/**
	 * delete all subject therapies for a given animalSubject
	 * 
	 * @param animalSubject
	 * @throws ShanoirPreclinicalException
	 */
	public void deleteByAnimalSubject(AnimalSubject animalSubject) throws ShanoirPreclinicalException;

	/**
	 * Get all the subject therapies.
	 * 
	 * @return a list of subject therapies.
	 */
	List<SubjectTherapy> findAll();

	List<SubjectTherapy> findAllByAnimalSubject(AnimalSubject animalSubject);

	List<SubjectTherapy> findAllByTherapy(Therapy therapy);

	/**
	 * Find subject therapy by its id.
	 *
	 * @param id
	 *            subject therapy id.
	 * @return a subject therapy or null.
	 */
	SubjectTherapy findById(Long id);

	/**
	 * Save a subject therapy.
	 *
	 * @param subject
	 *            therapy subject therapy to create.
	 * @return created SubjectTherapy.
	 * @throws ShanoirPreclinicalException
	 */
	SubjectTherapy save(SubjectTherapy subtherapy) throws ShanoirPreclinicalException;

	/**
	 * Update a subject therapy.
	 *
	 * @param subject
	 *            therapy subject therapy to update.
	 * @return updated SubjectTherapy.
	 * @throws ShanoirPreclinicalException
	 */
	SubjectTherapy update(SubjectTherapy subtherapy) throws ShanoirPreclinicalException;

}
