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

package org.shanoir.ng.tag.model;

import java.util.List;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@DecoratedWith(StudyTagDecorator.class)
public interface StudyTagMapper {

    /**
     * Map list of @StudyTag to list of @StudyTagDTO.
     *
     * @param studyTagList
     *            list of tags
     * @return list of DTO.
     */
    List<StudyTagDTO> studyTagListToStudyTagDTOList(List<StudyTag> studyTagList);

    /**
     * Map list of @StudyTagDTO to list of @StudyTag.
     *
     * @param studyTags
     * @return
     */
    List<StudyTag> studyTagDTOListToStudyTagList(List<StudyTagDTO> studyTags);

    /**
     * Map a @StudyTag to a @StudyTagDTO.
     *
     * @param studyTag
     * @return DTO.
     */
    StudyTagDTO studyTagToStudyTagDTO(StudyTag studyTag);

    /**
     * Map a @StudyTag to a @StudyTagDTO.
     *
     * @param studyTag
     * @return DTO.
     */
    StudyTag studyTagDTOToStudyTag(StudyTagDTO studyTagDTO);
}
