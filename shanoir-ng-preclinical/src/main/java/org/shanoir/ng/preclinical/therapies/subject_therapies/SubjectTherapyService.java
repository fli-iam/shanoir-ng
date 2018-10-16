package org.shanoir.ng.preclinical.therapies.subject_therapies;

import java.util.List;

import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.preclinical.therapies.Therapy;
import org.shanoir.ng.shared.exception.ShanoirException;
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
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * delete all subject therapies for a given animalSubject
	 * 
	 * @param animalSubject
	 * @throws ShanoirException
	 */
	public void deleteByAnimalSubject(AnimalSubject animalSubject) throws ShanoirException;

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
	 * @throws ShanoirException
	 */
	SubjectTherapy save(SubjectTherapy subtherapy) throws ShanoirException;

	/**
	 * Update a subject therapy.
	 *
	 * @param subject
	 *            therapy subject therapy to update.
	 * @return updated SubjectTherapy.
	 * @throws ShanoirException
	 */
	SubjectTherapy update(SubjectTherapy subtherapy) throws ShanoirException;

}
