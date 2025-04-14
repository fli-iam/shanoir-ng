package org.shanoir.ng.vip.executionTemplate.repository;

import org.shanoir.ng.vip.executionTemplate.model.ExecutionTemplateParameter;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface ExecutionTemplateParameterRepository extends CrudRepository<ExecutionTemplateParameter, Long> {

    List<ExecutionTemplateParameter> findByExecutionTemplateId(Long executionTemplateId);

}