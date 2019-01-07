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
