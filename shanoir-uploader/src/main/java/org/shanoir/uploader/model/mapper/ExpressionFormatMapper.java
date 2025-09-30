package org.shanoir.uploader.model.mapper;

import org.mapstruct.Mapper;

@Mapper(uses = DatasetFileMapper.class)
public interface ExpressionFormatMapper {

    org.shanoir.ng.importer.dto.ExpressionFormat toDto(org.shanoir.ng.importer.model.ExpressionFormat modelExpressionFormat);

    org.shanoir.ng.importer.model.ExpressionFormat toModel(org.shanoir.ng.importer.dto.ExpressionFormat dtoExpressionFormat);
}
