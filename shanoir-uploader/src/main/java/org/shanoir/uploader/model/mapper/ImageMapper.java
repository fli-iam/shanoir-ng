package org.shanoir.uploader.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    @Mappings({
		@Mapping(target = "acquisitionNumber", source = "acquisitionNumber"),
        @Mapping(target = "echoTimes", source = "echoTimes")
	})
    org.shanoir.ng.importer.dto.Image toDto(org.shanoir.ng.importer.model.Image modelImage);

    org.shanoir.ng.importer.model.Image toModel(org.shanoir.ng.importer.dto.Image dtoImage);
}