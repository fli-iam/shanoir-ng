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

package org.shanoir.ng.study;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.study.dto.SimpleStudyDTO;
import org.shanoir.ng.timepoint.TimepointMapper;

/**
 * Mapper for studies.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { TimepointMapper.class })
@DecoratedWith(StudyDecorator.class)
public interface StudyMapper {

	/**
	 * Map list of @Study to list of @StudyDTO.
	 * 
	 * @param studies
	 *            list of studies.
	 * @return list of studies DTO.
	 */
	List<StudyDTO> studiesToStudyDTOs (List<Study> studies);

	/**
	 * Map a @Study to a @StudyDTO.
	 * 
	 * @param study
	 *            study to map.
	 * @return study DTO.
	 */
	@Mappings({ @Mapping(target = "experimentalGroupsOfSubjects", ignore = true),
			@Mapping(target = "membersCategories", ignore = true), @Mapping(target = "nbExaminations", ignore = true),
			@Mapping(target = "nbSujects", ignore = true), @Mapping(target = "studyCards", ignore = true),
			@Mapping(target = "studyCenterList", ignore = true), @Mapping(target = "subjectStudyList", ignore = true) })
	StudyDTO studyToStudyDTO (Study study);
	
	@Mappings({ @Mapping(target = "studyCenterList", ignore = true) })
	SimpleStudyDTO studyToSimpleStudyDTO (Study study);
	
	List<SimpleStudyDTO> studiesToSimpleStudyDTOs (List<Study> studies);
	
	IdNameDTO studyToIdNameDTO (Study study);
	
	List<IdNameDTO> studiesToIdNameDTOs (List<Study> studies);

}
