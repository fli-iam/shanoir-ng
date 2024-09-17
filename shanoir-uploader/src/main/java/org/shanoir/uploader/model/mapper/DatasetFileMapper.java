package org.shanoir.uploader.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper()
public interface DatasetFileMapper {

    //DatasetFileMapper INSTANCE = Mappers.getMapper(DatasetFileMapper.class);

    @Mapping(target = "echoNumbers", ignore = true)
    org.shanoir.ng.importer.dto.DatasetFile toDto(org.shanoir.ng.importer.model.DatasetFile modelDatasetFile);

    org.shanoir.ng.importer.model.DatasetFile toModel(org.shanoir.ng.importer.dto.DatasetFile dtoDatasetFile);
}