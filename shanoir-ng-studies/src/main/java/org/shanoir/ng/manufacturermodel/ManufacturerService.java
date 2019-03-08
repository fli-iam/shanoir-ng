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

package org.shanoir.ng.manufacturermodel;

import java.util.List;

import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * Manufacturer service.
 * 
 * @author msimon
 *
 */
public interface ManufacturerService extends UniqueCheckableService<Manufacturer> {

	/**
	 * Get all the manufacturers.
	 * 
	 * @return a list of manufacturers.
	 */
	List<Manufacturer> findAll();

	/**
	 * Find manufacturer by its id.
	 *
	 * @param id
	 *            manufacturer id.
	 * @return a manufacturer or null.
	 */
	Manufacturer findById(Long id);

	/**
	 * Save a manufacturer.
	 *
	 * @param manufacturer
	 *            manufacturer to create.
	 * @return created manufacturer.
	 * @throws ShanoirStudiesException
	 */
	Manufacturer save(Manufacturer manufacturer) throws ShanoirStudiesException;

	/**
	 * Update a manufacturer.
	 *
	 * @param manufacturer
	 *            manufacturer to update.
	 * @return updated manufacturer.
	 * @throws ShanoirStudiesException
	 */
	Manufacturer update(Manufacturer manufacturer) throws ShanoirStudiesException;

}
