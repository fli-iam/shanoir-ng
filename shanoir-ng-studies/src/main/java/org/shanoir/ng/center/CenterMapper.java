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

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.shanoir.ng.acquisitionequipment.AcquisitionEquipmentMapper;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.studycenter.StudyCenterMapper;

/**
 * Mapper for centers.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { AcquisitionEquipmentMapper.class, StudyCenterMapper.class })
public interface CenterMapper {

	/**
	 * Map list of @Center to list of @CenterDTO.
	 * 
	 * @param centers
	 *            list of centers.
	 * @return list of centers DTO.
	 */
	List<CenterDTO> centersToCenterDTOs(List<Center> centers);

	/**
	 * Map a @Center to a @CenterDTO.
	 * 
	 * @param center
	 *            center to map.
	 * @return center DTO.
	 */
	@Mapping(target = "compatible", ignore = true) 
	CenterDTO centerToCenterDTO(Center center);

	/**
	 * Map list of @Center to list of @IdNameDTO.
	 * 
	 * @param centers
	 *            list of centers.
	 * @return list of centers DTO.
	 */
	List<IdNameDTO> centersToIdNameDTOs(List<Center> centers);

	/**
	 * Map a @Center to a @IdNameDTO.
	 * 
	 * @param center
	 *            center to map.
	 * @return center DTO.
	 */
	IdNameDTO centerToIdNameDTO(Center center);

}
