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

package org.shanoir.ng.subject.dto.mapper;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subjectstudy.dto.mapper.SubjectStudyMapper;
import org.springframework.data.domain.Page;

/**
 * Mapper for subjects.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { SubjectStudyMapper.class })
@DecoratedWith(SubjectDecorator.class)
public interface SubjectMapper {

	@Named("subjectWithStudyList")
	@Mappings({ @Mapping(target = "studyId", source = "study.id")})
	SubjectDTO subjectToSubjectDTO(Subject subject);

	@Named("subjectWithoutStudyList")
	@Mappings({ @Mapping(target = "studyId", source = "study.id"),
		@Mapping(target = "subjectStudyList", ignore = true) })
	SubjectDTO subjectToSubjectDTONoStudies(Subject subject);

	@IterableMapping(qualifiedByName = "subjectWithStudyList")
	List<SubjectDTO> subjectsToSubjectDTOs(List<Subject> subjects);

	default PageImpl<SubjectDTO> subjectsToSubjectDTOs(Page<Subject> page) {
		List<SubjectDTO> dtos = subjectsToSubjectDTOs(page.getContent());
		return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
	}

}
