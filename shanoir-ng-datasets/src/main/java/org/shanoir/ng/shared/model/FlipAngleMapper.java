package org.shanoir.ng.shared.model;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for inversion Time.
 * 
 * @author atouboul
 *
 */

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlipAngleMapper {

	
	List<FlipAngle> FlipAngleDTOListToFlipAngleList(
			List<String> flipAngleDTOList);


	@Mapping(target = "flipAngleValue", source = "flipAngle")
	FlipAngle FlipAngleDTOToFlipAngle(
			String flipAngle);

}