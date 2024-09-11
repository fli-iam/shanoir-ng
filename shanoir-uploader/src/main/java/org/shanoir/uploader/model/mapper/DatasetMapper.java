package org.shanoir.uploader.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {ExpressionFormatMapper.class, DatasetFileMapper.class, DiffusionGradientMapper.class})
public interface DatasetMapper {

    DatasetMapper INSTANCE = Mappers.getMapper(DatasetMapper.class);

    org.shanoir.ng.importer.dto.Dataset toDto(org.shanoir.ng.importer.model.Dataset modelDataset);

    org.shanoir.ng.importer.model.Dataset toModel(org.shanoir.ng.importer.dto.Dataset dtoDataset);
}