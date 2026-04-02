package org.shanoir.ng.vip.executionTemplate.service;

import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.model.MiscellaneousParameter;
import org.shanoir.ng.shared.repository.MiscellaneousParameterRepository;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.shanoir.ng.vip.executionMonitoring.service.ExecutionMonitoringResumptionRunner;
import org.shanoir.ng.vip.executionTemplate.model.PlannedExecution;
import org.shanoir.ng.vip.executionTemplate.repository.PlannedExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Profile("!test")
public class ExecutionTemplateRunner implements ApplicationRunner {

    /**
     * At the end of the Spring context loading (on application startup),
     * scan the examinations having an Id greater than miscellaneous_parameter.name = import_exec_count in DB
     * and register execution template when relevant
     * then launch
     *
     * @param args
     * @throws EntityNotFoundException
     * @throws SecurityException
     */

    @Autowired
    private PlannedExecutionRepository plannedExecutionRepository;

    @Autowired
    private MiscellaneousParameterRepository miscelleneousParamRepository;

    @Autowired
    private PlannedExecutionService plannedExecutionService;

    @Autowired
    private DatasetAcquisitionRepository acquisitionRepository;

    @Autowired
    private ExecutionTemplateService templateService;

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionTemplateRunner.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");

            //Get counter for exec to import
            MiscellaneousParameter importExecParam = miscelleneousParamRepository.findById("import_exec_count").get();
            long importExecCount = Long.parseLong(importExecParam.getValue());

            //Get new acquisitions
            List<DatasetAcquisition> newAcquisitions = acquisitionRepository.findByIdGreaterThan(importExecCount);
            if(newAcquisitions.isEmpty()) {
                LOG.info("No acquisitions to check for executions following import.");
                return;
            }

            //Set new counter value
            importExecParam.setValue(newAcquisitions.stream().map(DatasetAcquisition::getId).reduce(Long::max).get().toString());
            miscelleneousParamRepository.save(importExecParam);

            //Check execution template for auto-exec for new acquisitions
            newAcquisitions.stream()
                    .collect(Collectors.groupingBy(a -> a.getExamination().getId()))
                    .values()
                    .forEach(group -> templateService.createExecutionsFromExecutionTemplates(group));

            LOG.info("New acquisitions checked for executions following import.");
        } catch (Exception e){
            LOG.error("Error while checking execution templates for new acquisitions", e);
            LOG.error(e.getMessage(), e);
        }

        try {
            // Planned executions launching/resumption
            Map<Long, List<Long>> createdAcquisitionsPerTemplateId = StreamSupport.stream(plannedExecutionRepository.findAll().spliterator(), false)
                    .collect(Collectors.groupingBy(
                            PlannedExecution::getTemplateId,
                            Collectors.mapping(PlannedExecution::getAcquisitionId, Collectors.toList())
                    ));
            if (!createdAcquisitionsPerTemplateId.isEmpty()) {
                plannedExecutionService.applyExecution(createdAcquisitionsPerTemplateId);
            }
        } catch (Exception e) {
            LOG.error("Error while running execution templates for new acquisitions", e);
            LOG.error(e.getMessage(), e);
        }
    }
}
