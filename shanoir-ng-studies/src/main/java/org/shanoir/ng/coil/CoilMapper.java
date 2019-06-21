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

import org.mapstruct.Mapper;
import org.shanoir.ng.center.CenterMapper;
import org.shanoir.ng.manufacturermodel.ManufacturerModelMapper;

/**
 * Mapper for coils.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { CenterMapper.class/*, ManufacturerModelMapper.class*/ })
public interface CoilMapper {

	/**
	 * Map list of @Coil to list of @CoilDTO.
	 * 
	 * @param coils
	 *            list of coils.
	 * @return list of coils DTO.
	 */
	List<CoilDTO> coilsToCoilDTOs(List<Coil> coils);

	/**
	 * Map a @Coil to a @CoilDTO.
	 * 
	 * @param coil
	 *            coil to map.
	 * @return coil DTO.
	 */
	CoilDTO coilToCoilDTO(Coil coil);

}
