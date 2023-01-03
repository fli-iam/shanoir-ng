package org.shanoir.ng.processing.carmin.schedule;

import org.shanoir.ng.shared.exception.EntityNotFoundException;

public interface ExecutionStatusMonitorService {

    void startJob(String identifier) throws EntityNotFoundException;
}
