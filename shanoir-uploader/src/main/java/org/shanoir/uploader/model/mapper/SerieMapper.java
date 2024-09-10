package org.shanoir.uploader.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {DatasetMapper.class, ExpressionFormatMapper.class, DatasetFileMapper.class, DiffusionGradientMapper.class, ImageMapper.class})
public interface SerieMapper {

    @Mappings({ @Mapping(target = "ignored", ignore = true), @Mapping(target = "erroneous", ignore = true),
             @Mapping(target = "errorMessage", ignore = true), @Mapping(target = "institution", ignore = true)})
    org.shanoir.ng.importer.dto.Serie toDto(org.shanoir.ng.importer.model.Serie modelSerie);

    org.shanoir.ng.importer.model.Serie toModel(org.shanoir.ng.importer.dto.Serie dtoSerie);
}