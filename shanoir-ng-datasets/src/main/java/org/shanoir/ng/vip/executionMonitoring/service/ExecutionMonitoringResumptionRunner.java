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

package org.shanoir.ng.vip.executionMonitoring.service;

import java.util.List;

import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.shanoir.ng.vip.execution.service.ExecutionService;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.executionMonitoring.repository.ExecutionMonitoringRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Profile("!test")
public class ExecutionMonitoringResumptionRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionMonitoringResumptionRunner.class);

    @Autowired
    private ExecutionMonitoringService executionMonitoringService;

    @Autowired
    private ExecutionService executionService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExecutionMonitoringRepository executionMonitoringRepository;

    /**
     * At the end of the Spring context loading (on application startup),
     * restart status monitoring of VIP execution monitoring with a status "Running" in database
     * These are executions that were still running on last application shutdown
     *
     * @param args
     * @throws EntityNotFoundException
     * @throws SecurityException
     */
    @Override
    public void run(ApplicationArguments args) throws EntityNotFoundException, SecurityException, JsonProcessingException {
        try {
            SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
            List<ExecutionMonitoring> runningMonitorings = executionMonitoringRepository.findByStatus(ExecutionStatus.RUNNING);
            for (ExecutionMonitoring monitoring : runningMonitorings) {
                List<ShanoirEvent> events;
                String eventsAsString = (String) rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXECUTION_MONITORING_TASK, monitoring.getId());
                if (eventsAsString == null || eventsAsString.isEmpty()) {
                    LOG.error("No [{}] type event found for object id [{}]", ShanoirEventType.EXECUTION_MONITORING_EVENT, monitoring.getId());
                    continue;
                }
                events = objectMapper.readValue(eventsAsString, new TypeReference<List<ShanoirEvent>>() {
                });
                for (ShanoirEvent event : events) {
                    try {
                        executionService.getExecutionAsServiceAccount(1, monitoring.getIdentifier()).block();
                        executionMonitoringService.startMonitoringJob(monitoring, event);
                        LOG.info("Monitoring of VIP execution [{}] resumed", monitoring.getName());
                    } catch (Exception e) {
                        LOG.error("Monitoring resumption of VIP execution [" + monitoring.getName() + "," + monitoring.getIdentifier() + "] failed.");
                    }
                }
            }
        } catch (Exception ignored) {
            //Try-catch is only for dodging container shutdown if exception is raised (due to @Component state)
        }
    }
}
