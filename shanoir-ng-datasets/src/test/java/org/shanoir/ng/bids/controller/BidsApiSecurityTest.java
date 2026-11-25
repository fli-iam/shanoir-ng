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

package org.shanoir.ng.bids.controller;

import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.bids.service.BIDSService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.study.rights.ampq.RabbitMqStudyUserService;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;

/**
 * Generating the BIDS structure of a study is costly work on its data: it needs the same right as refreshing it.
 */
@SpringBootTest
@ActiveProfiles("test")
class BidsApiSecurityTest {

    @Autowired
    private BidsApi api;

    @MockitoBean
    private BIDSService bidsService;

    @MockitoBean
    private StudyRightsService rightsService;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @MockitoBean
    private RabbitMqStudyUserService rabbitMqStudyUserService;

    @Test
    @WithAnonymousUser
    void anonymousCantGenerateBids() throws ShanoirException {
        assertAccessDenied(api::generateBIDSByStudyId, 1L, "study");
    }

    @Test
    @WithMockKeycloakUser(id = 2L, username = "user", authorities = { "ROLE_USER" })
    void aUserWhoCantSeeTheStudyCantGenerateItsBids() throws ShanoirException {
        given(rightsService.hasRightOnStudy(Mockito.anyLong(), Mockito.anyString())).willReturn(false);
        assertAccessDenied(api::generateBIDSByStudyId, 1L, "study");
    }

    @Test
    @WithMockKeycloakUser(id = 2L, username = "user", authorities = { "ROLE_USER" })
    void aUserWhoCanSeeTheStudyGeneratesItsBids() throws ShanoirException {
        given(rightsService.hasRightOnStudy(1L, StudyUserRight.CAN_SEE_ALL.name())).willReturn(true);
        assertAccessAuthorized(api::generateBIDSByStudyId, 1L, "study");
    }

    @Test
    @WithMockKeycloakUser(id = 3L, username = "admin", authorities = { "ROLE_ADMIN" })
    void anAdminGeneratesTheBidsOfAnyStudy() throws ShanoirException {
        assertAccessAuthorized(api::generateBIDSByStudyId, 1L, "study");
    }

}
