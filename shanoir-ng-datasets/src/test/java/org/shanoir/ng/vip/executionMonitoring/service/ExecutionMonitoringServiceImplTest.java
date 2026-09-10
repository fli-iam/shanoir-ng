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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;
import org.shanoir.ng.vip.executionMonitoring.repository.ExecutionMonitoringRepository;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests for the execution-timeout handling in ExecutionMonitoringServiceImpl: an execution that
 * VIP keeps reporting as Running past the configured timeout must be marked as failed instead of
 * being monitored forever (see #3544).
 *
 * @author afragkia
 */
@ExtendWith(MockitoExtension.class)
public class ExecutionMonitoringServiceImplTest {

    private static final long TIMEOUT_MS = 900000L; // 15 minutes

    @Mock
    private ExecutionMonitoringRepository repository;

    @Mock
    private ShanoirEventService eventService;

    @InjectMocks
    private ExecutionMonitoringServiceImpl service;

    private ExecutionMonitoring runningMonitoring() {
        ExecutionMonitoring monitoring = new ExecutionMonitoring();
        monitoring.setId(1L);
        monitoring.setName("deface_pipeline");
        monitoring.setPipelineIdentifier("deface/1.0");
        monitoring.setStatus(ExecutionStatus.RUNNING);
        return monitoring;
    }

    @Test
    public void isExecutionTimedOutReturnsTrueWhenStartTimeIsOlderThanTimeout() {
        ReflectionTestUtils.setField(service, "executionTimeoutMs", TIMEOUT_MS);
        long staleStart = System.currentTimeMillis() - (TIMEOUT_MS + 60000L);

        assertTrue(service.isExecutionTimedOut(staleStart));
    }

    @Test
    public void isExecutionTimedOutReturnsFalseForRecentStartTime() {
        ReflectionTestUtils.setField(service, "executionTimeoutMs", TIMEOUT_MS);
        long recentStart = System.currentTimeMillis() - 1000L;

        assertFalse(service.isExecutionTimedOut(recentStart));
    }

    @Test
    public void isExecutionTimedOutReturnsFalseForNullStartTime() {
        ReflectionTestUtils.setField(service, "executionTimeoutMs", TIMEOUT_MS);

        assertFalse(service.isExecutionTimedOut(null));
    }

    @Test
    public void processTimedOutJobMarksExecutionFailedAndSetsEventInError() throws EntityNotFoundException {
        ExecutionMonitoring monitoring = runningMonitoring();
        ShanoirEvent event = new ShanoirEvent(ShanoirEventType.EXECUTION_MONITORING_EVENT,
                monitoring.getId().toString(), 42L, "running", ShanoirEvent.IN_PROGRESS, 0.5f);

        when(repository.findById(monitoring.getId())).thenReturn(Optional.of(monitoring));
        when(repository.save(any(ExecutionMonitoring.class))).thenReturn(monitoring);

        service.processTimedOutJob(monitoring, event);

        assertEquals(ExecutionStatus.EXECUTION_FAILED, monitoring.getStatus());
        verify(repository).save(monitoring);

        assertEquals(ShanoirEvent.ERROR, event.getStatus());
        assertEquals(1f, event.getProgress());
        verify(eventService).publishEvent(event);
    }

}
