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

package org.shanoir.ng.studycenter;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Mapper for link between a study and a center.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring")
@DecoratedWith(StudyCenterDecorator.class)
public interface StudyCenterMapper {

	/**
	 * Map list of @StudyCenter to list of @StudyCenterDTO.
	 * 
	 * @param studyCenterList
	 *            list of links between a study and a center.
	 * @return list of DTO.
	 */
	List<StudyCenterDTO> studyCenterListToStudyCenterDTOList(List<StudyCenter> studyCenterList);

	/**
	 * Map a @StudyCenter to a @StudyCenterDTO.
	 * 
	 * @param study
	 *            link between a study and a center.
	 * @return DTO.
	 */
	@Mappings({ @Mapping(target = "study.id", source = "study.id"),
		@Mapping(target = "center.id", source = "center.id"),
		@Mapping(target = "center.name", source = "center.name"),
		@Mapping(target = "study.name", source = "study.name")})
	StudyCenterDTO studyCenterToStudyCenterDTO(StudyCenter studyCenter);

}
