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
public interface InversionTimeMapper {

	
	List<InversionTime> InversionTimeDTOListToInversionTimeList(
			List<Double> inversionTimeDTOList);


	@Mapping(target = "inversionTimeValue", source = "inversionTime")
	InversionTime InversionTimeDTOToInversionTime(
			Double inversionTime);

}