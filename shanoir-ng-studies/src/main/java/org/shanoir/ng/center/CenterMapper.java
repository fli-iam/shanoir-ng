package org.shanoir.ng.center;

import java.util.List;

import org.mapstruct.Mapper;
import org.shanoir.ng.acquisitionequipment.AcquisitionEquipmentMapper;

/**
 * Mapper for centers.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = AcquisitionEquipmentMapper.class)
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
	CenterDTO centerToCenterDTO(Center center);

	/**
	 * Map list of @Center to list of @SimpleCenterDTO.
	 * 
	 * @param centers
	 *            list of centers.
	 * @return list of centers DTO.
	 */
	List<SimpleCenterDTO> centersToSimpleCenterDTOs(List<Center> centers);

	/**
	 * Map a @Center to a @SimpleCenterDTO.
	 * 
	 * @param center
	 *            center to map.
	 * @return center DTO.
	 */
	SimpleCenterDTO centerToSimpleCenterDTO(Center center);

}
