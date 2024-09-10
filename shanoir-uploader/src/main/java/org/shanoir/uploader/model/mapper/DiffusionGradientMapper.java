package org.shanoir.uploader.model.mapper;

import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface DiffusionGradientMapper {

    org.shanoir.ng.importer.dto.DiffusionGradient toDto(org.shanoir.ng.importer.model.DiffusionGradient modelDiffusionGradient);

    org.shanoir.ng.importer.model.DiffusionGradient toModel(org.shanoir.ng.importer.dto.DiffusionGradient dtoDiffusionGradient);
}