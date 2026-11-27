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

package org.shanoir.ng.preclinical.extradata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.shanoir.ng.preclinical.extradata.bloodgas.BloodGasData;
import org.shanoir.ng.preclinical.extradata.bloodgas.BloogGasUniqueConstraintManager;
import org.shanoir.ng.preclinical.extradata.examination.ExaminationExtraData;
import org.shanoir.ng.preclinical.extradata.examination.ExaminationExtraDataRepository;
import org.shanoir.ng.preclinical.extradata.physiological.PhysioDataUniqueConstraintManager;
import org.shanoir.ng.preclinical.extradata.physiological.PhysiologicalData;
import org.shanoir.ng.shared.configuration.RabbitMQConfiguration;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.storage.StorageService;
import org.shanoir.ng.study.rights.StudyUser;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.assertion.AccessCheckedFunction0Arg;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * Rights on the extra data of the examinations, which are part of the examination:
 * view with CAN_SEE_ALL, download the file with CAN_DOWNLOAD, change with CAN_IMPORT only (the "Extra data"
 * row of the roles and rights specification, for the experts too), on the study of the examination and on its
 * center when the user is restricted to some centers. The upload and the download only know the extra data:
 * the rights are checked on its examination.
 *
 * The study rights are really checked (PreclinicalSecurityService, StudyRightsService): only the study members,
 * the answers of ms datasets and the extra data are mocked.
 */
@SpringBootTest
@ActiveProfiles("test")
public class ExtraDataApiSecurityTest {

    private static final long USER_ID = 2L;
    private static final long EXAMINATION_ID = 20L;
    private static final long STUDY_ID = 1L;
    private static final long OTHER_EXAMINATION_ID = 21L;
    private static final long OTHER_STUDY_ID = 2L;
    /** Extra data of the examination EXAMINATION_ID */
    private static final long ENTRY_ID = 5L;
    /** Extra data of the examination OTHER_EXAMINATION_ID */
    private static final long OTHER_ENTRY_ID = 6L;

    @Autowired
    private ExtraDataApiController controller;

    @MockitoBean
    private ExtraDataService<ExaminationExtraData> extraDataService;

    @MockitoBean
    private ExtraDataService<PhysiologicalData> physioDataService;

    @MockitoBean
    private ExtraDataService<BloodGasData> bloodGasDataService;

    @MockitoBean
    private ExtraDataUniqueConstraintManager uniqueConstraintManager;

    @MockitoBean
    private PhysioDataUniqueConstraintManager physioConstraintManager;

    @MockitoBean
    private BloogGasUniqueConstraintManager bloodGasConstraintManager;

    @MockitoBean
    private ExtraDataEditableByManager editableOnlyValidator;

    @MockitoBean
    private StorageService storageService;

    @MockitoBean
    private ExaminationExtraDataRepository extraDataRepository;

    @MockitoBean
    private StudyUserRightsRepository studyUserRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    public void setup() throws ShanoirException {
        givenExamination(EXAMINATION_ID, STUDY_ID);
        givenExamination(OTHER_EXAMINATION_ID, OTHER_STUDY_ID);
        givenExtraData(ENTRY_ID, EXAMINATION_ID);
        givenExtraData(OTHER_ENTRY_ID, OTHER_EXAMINATION_ID);
        given(uniqueConstraintManager.validate(any())).willReturn(new FieldErrorMap());
        given(physioConstraintManager.validate(any())).willReturn(new FieldErrorMap());
        given(bloodGasConstraintManager.validate(any())).willReturn(new FieldErrorMap());
        given(editableOnlyValidator.validate(any())).willReturn(new FieldErrorMap());
    }

