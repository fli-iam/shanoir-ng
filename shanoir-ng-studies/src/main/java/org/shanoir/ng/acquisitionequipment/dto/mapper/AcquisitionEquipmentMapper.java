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

<<<<<<< HEAD:shanoir-ng-studies/src/main/java/org/shanoir/ng/acquisitionequipment/dto/mapper/AcquisitionEquipmentMapper.java
package org.shanoir.ng.acquisitionequipment.dto.mapper;
=======
package org.shanoir.ng.acquisitionequipment;
>>>>>>> upstream/develop:shanoir-ng-studies/src/main/java/org/shanoir/ng/acquisitionequipment/AcquisitionEquipmentMapper.java

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.shanoir.ng.acquisitionequipment.dto.AcquisitionEquipmentDTO;
import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.center.dto.mapper.CenterMapper;
import org.shanoir.ng.manufacturermodel.dto.mapper.ManufacturerModelMapper;

/**
 * Mapper for acquisition equipments.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { CenterMapper.class, ManufacturerModelMapper.class })
public interface AcquisitionEquipmentMapper {

	/**
	 * Map list of @AcquisitionEquipment to list of @AcquisitionEquipmentDTO.
	 * 
	 * @param acquisitionEquipments
	 *            list of acquisition equipments.
	 * @return list of acquisition equipments DTO.
	 */
	List<AcquisitionEquipmentDTO> acquisitionEquipmentsToAcquisitionEquipmentDTOs(
			List<AcquisitionEquipment> acquisitionEquipments);

	/**
	 * Map a @AcquisitionEquipment to a @AcquisitionEquipmentDTO.
	 * 
	 * @param acquisitionEquipment
	 *            acquisition equipment to map.
	 * @return acquisition equipment DTO.
	 */
	@Mapping(target = "compatible", ignore = true) 
	AcquisitionEquipmentDTO acquisitionEquipmentToAcquisitionEquipmentDTO(AcquisitionEquipment acquisitionEquipment);

}
