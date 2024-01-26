package org.shanoir.ng.vip.monitoring.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.monitoring.service.ExecutionMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class ExecutionStatusMonitorRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionStatusMonitorRunner.class);

    @Autowired
    private ExecutionMonitoringService execMonitoringSrv;

    @Autowired
    private ExecutionStatusMonitorService execMonitor;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;




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

        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");

        List<ExecutionMonitoring> runningMonitorings = execMonitoringSrv.findAllRunning();

        for(ExecutionMonitoring monitoring : runningMonitorings){

            List<ShanoirEvent> events;

            String eventsAsString = (String) this.rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXECUTION_MONITORING_TASK, monitoring.getId());

            if (eventsAsString == null || eventsAsString.isEmpty()) {
                LOG.error("No [{}] type event found for object id [{}]", ShanoirEventType.EXECUTION_MONITORING_EVENT, monitoring.getId());
                continue;
            }
            events = objectMapper.readValue(eventsAsString, new TypeReference<List<ShanoirEvent>>() {});

            for(ShanoirEvent event : events){
                execMonitor.startMonitoringJob(monitoring, event);
                LOG.info("Monitoring of VIP execution [{}] resumed", monitoring.getName());
            }

        }

    }
}
