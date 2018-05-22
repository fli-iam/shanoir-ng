package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

import java.util.List;

import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModel;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Subject Pathology service.
 *
 * @author sloury
 *
 */
public interface SubjectPathologyService extends UniqueCheckableService<SubjectPathology> {

	/**
	 * Delete a reference value.
	 * 
	 * @param id
	 *            template id.
	 * @throws ShanoirPreclinicalException
	 */
	void deleteById(Long id) throws ShanoirPreclinicalException;

	/**
	 * delete all subject pathologies for a given animalSubject
	 * 
	 * @param animalSubject
	 * @throws ShanoirPreclinicalException
	 */
	public void deleteByAnimalSubject(AnimalSubject animalSubject) throws ShanoirPreclinicalException;

	/**
	 * Get all the references.
	 * 
	 * @return a list of references.
	 */
	List<SubjectPathology> findAll();

	List<SubjectPathology> findByAnimalSubject(AnimalSubject animalSubject);

	List<SubjectPathology> findAllByPathology(Pathology pathology);

	List<SubjectPathology> findAllByPathologyModel(PathologyModel model);

	List<SubjectPathology> findAllByLocation(Reference location);

	/**
	 * Find reference by its id.
	 *
	 * @param id
	 *            reference id.
	 * @return a reference or null.
	 */
	SubjectPathology findById(Long id);

	/**
	 * Save a reference.
	 *
	 * @param reference
	 *            reference to create.
	 * @return created reference.
	 * @throws ShanoirPreclinicalException
	 */
	SubjectPathology save(SubjectPathology pathos) throws ShanoirPreclinicalException;

	/**
	 * Update a reference.
	 *
	 * @param reference
	 *            reference to update.
	 * @return updated reference.
	 * @throws ShanoirPreclinicalException
	 */
	SubjectPathology update(SubjectPathology pathos) throws ShanoirPreclinicalException;

}
