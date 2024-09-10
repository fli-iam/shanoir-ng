package org.shanoir.uploader.model.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ExpressionFormatMapper.class, DatasetFileMapper.class, DiffusionGradientMapper.class})
public interface DatasetMapper {

    org.shanoir.ng.importer.dto.Dataset toDto(org.shanoir.ng.importer.model.Dataset modelDataset);

    org.shanoir.ng.importer.model.Dataset toModel(org.shanoir.ng.importer.dto.Dataset dtoDataset);
}