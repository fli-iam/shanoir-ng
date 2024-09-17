package org.shanoir.uploader.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {SerieMapper.class, DatasetMapper.class, ExpressionFormatMapper.class, DatasetFileMapper.class, DiffusionGradientMapper.class, ImageMapper.class})
public interface StudyMapper {

    StudyMapper INSTANCE = Mappers.getMapper(StudyMapper.class);

    org.shanoir.ng.importer.dto.Study toDto(org.shanoir.ng.importer.model.Study modelStudy);

    org.shanoir.ng.importer.model.Study toModel(org.shanoir.ng.importer.dto.Study dtoStudy);
}
