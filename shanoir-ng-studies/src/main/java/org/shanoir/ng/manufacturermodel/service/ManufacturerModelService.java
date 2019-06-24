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

package org.shanoir.ng.manufacturermodel.service;

import java.util.List;

import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.core.service.BasicEntityService;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Manufacturer model service.
 * 
 * @author jlouis
 *
 */
public interface ManufacturerModelService extends BasicEntityService<ManufacturerModel> {

	
	/**
	 * Find id and name for all manufacturer models.
	 * 
	 * @return list of IdNameDTO.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<IdName> findIdsAndNames();
	
	/**
	 * Find id and name for manufacturer models related to a center.
	 * 
	 * @param centerId: the id of the center
	 * @return list of IdNameDTO.
	 */
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	List<IdName> findIdsAndNamesForCenter(Long centerId);

}
