package org.shanoir.ng.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.shanoir.ng.center.Center;
import org.shanoir.ng.center.CenterDTO;

/**
 * Mapper for centers.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring")
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

}
