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

package org.shanoir.ng.subject;

import java.util.List;

import org.mapstruct.Mapper;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subjectstudy.SubjectStudyMapper;

/**
 * Mapper for subjects.
 * 
 * @author msimon
 *
 */
@Mapper(componentModel = "spring", uses = { SubjectStudyMapper.class })
public interface SubjectMapper {

	SubjectDTO subjectToSubjectDTO(Subject subject);

	List<SubjectDTO> subjectsToSubjectDTOs(List<Subject> subjects);
	

}
