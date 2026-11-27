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

package org.shanoir.ng.preclinical.anesthetics.examinationanesthetics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.anesthetics.anesthetic.AnestheticService;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.assertion.AccessCheckedFunction0Arg;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.BeanPropertyBindingResult;

/**
 * Rights on the anesthetics of the examinations, which are part of the examination:
 * view with CAN_SEE_ALL, change with the examination edit right (CAN_IMPORT, or expert role and CAN_ADMINISTRATE),
 * on the study of the examination and on its center when the user is restricted to some centers.
 * The list across all the examinations is only used before deleting an anesthetic: experts and admins,
 * filtered to the examinations the user can see.
 *
 * The study rights are really checked (PreclinicalSecurityService, StudyRightsService): only the study members
 * and the answers of ms datasets are mocked.
 */
@SpringBootTest
@ActiveProfiles("test")
public class ExaminationAnestheticApiSecurityTest {

    private static final long USER_ID = 2L;
    private static final long STUDY_USER_ID = 30L;
    private static final long EXAMINATION_ID = 20L;
    private static final long STUDY_ID = 1L;
    private static final long CENTER_ID = 7L;
    private static final long OTHER_EXAMINATION_ID = 21L;
    private static final long OTHER_STUDY_ID = 2L;
    /** Examination in the study STUDY_ID, but in another center */
    private static final long OTHER_CENTER_EXAMINATION_ID = 22L;
    private static final long OTHER_CENTER_ID = 8L;
    /** Examination anesthetic of the examination EXAMINATION_ID */
    private static final long ENTRY_ID = 5L;
    /** Examination anesthetic of the examination OTHER_EXAMINATION_ID */
    private static final long OTHER_ENTRY_ID = 6L;

    @Autowired
    private ExaminationAnestheticApiController controller;

    @MockitoBean
    private ExaminationAnestheticService examAnestheticService;

    @MockitoBean
    private AnestheticService anestheticService;

    @MockitoBean
    private ExaminationAnestheticUniqueValidator uniqueValidator;

    @MockitoBean
    private ExaminationAnestheticEditableByManager editableOnlyValidator;

    @MockitoBean
    private StudyUserRightsRepository studyUserRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    public void setup() {
        givenExamination(EXAMINATION_ID, STUDY_ID, CENTER_ID);
        givenExamination(OTHER_EXAMINATION_ID, OTHER_STUDY_ID, CENTER_ID);
        givenExamination(OTHER_CENTER_EXAMINATION_ID, STUDY_ID, OTHER_CENTER_ID);
        given(examAnestheticService.findById(ENTRY_ID)).willReturn(examAnesthetic(EXAMINATION_ID));
        given(examAnestheticService.findById(OTHER_ENTRY_ID)).willReturn(examAnesthetic(OTHER_EXAMINATION_ID));
        given(uniqueValidator.validate(any())).willReturn(new FieldErrorMap());
        given(editableOnlyValidator.validate(any())).willReturn(new FieldErrorMap());
    }

