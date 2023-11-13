package org.shanoir.ng.vip.monitoring.schedule;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.SecurityException;

public interface ExecutionStatusMonitorService {

    void startMonitoringJob(String identifier) throws EntityNotFoundException, SecurityException;
}
