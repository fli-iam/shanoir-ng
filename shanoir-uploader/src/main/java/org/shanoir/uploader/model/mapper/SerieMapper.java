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

package org.shanoir.uploader.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {DatasetMapper.class, ExpressionFormatMapper.class, DatasetFileMapper.class, DiffusionGradientMapper.class, ImageMapper.class})
public interface SerieMapper {

    org.shanoir.ng.importer.dto.Serie toDto(org.shanoir.ng.importer.model.Serie modelSerie);

    @Mapping(target = "ignored", ignore = true)
    @Mapping(target = "erroneous", ignore = true)
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "institution", ignore = true)
    @Mapping(target = "instances", ignore = true)
    org.shanoir.ng.importer.model.Serie toModel(org.shanoir.ng.importer.dto.Serie dtoSerie);
}
