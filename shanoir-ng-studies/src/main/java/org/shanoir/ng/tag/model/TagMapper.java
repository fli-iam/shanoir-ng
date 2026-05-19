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
@DecoratedWith(TagDecorator.class)
public interface TagMapper {

    /**
     * Map list of @Tag to list of @TagDTO.
     *
     * @param tagList
     *            list of tags
     * @return list of DTO.
     */
    List<TagDTO> tagListToTagDTOList(List<Tag> tagList);

    /**
     * Map a @Tag to a @TagDTO.
     *
     * @param tag
     * @return DTO.
     */
    TagDTO tagToTagDTO(Tag tag);
}