    @Test
    @WithAnonymousUser
    public void testAsAnonymous() throws ShanoirException {
        checkReads(EXAMINATION_ID, ENTRY_ID, false);
        checkDownload(ENTRY_ID, false);
        checkWrites(EXAMINATION_ID, ENTRY_ID, false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithoutRights() throws ShanoirException {
        checkReads(EXAMINATION_ID, ENTRY_ID, false);
        checkDownload(ENTRY_ID, false);
        checkWrites(EXAMINATION_ID, ENTRY_ID, false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithSeeAll() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL);
        checkReads(EXAMINATION_ID, ENTRY_ID, true);
        checkReads(OTHER_EXAMINATION_ID, OTHER_ENTRY_ID, false);
        // seeing is not downloading
        checkDownload(ENTRY_ID, false);
        checkWrites(EXAMINATION_ID, ENTRY_ID, false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithDownload() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_DOWNLOAD);
        checkDownload(ENTRY_ID, true);
        checkDownload(OTHER_ENTRY_ID, false);
        checkWrites(EXAMINATION_ID, ENTRY_ID, false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithImport() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT);
        checkWrites(EXAMINATION_ID, ENTRY_ID, true);
        checkWrites(OTHER_EXAMINATION_ID, OTHER_ENTRY_ID, false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithAdministrate() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_ADMINISTRATE);
        checkWrites(EXAMINATION_ID, ENTRY_ID, false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_EXPERT" })
    public void testAsExpertWithAdministrate() throws ShanoirException {
        // unlike the other examination data, CAN_ADMINISTRATE is not enough for the extra data
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_ADMINISTRATE);
        checkReads(EXAMINATION_ID, ENTRY_ID, true);
        checkWrites(EXAMINATION_ID, ENTRY_ID, false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_EXPERT" })
    public void testAsExpertWithImport() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT);
        checkReads(EXAMINATION_ID, ENTRY_ID, true);
        checkWrites(EXAMINATION_ID, ENTRY_ID, true);
        checkWrites(OTHER_EXAMINATION_ID, OTHER_ENTRY_ID, false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_ADMIN" })
    public void testAsAdmin() throws ShanoirException {
        checkReads(EXAMINATION_ID, ENTRY_ID, true);
        checkDownload(ENTRY_ID, true);
        checkWrites(EXAMINATION_ID, ENTRY_ID, true);
        assertAccessAuthorized(controller::getAllFiles);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_EXPERT" })
    public void testFilesListIsForAdminsOnly() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_DOWNLOAD);
        assertAccessDenied(controller::getAllFiles);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testEntriesOfAnotherExaminationCannotBeReachedThroughAnAllowedExamination() throws Exception {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT);

        assertEquals(HttpStatus.NOT_FOUND, controller.getExtraDataById(EXAMINATION_ID, OTHER_ENTRY_ID).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteExtraData(EXAMINATION_ID, OTHER_ENTRY_ID).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.updatePhysiologicalData(EXAMINATION_ID, OTHER_ENTRY_ID,
                new PhysiologicalData(), bindingResult()).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.updateBloodGasData(EXAMINATION_ID, OTHER_ENTRY_ID,
                new BloodGasData(), bindingResult()).getStatusCode());
        verify(extraDataService, never()).deleteById(anyLong());
        verify(storageService, never()).deletePreclinicalExtraData(any(), any());
        verify(physioDataService, never()).update(any());
        verify(bloodGasDataService, never()).update(any());

        // the entries of the examination itself are still reached
        assertEquals(HttpStatus.OK, controller.getExtraDataById(EXAMINATION_ID, ENTRY_ID).getStatusCode());
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testExtraDataIsCreatedAndUpdatedInTheExaminationOfThePath() throws Exception {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT);

        PhysiologicalData created = new PhysiologicalData();
        created.setExaminationId(OTHER_EXAMINATION_ID);
        controller.createPhysiologicalExtraData(EXAMINATION_ID, created, bindingResult());
        ArgumentCaptor<PhysiologicalData> saved = ArgumentCaptor.forClass(PhysiologicalData.class);
        verify(physioDataService).save(saved.capture());
        assertEquals(EXAMINATION_ID, saved.getValue().getExaminationId());

        BloodGasData updated = new BloodGasData();
        updated.setExaminationId(OTHER_EXAMINATION_ID);
        controller.updateBloodGasData(EXAMINATION_ID, ENTRY_ID, updated, bindingResult());
        ArgumentCaptor<BloodGasData> changed = ArgumentCaptor.forClass(BloodGasData.class);
        verify(bloodGasDataService).update(changed.capture());
        assertEquals(EXAMINATION_ID, changed.getValue().getExaminationId());
    }

    private void checkReads(Long examinationId, Long entryId, boolean authorized) throws ShanoirException {
        check(authorized, () -> controller.getExtraDataById(examinationId, entryId));
        check(authorized, () -> controller.getExaminationExtraData(examinationId));
    }

    private void checkDownload(Long entryId, boolean authorized) throws ShanoirException {
        check(authorized, () -> controller.downloadExtraData(entryId, new MockHttpServletResponse()));
    }

    private void checkWrites(Long examinationId, Long entryId, boolean authorized) throws ShanoirException {
        check(authorized, () -> controller.createExtraData(examinationId, new ExaminationExtraData(), bindingResult()));
        check(authorized, () -> controller.createPhysiologicalExtraData(examinationId, new PhysiologicalData(), bindingResult()));
        check(authorized, () -> controller.createBloodGasExtraData(examinationId, new BloodGasData(), bindingResult()));
        check(authorized, () -> controller.updatePhysiologicalData(examinationId, entryId, new PhysiologicalData(), bindingResult()));
        check(authorized, () -> controller.updateBloodGasData(examinationId, entryId, new BloodGasData(), bindingResult()));
        check(authorized, () -> controller.deleteExtraData(examinationId, entryId));
        MultipartFile file = new MockMultipartFile("files", "data.txt", "text/plain", "data".getBytes());
        check(authorized, () -> controller.uploadExtraData(entryId, new MultipartFile[] {file}));
    }

    private static void check(boolean authorized, AccessCheckedFunction0Arg call) throws ShanoirException {
        if (authorized) {
            assertAccessAuthorized(call);
        } else {
            assertAccessDenied(call);
        }
    }

    private void givenExamination(Long examinationId, Long studyId) {
        given(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.EXAMINATION_STUDY_CENTER_QUEUE, examinationId))
                .willReturn("{\"studyId\":" + studyId + ",\"centerId\":7}");
    }

    private void givenExtraData(Long extraDataId, Long examinationId) {
        ExaminationExtraData extraData = new ExaminationExtraData();
        extraData.setId(extraDataId);
        extraData.setExaminationId(examinationId);
        given(extraDataRepository.findById(extraDataId)).willReturn(Optional.of(extraData));
        given(extraDataService.findById(extraDataId)).willReturn(extraData);
        PhysiologicalData physioData = new PhysiologicalData();
        physioData.setExaminationId(examinationId);
        given(physioDataService.findById(extraDataId)).willReturn(physioData);
        BloodGasData bloodGasData = new BloodGasData();
        bloodGasData.setExaminationId(examinationId);
        given(bloodGasDataService.findById(extraDataId)).willReturn(bloodGasData);
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

    private static BeanPropertyBindingResult bindingResult() {
        return new BeanPropertyBindingResult(new ExaminationExtraData(), "extradata");
    }

}
