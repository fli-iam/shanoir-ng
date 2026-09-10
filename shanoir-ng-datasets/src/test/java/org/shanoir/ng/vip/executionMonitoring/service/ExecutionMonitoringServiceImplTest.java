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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.shanoir.ng.vip.execution.dto.VipExecutionDTO;
import org.shanoir.ng.vip.execution.service.ExecutionServiceImpl;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionMonitoring;
import org.shanoir.ng.vip.executionMonitoring.model.ExecutionStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import reactor.core.publisher.Mono;

/**
 * Tests that the shared VIP monitoring loop reports each execution to the user who launched it, whoever happens
 * to be running the loop at that moment.
 *
 * See issue #3813: the loop is started by the first user to queue an execution and its thread keeps that user's
 * security context, so every execution queued afterwards used to be reported to that first user.
 */
@ExtendWith(MockitoExtension.class)
public class ExecutionMonitoringServiceImplTest {

    private static final Long USER_A = 11L;

    private static final Long USER_B = 22L;

    private static final Long LOOP_RUNNER = 99L;

    @Mock
    private ShanoirEventService eventService;

    @Mock
    private ExecutionServiceImpl executionService;

    @Mock
    private ExecutionMonitoringServiceImpl emProxyService;

    private ExecutionMonitoringServiceImpl service;

    @BeforeEach
    public void setUp() {
        service = new ExecutionMonitoringServiceImpl();
        ReflectionTestUtils.setField(service, "eventService", eventService);
        ReflectionTestUtils.setField(service, "executionService", executionService);
        ReflectionTestUtils.setField(service, "emProxyService", emProxyService);
        ReflectionTestUtils.setField(service, "sleepTime", 0L);
        ReflectionTestUtils.setField(service, "jobPollDelay", 0L);
    }

    @AfterEach
    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldReportEachExecutionToItsOwnOwner() throws Exception {
        ExecutionMonitoring executionOfA = executionMonitoring(1L, "exec-of-a");
        ExecutionMonitoring executionOfB = executionMonitoring(2L, "exec-of-b");

        given(executionService.getExecutionAsServiceAccount(anyInt(), anyString()))
                .willReturn(Mono.just(finishedExecution()));

        queueAs(USER_A, executionOfA);
        queueAs(USER_B, executionOfB);

        runLoopAs(LOOP_RUNNER);

        assertEquals(USER_A, publishedEventFor(executionOfA).getUserId());
        assertEquals(USER_B, publishedEventFor(executionOfB).getUserId());
    }

    @Test
    public void shouldRestoreTheCallingContextAfterEachPolledExecution() throws Exception {
        given(executionService.getExecutionAsServiceAccount(anyInt(), anyString()))
                .willReturn(Mono.just(finishedExecution()));

        queueAs(USER_A, executionMonitoring(1L, "exec-of-a"));

        runLoopAs(LOOP_RUNNER);

        assertEquals(LOOP_RUNNER, KeycloakUtil.getTokenUserId());
    }

    @Test
    public void shouldStartANewLoopOnceTheQueueHasBeenDrained() throws Exception {
        given(executionService.getExecutionAsServiceAccount(anyInt(), anyString()))
                .willReturn(Mono.just(finishedExecution()));

        queueAs(USER_A, executionMonitoring(1L, "exec-of-a"));
        runLoopAs(LOOP_RUNNER);

        queueAs(USER_B, executionMonitoring(2L, "exec-of-b"));

        verify(emProxyService, times(2)).monitoringLoop();
    }

    private void queueAs(Long userId, ExecutionMonitoring monitoring) throws Exception {
        SecurityContextUtil.initAuthenticationContext(KeycloakUtil.ROLE_EXPERT, userId);
        service.startMonitoringJob(monitoring, null, 1);
    }

    /**
     * Run the monitoring loop synchronously, under a user unrelated to the queued executions. In production this
     * is the user who queued the very first execution, kept on the loop thread by the async executor.
     */
    private void runLoopAs(Long userId) {
        SecurityContextUtil.initAuthenticationContext(KeycloakUtil.ROLE_EXPERT, userId);
        service.monitoringLoop();
    }

    private ShanoirEvent publishedEventFor(ExecutionMonitoring monitoring) {
        ArgumentCaptor<ShanoirEvent> captor = ArgumentCaptor.forClass(ShanoirEvent.class);
        verify(eventService, atLeastOnce()).publishEvent(captor.capture());

        List<ShanoirEvent> events = captor.getAllValues().stream()
                .filter(event -> monitoring.getId().toString().equals(event.getObjectId()))
                .toList();
        assertEquals(1, events.size(), "expected exactly one event for execution " + monitoring.getName());
        return events.getFirst();
    }

    private ExecutionMonitoring executionMonitoring(Long id, String name) {
        ExecutionMonitoring monitoring = new ExecutionMonitoring();
        monitoring.setId(id);
        monitoring.setName(name);
        monitoring.setIdentifier("vip-" + id);
        monitoring.setPipelineIdentifier("pipeline/1.0");
        monitoring.setStatus(ExecutionStatus.RUNNING);
        return monitoring;
    }

    private VipExecutionDTO finishedExecution() {
        VipExecutionDTO dto = new VipExecutionDTO();
        dto.setStatus(ExecutionStatus.FINISHED);
        return dto;
    }
}
