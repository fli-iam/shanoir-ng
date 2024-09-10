package org.shanoir.uploader.model.mapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {SerieMapper.class, DatasetMapper.class, ExpressionFormatMapper.class, DatasetFileMapper.class, DiffusionGradientMapper.class, ImageMapper.class})
public interface StudyMapper {

    org.shanoir.ng.importer.dto.Study toDto(org.shanoir.ng.importer.model.Study modelStudy);

    org.shanoir.ng.importer.model.Study toModel(org.shanoir.ng.importer.dto.Study dtoStudy);
}
