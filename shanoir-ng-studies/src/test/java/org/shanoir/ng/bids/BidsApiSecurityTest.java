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

package org.shanoir.ng.bids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.bids.controller.BidsApi;
import org.shanoir.ng.bids.service.BIDSService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.utils.ModelsUtil;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * The participants.tsv endpoint exposes subject ids, ages and sexes of any study:
 * admins only, whatever the user's rights on the study.
 */
@SpringBootTest
@ActiveProfiles("test")
public class BidsApiSecurityTest {

    private static final long LOGGED_USER_ID = 2L;
    private static final String LOGGED_USER_USERNAME = "logged";
    private static final long STUDY_ID = 1L;

    @Autowired
    private BidsApi api;

    @MockitoBean
    private BIDSService bidsService;

    @MockitoBean
    private StudyRepository studyRepository;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @Test
    @WithAnonymousUser
    public void testAsAnonymous() throws ShanoirException {
        assertAccessDenied(api::generateParticipantsTsvByStudyId, STUDY_ID);
    }

    @Test
    @WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
    public void testAsUser() throws ShanoirException {
        testWithStudyRights();
    }

    @Test
    @WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
    public void testAsExpert() throws ShanoirException {
        testWithStudyRights();
    }

    @Test
    @WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
    public void testAsAdmin() throws ShanoirException {
        given(studyRepository.findById(STUDY_ID)).willReturn(Optional.of(buildStudyMock(STUDY_ID)));
        assertAccessAuthorized(api::generateParticipantsTsvByStudyId, STUDY_ID);
    }

    private void testWithStudyRights() throws ShanoirException {
        // No rights on the study
        given(studyRepository.findById(STUDY_ID)).willReturn(Optional.of(buildStudyMock(STUDY_ID)));
        assertAccessDenied(api::generateParticipantsTsvByStudyId, STUDY_ID);

        // Every right on the study is still not enough
        given(studyRepository.findById(STUDY_ID)).willReturn(Optional.of(buildStudyMock(STUDY_ID,
                StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT)));
        assertAccessDenied(api::generateParticipantsTsvByStudyId, STUDY_ID);
    }

    private Study buildStudyMock(Long id, StudyUserRight... rights) {
        Study study = ModelsUtil.createStudy();
        study.setId(id);
        List<StudyUser> studyUserList = new ArrayList<>();
        for (StudyUserRight right : rights) {
            StudyUser studyUser = new StudyUser();
            studyUser.setUserId(LOGGED_USER_ID);
            studyUser.setUserName(LOGGED_USER_USERNAME);
            studyUser.setStudy(study);
            studyUser.setStudyUserRights(Arrays.asList(right));
            studyUserList.add(studyUser);
        }
        study.setStudyUserList(studyUserList);
        return study;
    }

}
