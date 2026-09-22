/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.vip.executionMonitoring.dto.mapper;

import org.mapstruct.*;
import org.shanoir.ng.processing.dto.mapper.DatasetProcessingMapper;
import org.shanoir.ng.processing.dto.mapper.DatasetProcessingMappingConfig;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.executionMonitoring.dto.ExecutionMonitoringDTO;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", config = DatasetProcessingMappingConfig.class, uses = { DatasetProcessingMapper.class })
public interface ExecutionMonitoringMapper {

    ////// Entity to DTO

    @Named("id")
    default Long monitoringToLongId(ExecutionMonitoring monitoring) {
        if (monitoring == null) {
            return null;
        }
        return monitoring.getId();
    }

    @Named("id")
    default List<Long> monitoringListToLongIds(List<ExecutionMonitoring> monitorings) {
        if (monitorings == null) {
            return null;
        }
        return monitorings.stream().filter(Objects::nonNull).map(ExecutionMonitoring::getId).collect(Collectors.toList());
    }

    @Named("idOnly")
    default ExecutionMonitoringDTO monitoringToId(ExecutionMonitoring monitoring) {
        if (monitoring == null) {
            return null;
        }
        ExecutionMonitoringDTO dto = new ExecutionMonitoringDTO();
        dto.setId(monitoring.getId());
        return dto;
    }

    @Named("idOnly")
    default List<ExecutionMonitoringDTO> monitoringListToIds(List<ExecutionMonitoring> monitorings) {
        if (monitorings == null) {
            return null;
        }
        return monitorings.stream().filter(Objects::nonNull).map(proc -> {
            ExecutionMonitoringDTO dto = new ExecutionMonitoringDTO();
            dto.setId(proc.getId());
            return dto;
        }).collect(Collectors.toList());
    }

    //Single entity

    /**
     * Some context of usage :
     */
    @Named("nullRelations")
    @InheritConfiguration(name = "processingToProcessingDTOWithNullRelationsPrototype")
    ExecutionMonitoringDTO executionMonitoringToExecutionMonitoringDTOWithNullRelations(ExecutionMonitoring processing);

    /**
     * Some context of usage :
     */
    @Named("idRelations")
    @InheritConfiguration(name = "processingToProcessingDTOWithIdRelationsPrototype")
    ExecutionMonitoringDTO executionMonitoringToExecutionMonitoringDTOWithIdRelations(ExecutionMonitoring processing);

    //Entity list

    /**
     * Some context of usage :
     */
    @IterableMapping(qualifiedByName = "nullRelations")
    List<ExecutionMonitoringDTO> executionMonitoringsToExecutionMonitoringDTOListWithNullRelations(List<ExecutionMonitoring> datasetProcessings);

    /**
     * Some context of usage :
     */
    @IterableMapping(qualifiedByName = "idRelations")
    List<ExecutionMonitoringDTO> executionMonitoringsToExecutionMonitoringDTOListWithIdRelations(List<ExecutionMonitoring> datasetProcessings);
}
