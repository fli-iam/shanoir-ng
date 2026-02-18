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

package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateFilterDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplate;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateFilter;
import org.shanoir.ng.vip.executionTemplate.repository.ExecutionTemplateFilterRepository;
import org.shanoir.ng.vip.executionTemplate.repository.ExecutionTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExecutionTemplateFilterServiceImpl implements ExecutionTemplateFilterService {

    @Autowired
    private ExecutionTemplateFilterRepository repository;

    @Autowired
    private ExecutionTemplateRepository templateRepository;

    public ExecutionTemplateFilter update(ExecutionTemplateFilterDTO templateFilter) {
        ExecutionTemplateFilter dbTemplateFilter = this.repository.findById(templateFilter.getId()).orElse(null);

        if (dbTemplateFilter == null) {
            return null;
        }

        dbTemplateFilter.setFieldName(templateFilter.getFieldName());
        dbTemplateFilter.setExecutionTemplate(templateRepository.findById(templateFilter.getExecutionTemplateId()).orElse(null));
        dbTemplateFilter.setExcluded(templateFilter.isExcluded());
        dbTemplateFilter.setComparedRegex(templateFilter.getComparedRegex());
        dbTemplateFilter.setIdentifier(templateFilter.getIdentifier());

        return repository.save(dbTemplateFilter);
    }

    public ExecutionTemplateFilter prepareNewEntity(ExecutionTemplateFilterDTO executionTemplateFilterDTO) {
        ExecutionTemplate executionTemplate = templateRepository.findById(executionTemplateFilterDTO.getExecutionTemplateId()).orElse(null);
        return ExecutionTemplateFilterDTO.toEntity(executionTemplateFilterDTO, executionTemplate);
    }
}
