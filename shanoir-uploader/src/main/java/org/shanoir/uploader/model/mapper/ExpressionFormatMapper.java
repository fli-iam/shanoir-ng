package org.shanoir.uploader.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = DatasetFileMapper.class)
public interface ExpressionFormatMapper {

    ExpressionFormatMapper INSTANCE = Mappers.getMapper(ExpressionFormatMapper.class);

    org.shanoir.ng.importer.dto.ExpressionFormat toDto(org.shanoir.ng.importer.model.ExpressionFormat modelExpressionFormat);

    org.shanoir.ng.importer.model.ExpressionFormat toModel(org.shanoir.ng.importer.dto.ExpressionFormat dtoExpressionFormat);
}