package org.shanoir.ng.preclinical.therapies;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;



/**
 * Therapies service.
 *
 * @author sloury
 *
 */
public interface TherapyService extends UniqueCheckableService<Therapy> {

	/**
	 * Delete a therapy.
	 * 
	 * @param id
	 *            therapy id.
	 * @throws ShanoirException
	 */
	void deleteById(Long id) throws ShanoirException;

	/**
	 * Get all the therapies.
	 * 
	 * @return a list of therapies.
	 */
	List<Therapy> findAll();

	
	/**
	 * Find Therapy by its id.
	 *
	 * @param id
	 *            Therapy id.
	 * @return a Therapy or null.
	 */
	Therapy findById(Long id);
	
	/**
	 * Find Therapy by its name.
	 *
	 * @param id
	 *            Therapy name.
	 * @return a Therapy or null.
	 */
	Therapy findByName(String name);
	
	/**
	 * Find Therapies by their type.
	 *
	 * @param type
	 *            Therapy type.
	 * @return a list of therapies or null.
	 */
	List<Therapy> findByTherapyType(TherapyType therapyType);

	/**
	 * Save a therapy.
	 *
	 * @param therapy
	 *            therapy to create.
	 * @return created therapy.
	 * @throws ShanoirException
	 */
	Therapy save(Therapy therapy) throws ShanoirException;

	/**
	 * Update a therapy.
	 *
	 * @param therapy
	 *            therapy to update.
	 * @return updated therapy.
	 * @throws ShanoirException
	 */
	Therapy update(Therapy therapy) throws ShanoirException;

	

}
