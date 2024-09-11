package org.shanoir.uploader.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface DiffusionGradientMapper {

    DiffusionGradientMapper INSTANCE = Mappers.getMapper(DiffusionGradientMapper.class);

    org.shanoir.ng.importer.dto.DiffusionGradient toDto(org.shanoir.ng.importer.model.DiffusionGradient modelDiffusionGradient);

    org.shanoir.ng.importer.model.DiffusionGradient toModel(org.shanoir.ng.importer.dto.DiffusionGradient dtoDiffusionGradient);
}