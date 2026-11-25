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

package org.shanoir.ng.processing.controler;

import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.processing.service.ProcessingDownloaderService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.study.rights.ampq.RabbitMqStudyUserService;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;

import tools.jackson.databind.node.JsonNodeFactory;

/**
 * The complex massive download needs a Shanoir role; the datasets it lists are then filtered by the user's rights
 * (see ProcessingDownloaderComplexDownloadTest).
 */
@SpringBootTest
@ActiveProfiles("test")
class DatasetProcessingApiSecurityTest {

    @Autowired
    private DatasetProcessingApi api;

    @MockitoBean
    private ProcessingDownloaderService processingDownloaderService;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @MockitoBean
    private RabbitMqStudyUserService rabbitMqStudyUserService;

    @Test
    @WithAnonymousUser
    void anonymousCantPrepareAComplexDownload() throws ShanoirException {
        assertAccessDenied(api::complexMassiveDownload, JsonNodeFactory.instance.objectNode());
    }

    @Test
    @WithMockKeycloakUser(id = 2L, username = "user", authorities = { "ROLE_USER" })
    void aUserPreparesAComplexDownload() throws ShanoirException {
        assertAccessAuthorized(api::complexMassiveDownload, JsonNodeFactory.instance.objectNode());
    }

}
