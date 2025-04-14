package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.vip.executionTemplate.dto.ExecutionTemplateParameterDTO;
import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateParameter;
import org.shanoir.ng.vip.executionTemplate.repository.ExecutionTemplateParameterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecutionTemplateParameterServiceImpl implements ExecutionTemplateParameterService {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionTemplateParameterServiceImpl.class);

    @Autowired
    private ExecutionTemplateParameterRepository repository;

    @Override
    public List<ExecutionTemplateParameter> findByExecutionTemplateId(Long executionTemplateId) {
        return repository.findByExecutionTemplateId(executionTemplateId);
    }

    @Override
    public ExecutionTemplateParameter update(Long executionTemplateParameterId, ExecutionTemplateParameterDTO executionTemplateParameter) {
        ExecutionTemplateParameter dbExecution = this.repository.findById(executionTemplateParameterId).orElse(null);

        if (dbExecution == null) {
            return null;
        }

        // Update updatable fields only
        dbExecution.setName(executionTemplateParameter.getName());

        return this.repository.save(dbExecution);
    }

    @Override
    public ExecutionTemplateParameter findById(Long executionTemplateParameterId) {
        return this.repository.findById(executionTemplateParameterId).orElse(null);
    }

    @Override
    public void delete(ExecutionTemplateParameter executionTemplateParameter) {
        this.repository.delete(executionTemplateParameter);
    }

    @Override
    public ExecutionTemplateParameter save(ExecutionTemplateParameter executionTemplateParameter) {
        return this.repository.save(executionTemplateParameter);
    }
}