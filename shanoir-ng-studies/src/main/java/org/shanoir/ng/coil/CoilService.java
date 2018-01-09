package org.shanoir.ng.coil;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.exception.ShanoirStudiesException;

/**
 * Coil service.
 *
 * @author msimon
 *
 */
public interface CoilService {

	/**
	 * Delete a coil.
	 * 
	 * @param id
	 *            coil id.
	 * @throws ShanoirStudiesException
	 */
	void deleteById(Long id) throws ShanoirStudiesException;

	/**
	 * Get all the coils.
	 * 
	 * @return a list of coils.
	 */
	List<Coil> findAll();

	/**
	 * Find coil by name.
	 *
	 * @param name
	 *            name.
	 * @return a coil.
	 */
	Optional<Coil> findByName(String name);

	/**
	 * Find coil by its id.
	 *
	 * @param id
	 *            coil id.
	 * @return a coil or null.
	 */
	Coil findById(Long id);

	/**
	 * Save a coil.
	 *
	 * @param coil
	 *            coil to create.
	 * @return created coil.
	 * @throws ShanoirStudiesException
	 */
	Coil save(Coil coil) throws ShanoirStudiesException;

	/**
	 * Update a coil.
	 *
	 * @param coil
	 *            coil to update.
	 * @return updated coil.
	 * @throws ShanoirStudiesException
	 */
	Coil update(Coil coil) throws ShanoirStudiesException;

	/**
	 * Update a coil from the old Shanoir
	 * 
	 * @param coil
	 *            coil.
	 * @throws ShanoirStudiesException
	 */
	void updateFromShanoirOld(Coil coil) throws ShanoirStudiesException;

}
