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

package org.shanoir.ng.subjectstudy;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Mapper for link between a subject and a study.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring")
@DecoratedWith(SubjectStudyDecorator.class)
public interface SubjectStudyMapper {

	/**
	 * Map list of @SubjectStudy to list of @SubjectStudyDTO.
	 * 
	 * @param subjectStudies
	 *            list of links between a subject and a study.
	 * @return list of DTO.
	 */
	List<SubjectStudyDTO> subjectStudyListToSubjectStudyDTOList(List<SubjectStudy> subjectStudies);

	/**
	 * Map a @SubjectStudy to a @SubjectStudyDTO.
	 * 
	 * @param study
	 *            link between a subject and a study.
	 * @return DTO.
	 */
	@Mappings({ @Mapping(target = "subject.id", source = "subject.id"),
			@Mapping(target = "subject.name", source = "subject.name"),	
			@Mapping(target = "study.id", source = "study.id"),
			@Mapping(target = "study.name", source = "study.name"),	
			@Mapping(target = "subjectStudyIdentifier", ignore = true) })
	SubjectStudyDTO subjectStudyToSubjectStudyDTO(SubjectStudy subjectStudy);

}
