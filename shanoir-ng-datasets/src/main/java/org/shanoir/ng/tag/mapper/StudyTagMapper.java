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

package org.shanoir.ng.tag.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.shanoir.ng.tag.model.StudyTag;
import org.shanoir.ng.tag.model.StudyTagDTO;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudyTagMapper {

    List<StudyTagDTO> studyTagListToStudyTagDTOList(List<StudyTag> studyTags);

    StudyTagDTO studyTagToStudyTagDTO(StudyTag studyTag);

    List<StudyTag> studyTagDTOListToStudyTagList(List<StudyTagDTO> dtos);

    StudyTag studyTagDTOToStudyTag(StudyTagDTO dto);
}
