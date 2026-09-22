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

package org.shanoir.ng.shared.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.shanoir.ng.shared.model.Study;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface StudyMapper {

    ////// Entity to DTO

    @Named("id")
    default Long studytoLongId(Study study) {
        if (study == null) {
            return null;
        }
        return study.getId();
    }

    @Named("id")
    default List<Long> studysToLongIds(List<Study> studys) {
        if (studys == null) {
            return null;
        }
        return studys.stream().filter(Objects::nonNull).map(Study::getId).collect(Collectors.toList());
    }

    ////// DTO to entity

    @Named("idOnly")
    default Study idToStudy(Long id) {
        if (id == null) {
            return null;
        }
        Study  study = new Study();
        study.setId(id);
        return study;
    }

    @Named("idOnly")
    default List<Study> idsListToStudyList(List<Long> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream().filter(Objects::nonNull).map(id -> {
            Study study = new Study();
            study.setId(id);
            return study;
        }).collect(Collectors.toList());
    }
}
