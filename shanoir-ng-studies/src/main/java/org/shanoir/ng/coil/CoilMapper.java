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
