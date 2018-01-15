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
