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

package org.shanoir.ng.center;

import java.util.List;
import java.util.Optional;

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.validation.UniqueCheckableService;

/**
 * center service.
 *
 * @author msimon
 *
 */
public interface CenterService extends UniqueCheckableService<Center> {

	/**
	 * Delete a center.
	 * 
	 * @param id
	 *            center id.
	 * @throws ShanoirStudiesException
	 */
	void deleteById(Long id) throws ShanoirStudiesException;

	/**
	 * Get all the center.
	 * 
	 * @return a list of centers.
	 */
	List<Center> findAll();

	/**
	 * Find center by name.
	 *
	 * @param name
	 *            name.
	 * @return a center.
	 */
	Optional<Center> findByName(String name);

	/**
	 * Find center by its id.
	 *
	 * @param id
	 *            center id.
	 * @return a center or null.
	 */
	Center findById(Long id);

	/**
	 * Find id and name for all centers.
	 * 
	 * @return list of centers.
	 */
	List<IdNameDTO> findIdsAndNames();

	/**
	 * Save a center.
	 *
	 * @param center
	 *            center to create.
	 * @return created center.
	 * @throws ShanoirStudiesException
	 */
	Center save(Center center) throws ShanoirStudiesException;

	/**
	 * Update a center.
	 *
	 * @param center
	 *            center to update.
	 * @return updated center.
	 * @throws ShanoirStudiesException
	 */
	Center update(Center center) throws ShanoirStudiesException;

	/**
	 * Update a center from the old Shanoir
	 * 
	 * @param center
	 *            center.
	 * @throws ShanoirStudiesException
	 */
	void updateFromShanoirOld(Center center) throws ShanoirStudiesException;

}
