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

package org.shanoir.ng.preclinical.bruker;

import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

/**
 * The Bruker upload is the first step of an import, before the study is chosen:
 * the user must be able to import in at least one study, like for the uploads of ms import.
 *
 * The calls send no file: an authorized call stops at the missing file, without converting anything.
 */
@SpringBootTest
@ActiveProfiles("test")
public class BrukerApiSecurityTest {

    private static final long USER_ID = 2L;

    @Autowired
    private BrukerApiController controller;

    @MockitoBean
    private StudyUserRightsRepository studyUserRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    @WithAnonymousUser
    public void testAsAnonymous() throws ShanoirException {
        assertAccessDenied(controller::uploadBrukerFile, new MultipartFile[0]);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithoutStudy() throws ShanoirException {
        assertAccessDenied(controller::uploadBrukerFile, new MultipartFile[0]);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_EXPERT" })
    public void testAsExpertWithoutImportRight() throws ShanoirException {
        givenRights(StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_ADMINISTRATE);
        assertAccessDenied(controller::uploadBrukerFile, new MultipartFile[0]);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithImportRight() throws ShanoirException {
        givenRights(StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT);
        assertAccessAuthorized(controller::uploadBrukerFile, new MultipartFile[0]);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_ADMIN" })
    public void testAsAdmin() throws ShanoirException {
        assertAccessAuthorized(controller::uploadBrukerFile, new MultipartFile[0]);
    }

    private void givenRights(StudyUserRight... rights) {
        StudyUser studyUser = new StudyUser();
        studyUser.setUserId(USER_ID);
        studyUser.setStudyId(1L);
        studyUser.setStudyUserRights(Arrays.asList(rights));
        studyUser.setConfirmed(true);
        given(studyUserRepository.findByUserId(USER_ID)).willReturn(List.of(studyUser));
    }

}
