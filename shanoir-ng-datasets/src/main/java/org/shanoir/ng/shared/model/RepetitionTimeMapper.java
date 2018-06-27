package org.shanoir.ng.shared.model;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for repetition Time.
 * 
 * @author atouboul
 *
 */

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RepetitionTimeMapper {

	
	List<RepetitionTime> RepetitionTimeDTOListToRepetitionTimeList(
			List<Double> repetitionTimeDTOList);


	@Mapping(target = "repetitionTimeValue", source = "repetitionTime")
	RepetitionTime RepetitionTimeDTOToRepetitionTime(
			Double repetitionTime);

}