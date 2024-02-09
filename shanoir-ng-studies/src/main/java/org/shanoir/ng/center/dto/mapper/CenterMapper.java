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

package org.shanoir.ng.center.dto.mapper;

import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.shanoir.ng.acquisitionequipment.dto.mapper.AcquisitionEquipmentMapper;
import org.shanoir.ng.center.dto.CenterDTO;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.studycenter.StudyCenterMapper;

/**
 * Mapper for centers.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { AcquisitionEquipmentMapper.class, StudyCenterMapper.class })
public interface CenterMapper {

	@Named("centersWithoutAcquisitionEquipments")
	@IterableMapping(qualifiedByName = "centerWithoutAcquisitionEquipments")
	List<CenterDTO> centersToCenterDTOs(List<Center> centers);
	
	@Named("centerWithoutAcquisitionEquipments")
	@Mappings({
		@Mapping(target = "acquisitionEquipments", ignore = true),
		@Mapping(target = "compatible", ignore = true)
	})
	CenterDTO centerToCenterDTO(Center center);

	@Named("centersWithAcquisitionEquipments")
	@IterableMapping(qualifiedByName = "centerWithAcquisitionEquipments")
	List<CenterDTO> centersToCenterDTOsWithAcquisitionEquipments(List<Center> centers);

	@Named("centerWithAcquisitionEquipments")
	@Mappings({
		@Mapping(target = "compatible", ignore = true)
	})
	CenterDTO centerToCenterDTOWithAcquisitionEquipments(Center center);
	
	/**
	 * Map list of @Center to list of @IdNameDTO.
	 * 
	 * @param centers
	 *            list of centers.
	 * @return list of centers DTO.
	 */
	List<IdName> centersToIdNameDTOs(List<Center> centers);

	/**
	 * Map a @Center to a @IdNameDTO.
	 * 
	 * @param center
	 *            center to map.
	 * @return center DTO.
	 */
	IdName centerToIdNameDTO(Center center);

}
