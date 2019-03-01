/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

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

