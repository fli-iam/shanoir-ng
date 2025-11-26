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

package org.shanoir.ng.subject.service;

import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.repository.StudyUserRepository;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.repository.SubjectRepository;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;

/**
 * User security service test.
 *
 * @author jlouis
 *
 */

@SpringBootTest
@ActiveProfiles("test")
public class SubjectServiceSecurityTest {

    private static final long LOGGED_USER_ID = 2L;
    private static final String LOGGED_USER_USERNAME = "logged";
    private static final long ENTITY_ID = 1L;

    private Subject mockNew;
    private Subject mockExisting;

    @Autowired
    private SubjectService service;

    @MockBean
    private SubjectRepository repository;

    @MockBean
    private StudyRepository studyRepository;

    @MockBean
    private StudyUserRepository studyUserRepository;

    @BeforeEach
    public void setup() {
        mockNew = ModelsUtil.createSubject();
        mockExisting = ModelsUtil.createSubject();
        mockExisting.setId(ENTITY_ID);
    }

    @Test
    @WithAnonymousUser
    public void testAsAnonymous() throws ShanoirException {
        List<Study> studiesMock = new ArrayList<>();
        studiesMock.add(buildStudyMock(1L));
        assertAccessDenied(service::findAll);
        assertAccessDenied(service::findAllSubjectsOfStudyId, 1L);

        assertAccessDenied(service::findById, ENTITY_ID);
        assertAccessDenied(service::findByIdentifierInStudiesWithRights, "identifier", studiesMock);
        assertAccessDenied(service::findByIdWithSubjectStudies, ENTITY_ID);
        assertAccessDenied(service::findSubjectFromCenterCode, "centerCode");

        assertAccessDenied(service::create, mockNew);
        assertAccessDenied(service::update, mockExisting);
        assertAccessDenied(service::deleteById, ENTITY_ID);
    }

    @Test
    @WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
    public void testReadByUser() throws ShanoirException {
        testRead();
    }

    @Test
    @WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
    public void testCreateAsUser() throws ShanoirException {
        testCreate();
    }

    @Test
    @WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
    public void testEditAsUser() throws ShanoirException {
        assertAccessDenied(service::update, mockExisting);
        assertAccessDenied(service::deleteById, ENTITY_ID);
    }

    @Test
    @WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
    public void testReadByExpert() throws ShanoirException {
        testRead();
    }

    @Test
    @WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
    public void testCreateAsExpert() throws ShanoirException {
        testCreate();
    }

    @Test
    @WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
    public void testEditAsExpert() throws ShanoirException {
        List<Study> studiesMock = new ArrayList<>();
        studiesMock.add(buildStudyMock(1L, StudyUserRight.CAN_IMPORT));
        Subject subjectMock1 = buildSubjectMock(ENTITY_ID);
        addStudyToMock(subjectMock1, 1L, StudyUserRight.CAN_IMPORT);
        given(studyRepository.findAllById(Arrays.asList(new Long[]{1L}))).willReturn(studiesMock);
        given(studyUserRepository.findByStudy_Id(1L)).willReturn(studiesMock.get(0).getStudyUserList());
        given(repository.findById(ENTITY_ID)).willReturn(Optional.of(subjectMock1));
        assertAccessAuthorized(service::update, subjectMock1);

        Subject subjectMock2 = buildSubjectMock(ENTITY_ID);
        addStudyToMock(subjectMock2, 1L, StudyUserRight.CAN_SEE_ALL);
        given(repository.findById(ENTITY_ID)).willReturn(Optional.of(subjectMock2));
        assertAccessDenied(service::deleteById, ENTITY_ID);

        Subject subjectMock3 = buildSubjectMock(ENTITY_ID);
        addStudyToMock(subjectMock3, 1L, StudyUserRight.CAN_ADMINISTRATE);
        given(repository.findById(ENTITY_ID)).willReturn(Optional.of(subjectMock3));
        assertAccessAuthorized(service::deleteById, ENTITY_ID);
    }

    @Test
    @WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
    public void testAsAdmin() throws ShanoirException {
        List<Study> studiesMock = new ArrayList<>();
        studiesMock.add(buildStudyMock(1L));
        assertAccessAuthorized(service::findAll);
        assertAccessAuthorized(service::findAllSubjectsOfStudyId, 1L);
        assertAccessAuthorized(service::findById, ENTITY_ID);
        assertAccessAuthorized(service::findByIdentifierInStudiesWithRights, "identifier", studiesMock);
        assertAccessAuthorized(service::findByIdWithSubjectStudies, ENTITY_ID);
        assertAccessAuthorized(service::findSubjectFromCenterCode, "centerCode");
        assertAccessAuthorized(service::create, mockNew);
        assertAccessAuthorized(service::update, mockExisting);
        assertAccessAuthorized(service::deleteById, ENTITY_ID);
    }

