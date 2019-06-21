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

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.shared.dto.IdNameDTO;


/**
 * Mapper for manufacturer models.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring")
public interface ManufacturerModelMapper {

	/**
	 * Map a @ManufacturerModel to a @IdNameDTO.
	 * 
	 * @param manufacturerModel
	 *            manufacturer model to map.
	 * @return manufacturer model DTO.
	 */
	IdNameDTO manufacturerModelToIdNameDTO(ManufacturerModel manufacturerModel);

	/**
	 * Map a @ManufacturerModel to a @ManufacturerModelDTO.
	 * 
	 * @param manufacturerModel
	 *            manufacturer model to map.
	 * @return manufacturer model DTO.
	 */
	@Mappings({ @Mapping(source = "manufacturer.name", target = "manufacturerName")})
	ManufacturerModelDTO manufacturerModelToManufacturerModelDTO(ManufacturerModel manufacturerModel);

}
