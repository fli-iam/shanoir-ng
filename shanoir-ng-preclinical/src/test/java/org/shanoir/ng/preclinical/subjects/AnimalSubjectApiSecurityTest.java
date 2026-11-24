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

package org.shanoir.ng.preclinical.subjects;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.preclinical.subjects.controller.AnimalSubjectApiController;
import org.shanoir.ng.preclinical.subjects.dto.AnimalSubjectDto;
import org.shanoir.ng.preclinical.subjects.dto.SubjectDto;
import org.shanoir.ng.preclinical.subjects.service.AnimalSubjectEditableByManager;
import org.shanoir.ng.preclinical.subjects.service.AnimalSubjectService;
import org.shanoir.ng.preclinical.subjects.service.AnimalSubjectUniqueValidator;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.event.ShanoirEventService;
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
import org.springframework.validation.BeanPropertyBindingResult;

/**
 * Rights on the animal subjects, following the subject rows of the roles and rights specification:
 * view with CAN_SEE_ALL, create with CAN_IMPORT, edit with CAN_IMPORT or (expert role and CAN_ADMINISTRATE).
 *
 * The study rights are really checked (PreclinicalSecurityService, StudyRightsService): only the study members
 * and the answers of ms datasets are mocked.
 */
@SpringBootTest
@ActiveProfiles("test")
public class AnimalSubjectApiSecurityTest {

    private static final long USER_ID = 2L;
    private static final long SUBJECT_ID = 10L;
    private static final long STUDY_ID = 1L;
    private static final long OTHER_SUBJECT_ID = 11L;
    private static final long OTHER_STUDY_ID = 2L;

    @Autowired
    private AnimalSubjectApiController controller;

    @MockitoBean
    private AnimalSubjectService subjectService;

    @MockitoBean
    private RefsService refsService;

    @MockitoBean
    private ShanoirEventService eventService;

    @MockitoBean
    private AnimalSubjectUniqueValidator uniqueValidator;

    @MockitoBean
    private AnimalSubjectEditableByManager editableOnlyValidator;