    @Test
    @WithAnonymousUser
    public void testAsAnonymous() throws ShanoirException {
        checkReads(EXAMINATION_ID, false);
        checkWrites(EXAMINATION_ID, false);
        checkAnestheticUsage(false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithoutRights() throws ShanoirException {
        checkReads(EXAMINATION_ID, false);
        checkWrites(EXAMINATION_ID, false);
        checkAnestheticUsage(false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithSeeAll() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL);
        checkReads(EXAMINATION_ID, true);
        checkReads(OTHER_EXAMINATION_ID, false);
        checkWrites(EXAMINATION_ID, false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithImport() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT);
        checkWrites(EXAMINATION_ID, true);
        checkWrites(OTHER_EXAMINATION_ID, false);
        checkAnestheticUsage(false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserRestrictedToAnotherCenter() throws ShanoirException {
        StudyUser studyUser = givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT);
        studyUser.setId(STUDY_USER_ID);
        given(studyUserRepository.findCenterIdsByStudyUserId(STUDY_USER_ID)).willReturn(List.of(CENTER_ID));
        checkReads(EXAMINATION_ID, true);
        checkWrites(EXAMINATION_ID, true);
        checkReads(OTHER_CENTER_EXAMINATION_ID, false);
        checkWrites(OTHER_CENTER_EXAMINATION_ID, false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithAdministrate() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_ADMINISTRATE);
        checkWrites(EXAMINATION_ID, false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_EXPERT" })
    public void testAsExpertWithAdministrate() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_ADMINISTRATE);
        checkReads(EXAMINATION_ID, true);
        checkWrites(EXAMINATION_ID, true);
        checkWrites(OTHER_EXAMINATION_ID, false);
        checkAnestheticUsage(true);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_ADMIN" })
    public void testAsAdmin() throws ShanoirException {
        checkReads(EXAMINATION_ID, true);
        checkWrites(EXAMINATION_ID, true);
        checkAnestheticUsage(true);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testEntriesOfAnotherExaminationCannotBeReachedThroughAnAllowedExamination() throws Exception {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT);

        assertEquals(HttpStatus.NOT_FOUND,
                controller.getExaminationAnestheticById(EXAMINATION_ID, OTHER_ENTRY_ID).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.updateExaminationAnesthetic(EXAMINATION_ID, OTHER_ENTRY_ID,
                new ExaminationAnesthetic(), bindingResult()).getStatusCode());
        // same answer as an unknown examination anesthetic
        assertEquals(HttpStatus.NO_CONTENT,
                controller.deleteExaminationAnesthetic(EXAMINATION_ID, OTHER_ENTRY_ID).getStatusCode());
        verify(examAnestheticService, never()).update(any());
        verify(examAnestheticService, never()).deleteById(anyLong());

        // the entries of the examination itself are still reached
        assertEquals(HttpStatus.OK, controller.getExaminationAnestheticById(EXAMINATION_ID, ENTRY_ID).getStatusCode());
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_EXPERT" })
    public void testAnestheticUsageListKeepsOnlyTheExaminationsTheUserCanSee() throws Exception {
        StudyUser studyUser = givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL);
        given(studyUserRepository.findAllByUserId(USER_ID)).willReturn(Optional.of(List.of(studyUser)));
        given(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATIONS_STUDY_CENTERS_QUEUE, "[20,21]"))
                .willReturn("{\"20\":{\"studyId\":1,\"centerId\":7},\"21\":{\"studyId\":2,\"centerId\":7}}");
        givenAnestheticUsedByExaminations(EXAMINATION_ID, OTHER_EXAMINATION_ID);

        List<ExaminationAnesthetic> entries = controller.getExaminationAnestheticsByAnesthetic(1L).getBody();
        assertEquals(1, entries.size());
        assertEquals(EXAMINATION_ID, entries.get(0).getExaminationId());
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_ADMIN" })
    public void testAnestheticUsageListIsCompleteForAnAdmin() throws Exception {
        givenAnestheticUsedByExaminations(EXAMINATION_ID, OTHER_EXAMINATION_ID);

        assertEquals(2, controller.getExaminationAnestheticsByAnesthetic(1L).getBody().size());
    }

    private void checkReads(Long examinationId, boolean authorized) throws ShanoirException {
        check(authorized, () -> controller.getExaminationAnestheticById(examinationId, ENTRY_ID));
        check(authorized, () -> controller.getExaminationAnesthetics(examinationId));
    }

    private void checkWrites(Long examinationId, boolean authorized) throws ShanoirException {
        check(authorized, () -> controller.addExaminationAnesthetic(examinationId, new ExaminationAnesthetic(), bindingResult()));
        check(authorized, () -> controller.updateExaminationAnesthetic(examinationId, ENTRY_ID,
                new ExaminationAnesthetic(), bindingResult()));
        check(authorized, () -> controller.deleteExaminationAnesthetic(examinationId, ENTRY_ID));
    }

    private void checkAnestheticUsage(boolean authorized) throws ShanoirException {
        check(authorized, () -> controller.getExaminationAnestheticsByAnesthetic(1L));
    }

    private static void check(boolean authorized, AccessCheckedFunction0Arg call) throws ShanoirException {
        if (authorized) {
            assertAccessAuthorized(call);
        } else {
            assertAccessDenied(call);
        }
    }

    private void givenExamination(Long examinationId, Long studyId, Long centerId) {
        given(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_STUDY_CENTER_QUEUE, examinationId))
                .willReturn("{\"studyId\":" + studyId + ",\"centerId\":" + centerId + "}");
    }

    private void givenAnestheticUsedByExaminations(Long... examinationIds) {
        Anesthetic anesthetic = new Anesthetic();
        given(anestheticService.findById(1L)).willReturn(anesthetic);
        given(examAnestheticService.findByAnesthetic(anesthetic))
                .willReturn(Arrays.stream(examinationIds).map(ExaminationAnestheticApiSecurityTest::examAnesthetic).toList());
    }

    private StudyUser givenRights(Long studyId, StudyUserRight... rights) {
        StudyUser studyUser = new StudyUser();
        studyUser.setUserId(USER_ID);
        studyUser.setStudyId(studyId);
        studyUser.setStudyUserRights(Arrays.asList(rights));
        studyUser.setConfirmed(true);
        given(studyUserRepository.findByUserIdAndStudyId(USER_ID, studyId)).willReturn(studyUser);
        return studyUser;
    }

    private static ExaminationAnesthetic examAnesthetic(Long examinationId) {
        ExaminationAnesthetic examAnesthetic = new ExaminationAnesthetic();
        examAnesthetic.setExaminationId(examinationId);
        return examAnesthetic;
    }

    private static BeanPropertyBindingResult bindingResult() {
        return new BeanPropertyBindingResult(new ExaminationAnesthetic(), "examAnesthetic");
    }

}
