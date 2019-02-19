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

package org.shanoir.ng.groupofsubjects;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.shared.dto.IdNameDTO;

/**
 * Mapper for experimental group of subjects.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring")
public interface ExperimentalGroupOfSubjectsMapper {

	/**
	 * Map list of @ExperimentalGroupOfSubjects to list of @IdNameDTO.
	 * 
	 * @param experimentalGroupsOfSubjects
	 *            list of experimental group of subjects.
	 * @return list of DTO with id and name.
	 */
	List<IdNameDTO> experimentalGroupOfSubjectsToIdNameDTOs(List<ExperimentalGroupOfSubjects> experimentalGroupsOfSubjects);

	/**
	 * Map a @ExperimentalGroupOfSubjects to a @StudyDTO.
	 * 
	 * @param experimentalGroupOfSubjects
	 *            experimental group of subjects to map.
	 * @return DTO with id and name.
	 */
	@Mappings({ @Mapping(target = "name", source = "groupName") })
	IdNameDTO experimentalGroupOfSubjectsToIdNameDTO(ExperimentalGroupOfSubjects experimentalGroupOfSubjects);

}
