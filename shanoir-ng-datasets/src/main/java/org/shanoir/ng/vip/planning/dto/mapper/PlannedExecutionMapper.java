package org.shanoir.ng.vip.planning.dto.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.shanoir.ng.vip.planning.dto.PlannedExecutionDTO;
import org.shanoir.ng.vip.planning.model.PlannedExecution;

import java.util.List;

@Mapper(componentModel = "spring", uses = { })
public interface PlannedExecutionMapper {

    @Named("PlannedExecutionToDTO")
    @Mappings({
    })    PlannedExecutionDTO PlannedExecutionToDTO(PlannedExecution plannedExecution);

    @IterableMapping(qualifiedByName = "PlannedExecutionToDTO")
    List<PlannedExecutionDTO> PlannedExecutionsToDTOs(List<PlannedExecution> plannedExecutions);
}