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