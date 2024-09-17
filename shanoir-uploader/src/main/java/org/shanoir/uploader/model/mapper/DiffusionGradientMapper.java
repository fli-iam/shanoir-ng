package org.shanoir.uploader.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper()
public interface DiffusionGradientMapper {

    //DiffusionGradientMapper INSTANCE = Mappers.getMapper(DiffusionGradientMapper.class);

    @Mapping(target = "mrProtocol", ignore = true)
    @Mapping(target = "mrDataset", ignore = true)
    @Mapping(target = "id", ignore = true)
    org.shanoir.ng.shared.model.DiffusionGradient toDto(org.shanoir.ng.importer.model.DiffusionGradient modelDiffusionGradient);

    org.shanoir.ng.importer.model.DiffusionGradient toModel(org.shanoir.ng.shared.model.DiffusionGradient dtoDiffusionGradient);
}