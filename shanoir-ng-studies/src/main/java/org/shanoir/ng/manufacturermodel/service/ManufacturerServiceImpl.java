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

import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.springframework.stereotype.Service;

/**
 * Manufacturer model service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class ManufacturerServiceImpl extends BasicEntityServiceImpl<Manufacturer> implements ManufacturerService {

	@Override
	protected Manufacturer updateValues(Manufacturer from, Manufacturer to) {
		return to;
	}


}
