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
