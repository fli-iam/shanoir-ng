package org.shanoir.ng.shared.model;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for echo Time.
 * 
 * @author atouboul
 *
 */

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EchoTimeMapper {

	
	List<EchoTime> EchoTimeDTOListToEchoTimeList(
			List<org.shanoir.ng.importer.dto.EchoTime> echoTimeDTOList);


	@Mapping(target = "echoTimeValue", source = "echoTime")
	EchoTime EchoTimeDTOToEchoTime(
			org.shanoir.ng.importer.dto.EchoTime echoTimes);

}