    @MockitoBean
    private StudyUserRightsRepository studyUserRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    public void setup() {
        given(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECT_STUDY_ID_QUEUE, SUBJECT_ID))
                .willReturn(STUDY_ID);
        given(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECT_STUDY_ID_QUEUE, OTHER_SUBJECT_ID))
                .willReturn(OTHER_STUDY_ID);
    }

    @Test
    @WithAnonymousUser
    public void testAsAnonymous() throws ShanoirException {
        assertAccessDenied(controller::getAnimalSubjectById, SUBJECT_ID);
        assertAccessDenied(controller::findByIds, List.of(SUBJECT_ID));
        assertAccessDenied(controller::createAnimalSubject, animalSubjectIn(STUDY_ID), bindingResult());
        assertAccessDenied(this::update, SUBJECT_ID);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithoutRights() throws ShanoirException {
        assertAccessDenied(controller::getAnimalSubjectById, SUBJECT_ID);
        assertAccessAuthorized(controller::findByIds, List.of(SUBJECT_ID));
        assertAccessDenied(controller::createAnimalSubject, animalSubjectIn(STUDY_ID), bindingResult());
        assertAccessDenied(this::update, SUBJECT_ID);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithSeeAll() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_DOWNLOAD);
        assertAccessAuthorized(controller::getAnimalSubjectById, SUBJECT_ID);
        assertAccessDenied(controller::getAnimalSubjectById, OTHER_SUBJECT_ID);
        assertAccessDenied(controller::createAnimalSubject, animalSubjectIn(STUDY_ID), bindingResult());
        assertAccessDenied(this::update, SUBJECT_ID);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithImport() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT);
        assertAccessAuthorized(controller::createAnimalSubject, animalSubjectIn(STUDY_ID), bindingResult());
        assertAccessDenied(controller::createAnimalSubject, animalSubjectIn(OTHER_STUDY_ID), bindingResult());
        assertAccessDenied(controller::createAnimalSubject, new AnimalSubjectDto(), bindingResult());
        assertAccessAuthorized(this::update, SUBJECT_ID);
        assertAccessDenied(this::update, OTHER_SUBJECT_ID);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithAdministrate() throws ShanoirException {
        // a study administrator without the expert role cannot edit subjects
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_ADMINISTRATE);
        assertAccessDenied(this::update, SUBJECT_ID);
        assertAccessDenied(controller::createAnimalSubject, animalSubjectIn(STUDY_ID), bindingResult());
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_EXPERT" })
    public void testAsExpertWithAdministrate() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_ADMINISTRATE);
        assertAccessAuthorized(controller::getAnimalSubjectById, SUBJECT_ID);
        assertAccessAuthorized(this::update, SUBJECT_ID);
        assertAccessDenied(this::update, OTHER_SUBJECT_ID);
        // creating a subject is an import
        assertAccessDenied(controller::createAnimalSubject, animalSubjectIn(STUDY_ID), bindingResult());
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_EXPERT" })
    public void testAsExpertWithoutRights() throws ShanoirException {
        assertAccessDenied(controller::getAnimalSubjectById, SUBJECT_ID);
        assertAccessDenied(controller::createAnimalSubject, animalSubjectIn(STUDY_ID), bindingResult());
        assertAccessDenied(this::update, SUBJECT_ID);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_ADMIN" })
    public void testAsAdmin() throws ShanoirException {
        assertAccessAuthorized(controller::getAnimalSubjectById, SUBJECT_ID);
        assertAccessAuthorized(controller::findByIds, List.of(SUBJECT_ID));
        assertAccessAuthorized(controller::createAnimalSubject, animalSubjectIn(STUDY_ID), bindingResult());
        assertAccessAuthorized(this::update, SUBJECT_ID);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testFindByIdsKeepsOnlyTheSubjectsOfTheStudiesTheUserCanSee() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL);
        given(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECTS_STUDY_IDS_QUEUE, "[10,11,12]"))
                .willReturn("{\"10\":1,\"11\":2}");
        given(subjectService.findByIds(anyList())).willReturn(List.of());

        controller.findByIds(List.of(SUBJECT_ID, OTHER_SUBJECT_ID, 12L));

        verify(subjectService).findByIds(List.of(SUBJECT_ID));
    }

    private void update(Long subjectId) throws Exception {
        AnimalSubjectDto animalSubject = animalSubjectIn(STUDY_ID);
        controller.updateAnimalSubject(subjectId, animalSubject, bindingResult());
    }

    private void givenRights(Long studyId, StudyUserRight... rights) {
        StudyUser studyUser = new StudyUser();
        studyUser.setUserId(USER_ID);
        studyUser.setStudyId(studyId);
        studyUser.setStudyUserRights(Arrays.asList(rights));
        studyUser.setConfirmed(true);
        given(studyUserRepository.findByUserIdAndStudyId(USER_ID, studyId)).willReturn(studyUser);
        given(studyUserRepository.findByUserIdAndStudyIdIn(USER_ID, Set.of(studyId))).willReturn(List.of(studyUser));
        given(studyUserRepository.findByUserIdAndStudyIdIn(USER_ID, Set.of(studyId, OTHER_STUDY_ID)))
                .willReturn(List.of(studyUser));
    }

    private static AnimalSubjectDto animalSubjectIn(Long studyId) {
        SubjectDto subject = new SubjectDto();
        subject.setName("rat");
        subject.setStudy(new IdName(studyId, "study"));
        AnimalSubjectDto animalSubject = new AnimalSubjectDto();
        animalSubject.setSubject(subject);
        return animalSubject;
    }

    private static BeanPropertyBindingResult bindingResult() {
        return new BeanPropertyBindingResult(new AnimalSubjectDto(), "animalSubject");
    }

}
