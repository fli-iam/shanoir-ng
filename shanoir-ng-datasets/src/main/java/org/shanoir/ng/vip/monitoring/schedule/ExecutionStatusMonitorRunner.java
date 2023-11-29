package org.shanoir.ng.vip.monitoring.schedule;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.vip.monitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.monitoring.service.ExecutionMonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExecutionStatusMonitorRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutionStatusMonitorRunner.class);

    @Autowired
    private ExecutionMonitoringService execMonitoringSrv;

    @Autowired
    private ExecutionStatusMonitor execMonitor;


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
    public void run(ApplicationArguments args) throws EntityNotFoundException, SecurityException {

        List<ExecutionMonitoring> runningMonitorings = execMonitoringSrv.findAllRunning();

        for(ExecutionMonitoring monitoring : runningMonitorings){
            execMonitor.startMonitoringJob(monitoring.getIdentifier());
            LOG.info("Monitoring of VIP execution [{}] resumed", monitoring.getName());
        }

    }
}
