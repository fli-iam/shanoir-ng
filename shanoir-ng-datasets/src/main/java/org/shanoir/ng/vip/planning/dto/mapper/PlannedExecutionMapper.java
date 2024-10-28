package org.shanoir.ng.vip.planning.dto.mapper;

import org.mapstruct.*;
import org.shanoir.ng.vip.planning.dto.PlannedExecutionDTO;
import org.shanoir.ng.vip.planning.model.PlannedExecution;

import java.util.List;

@Mapper(componentModel = "spring", uses = { })
public interface PlannedExecutionMapper {

    @Named("PlannedExecutionToDTO")
    @Mappings({
            @Mapping(target = "study", source = "study.id"),
    })    PlannedExecutionDTO PlannedExecutionToDTO(PlannedExecution plannedExecution);

    @IterableMapping(qualifiedByName = "PlannedExecutionToDTO")
    List<PlannedExecutionDTO> PlannedExecutionsToDTOs(List<PlannedExecution> plannedExecutions);
}