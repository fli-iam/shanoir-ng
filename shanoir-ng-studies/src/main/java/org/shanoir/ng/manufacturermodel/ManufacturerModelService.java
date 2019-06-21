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

import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;

/**
 * Manufacturer model service.
 * 
 * @author msimon
 *
 */
public interface ManufacturerModelService {

	/**
	 * Get all the manufacturer models.
	 * 
	 * @return a list of manufacturer models.
	 */
	List<ManufacturerModel> findAll();

	/**
	 * Find manufacturer model by its id.
	 *
	 * @param id
	 *            manufacturer model id.
	 * @return a manufacturer model or null.
	 */
	ManufacturerModel findById(Long id);

	/**
	 * Save an manufacturer model.
	 *
	 * @param manufacturerModel
	 *            manufacturer model to create.
	 * @return created manufacturer model.
	 * @throws ShanoirStudiesException
	 */
	ManufacturerModel save(ManufacturerModel manufacturerModel) throws ShanoirStudiesException;

	/**
	 * Update a manufacturer model.
	 *
	 * @param manufacturerModel
	 *            manufacturer model to update.
	 * @return updated manufacturer model.
	 * @throws ShanoirStudiesException
	 */
	ManufacturerModel update(ManufacturerModel manufacturerModel) throws ShanoirStudiesException;
	
	/**
	 * Find id and name for all manufacturer models.
	 * 
	 * @return list of IdNameDTO.
	 */
	List<IdNameDTO> findIdsAndNames();
	
	/**
	 * Find id and name for manufacturer models related to a center.
	 * 
	 * @param centerId: the id of the center
	 * @return list of IdNameDTO.
	 */
	List<IdNameDTO> findIdsAndNamesForCenter(Long centerId);

}