    private void testRead() throws ShanoirException {
        final String name = "data";

        Subject subjectMockNoRights = buildSubjectMock(1L);
        given(repository.findByStudyIdAndName(1L, name)).willReturn(subjectMockNoRights);
        given(repository.findById(1L)).willReturn(Optional.of(subjectMockNoRights));
        given(repository.findSubjectWithSubjectStudyById(1L)).willReturn(subjectMockNoRights);
        given(repository.findSubjectFromCenterCode("centerCode%")).willReturn(subjectMockNoRights);
        assertAccessDenied(service::findById, 1L);
        assertAccessDenied(service::findByIdWithSubjectStudies, 1L);
        assertAccessDenied(service::findSubjectFromCenterCode, "centerCode");

        Subject subjectMockWrongRights = buildSubjectMock(1L);
        addStudyToMock(subjectMockWrongRights, 100L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT);
        given(repository.findByStudyIdAndName(1L, name)).willReturn(subjectMockWrongRights);
        given(repository.findById(1L)).willReturn(Optional.of(subjectMockWrongRights));
        given(repository.findSubjectWithSubjectStudyById(1L)).willReturn(subjectMockWrongRights);
        given(repository.findSubjectFromCenterCode("centerCode%")).willReturn(subjectMockWrongRights);
        assertAccessDenied(service::findById, 1L);
        assertAccessDenied(service::findByIdWithSubjectStudies, 1L);
        assertAccessDenied(service::findSubjectFromCenterCode, "centerCode");

        Subject subjectMockRightRights = buildSubjectMock(1L);
        addStudyToMock(subjectMockRightRights, 100L, StudyUserRight.CAN_SEE_ALL);
        given(repository.findByStudyIdAndName(1L, name)).willReturn(subjectMockRightRights);
        given(repository.findById(1L)).willReturn(Optional.of(subjectMockRightRights));
        given(repository.findSubjectWithSubjectStudyById(1L)).willReturn(subjectMockRightRights);
        given(repository.findSubjectFromCenterCode("centerCode%")).willReturn(subjectMockRightRights);
        given(studyRepository.findById(100L)).willReturn(Optional.of(subjectMockRightRights.getStudy()));
        assertAccessAuthorized(service::findById, 1L);
        assertAccessAuthorized(service::findByIdWithSubjectStudies, 1L);
        assertAccessAuthorized(service::findSubjectFromCenterCode, "centerCode");
    }

    private void testCreate() throws ShanoirException {
        List<Study> studiesMock;

        // Create subject without subject <-> study
        Subject newSubjectMock = buildSubjectMock(null);
        assertAccessDenied(service::create, newSubjectMock);

        // Create subject
        studiesMock = new ArrayList<>();
        studiesMock.add(buildStudyMock(9L));
        given(studyRepository.findAllById(Arrays.asList(new Long[] {9L}))).willReturn(studiesMock);
        newSubjectMock = buildSubjectMock(null);
        addStudyToMock(newSubjectMock, 9L);
        assertAccessDenied(service::create, newSubjectMock);

        // Create subject linked to a study where I can admin, download, see all but not import.
        studiesMock = new ArrayList<>();
        studiesMock.add(buildStudyMock(10L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_SEE_ALL));
        given(studyRepository.findAllById(Arrays.asList(new Long[] {10L}))).willReturn(studiesMock);
        newSubjectMock = buildSubjectMock(null);
        addStudyToMock(newSubjectMock, 10L);
        assertAccessDenied(service::create, newSubjectMock);

        // Create subject linked to a study where I can import and also to a study where I can't.
        studiesMock = new ArrayList<>();
        studiesMock.add(buildStudyMock(11L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_SEE_ALL));
        studiesMock.add(buildStudyMock(12L, StudyUserRight.CAN_IMPORT));
        given(studyRepository.findAllById(Arrays.asList(new Long[] {12L, 11L}))).willReturn(studiesMock);
        given(studyRepository.findAllById(Arrays.asList(new Long[] {11L, 12L}))).willReturn(studiesMock);
        newSubjectMock = buildSubjectMock(null);
        addStudyToMock(newSubjectMock, 11L);
        addStudyToMock(newSubjectMock, 12L);
        assertAccessDenied(service::create, newSubjectMock);

        // Create subject linked to a study where I can import
        studiesMock = new ArrayList<>();
        Study studyMock = buildStudyMock(13L, StudyUserRight.CAN_IMPORT);
        studiesMock.add(studyMock);
        given(studyRepository.findAllById(Arrays.asList(new Long[] {13L}))).willReturn(studiesMock);
        given(studyRepository.findById(13L)).willReturn(Optional.of(studyMock));
        given(studyUserRepository.findByStudy_Id(13L)).willReturn(studiesMock.get(0).getStudyUserList());
        newSubjectMock = buildSubjectMock(null);
        addStudyToMock(newSubjectMock, 13L, StudyUserRight.CAN_IMPORT);
        assertAccessAuthorized(service::create, newSubjectMock);
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

    private Subject buildSubjectMock(Long id) {
        Subject subject = ModelsUtil.createSubject();
        subject.setId(id);
        return subject;
    }

    private void addStudyToMock(Subject mock, Long id, StudyUserRight... rights) {
        Study study = buildStudyMock(id, rights);
        mock.setStudy(study);

        SubjectStudy subjectStudy = new SubjectStudy();
        subjectStudy.setSubject(mock);
        subjectStudy.setStudy(study);

        if (study.getSubjectStudyList() == null) {
            study.setSubjectStudyList(new ArrayList<SubjectStudy>());
        }
        if (mock.getSubjectStudyList() == null) {
            mock.setSubjectStudyList(new ArrayList<SubjectStudy>());
        }
        study.getSubjectStudyList().add(subjectStudy);
        mock.getSubjectStudyList().add(subjectStudy);
    }

}
