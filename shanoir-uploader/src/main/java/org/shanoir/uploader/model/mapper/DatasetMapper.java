package org.shanoir.uploader.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {SerieMapper.class, ExpressionFormatMapper.class, DatasetFileMapper.class, DiffusionGradientMapper.class})
public interface DatasetMapper {

    @Mapping(target = "bValues", ignore = true)
    @Mapping(target = "bVectors", ignore = true)
    org.shanoir.ng.importer.dto.Dataset toDto(org.shanoir.ng.importer.model.Dataset modelDataset);

    org.shanoir.ng.importer.model.Dataset toModel(org.shanoir.ng.importer.dto.Dataset dtoDataset);
}