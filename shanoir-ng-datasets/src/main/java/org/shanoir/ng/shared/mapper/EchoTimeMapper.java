package org.shanoir.ng.shared.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.shanoir.ng.shared.model.EchoTime;

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

