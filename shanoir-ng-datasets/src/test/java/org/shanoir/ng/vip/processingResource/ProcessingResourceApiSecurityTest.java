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

package org.shanoir.ng.vip.processingResource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.dataset.dto.DatasetForRightsProjection;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.processing.repository.DatasetProcessingRepository;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.UserRights;
import org.shanoir.ng.study.rights.ampq.RabbitMqStudyUserService;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.shanoir.ng.vip.processingResource.controller.ProcessingResourceApi;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;

/**
 * VIP downloads the inputs of an execution with the token of the user who launched it: the resource id alone is not
 * enough, the user needs CAN_EXECUTE on every dataset of the resource, as to launch the execution.
 */
@SpringBootTest
@ActiveProfiles("test")
class ProcessingResourceApiSecurityTest {

    private static final String RESOURCE = "5b2f1c6e-0d7a-4c55-9a1e-3f1f0c2b7d11";

    @Autowired
    private ProcessingResourceApi api;

    @MockitoBean
    private DatasetRepository datasetRepository;

    @MockitoBean
    private DatasetProcessingRepository processingRepository;

    @MockitoBean(name = "datasetDownloaderServiceImpl")
    private DatasetDownloaderServiceImpl datasetDownloaderService;

    @MockitoBean
    private StudyRightsService rightsService;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @MockitoBean
    private RabbitMqStudyUserService rabbitMqStudyUserService;

    @BeforeEach
    void setUp() {
        Dataset dataset = new MrDataset();
        dataset.setId(100L);
        given(datasetRepository.findByResourceIdWithDatasetFiles(RESOURCE)).willReturn(List.of(dataset));
        given(datasetRepository.findDatasetsForRights(List.of(100L))).willReturn(List.of(new DatasetForRightsProjection() {
            public Long getId() {
                return 100L;
            }

            public Long getStudyId() {
                return 1L;
            }

            public Long getCenterId() {
                return 10L;
            }

            public Set<Long> getRelatedStudiesIds() {
                return Set.of();
            }
        }));
        given(processingRepository.findTypeByResourceId(RESOURCE)).willReturn("SEGMENTATION");
    }

    private void rightOnStudy1(StudyUserRight right) {
        StudyUser member = new StudyUser();
        member.setStudyId(1L);
        member.setStudyUserRights(List.of(right));
        given(rightsService.getUserRights()).willReturn(new UserRights(List.of(member)));
    }

    private void download() throws Exception {
        api.getPath(RESOURCE, "content", "dcm", null, null, new MockHttpServletResponse());
    }

    @Test
    @WithAnonymousUser
    void anonymousCantDownloadTheInputs() {
        assertThrows(AccessDeniedException.class, this::download);
    }

    @Test
    @WithMockKeycloakUser(id = 2L, username = "user", authorities = { "ROLE_USER" })
    void aUserWithoutTheExecuteRightCantDownloadTheInputs() throws Exception {
        rightOnStudy1(StudyUserRight.CAN_SEE_ALL);

        RestServiceException e = assertThrows(RestServiceException.class, this::download);

        assertEquals(HttpStatus.FORBIDDEN.value(), e.getErrorModel().getCode());
        verify(datasetDownloaderService, never()).massiveDownload(anyString(), anyList(), any(), anyBoolean(), any(), anyBoolean(), any(), any(), anyBoolean());
    }

    @Test
    @WithMockKeycloakUser(id = 2L, username = "user", authorities = { "ROLE_USER" })
    void theUserWhoLaunchedTheExecutionDownloadsItsInputs() throws Exception {
        rightOnStudy1(StudyUserRight.CAN_EXECUTE);

        download();

        verify(datasetDownloaderService).massiveDownload(anyString(), anyList(), any(), anyBoolean(), any(), anyBoolean(), any(), any(), anyBoolean());
    }

    @Test
    @WithMockKeycloakUser(id = 3L, username = "admin", authorities = { "ROLE_ADMIN" })
    void anAdminDownloadsTheInputs() throws Exception {
        download();

        verify(datasetDownloaderService).massiveDownload(anyString(), anyList(), any(), anyBoolean(), any(), anyBoolean(), any(), any(), anyBoolean());
    }

}
