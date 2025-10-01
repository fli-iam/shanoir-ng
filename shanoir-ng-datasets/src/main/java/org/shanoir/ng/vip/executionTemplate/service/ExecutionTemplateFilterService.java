package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateFilterDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateFilter;

public interface ExecutionTemplateFilterService {

    ExecutionTemplateFilter update(ExecutionTemplateFilterDTO executionTemplateFilterDTO);

    ExecutionTemplateFilter prepareNewEntity(ExecutionTemplateFilterDTO executionTemplateFilterDTO);
}
