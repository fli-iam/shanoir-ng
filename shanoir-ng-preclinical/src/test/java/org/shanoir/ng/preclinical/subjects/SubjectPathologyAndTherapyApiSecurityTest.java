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
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.PathologyService;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModel;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModelService;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathology;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathologyApiController;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathologyEditableByManager;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathologyService;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathologyValidator;
import org.shanoir.ng.preclinical.subjects.model.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.service.AnimalSubjectService;
import org.shanoir.ng.preclinical.therapies.Therapy;
import org.shanoir.ng.preclinical.therapies.TherapyService;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapy;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapyApiController;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapyEditableByManager;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapyService;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapyUniqueValidator;
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
 * Rights on the pathologies and therapies of the animal subjects, which are part of the subject:
 * view with CAN_SEE_ALL, change with the subject edit right (CAN_IMPORT, or expert role and CAN_ADMINISTRATE).
 * The lists across all the subjects are only used before deleting a catalogue entry: experts and admins,
 * filtered to the subjects of the studies the user can see.
 *
 * The study rights are really checked (PreclinicalSecurityService, StudyRightsService): only the study members
 * and the answers of ms datasets are mocked.
 */
@SpringBootTest
@ActiveProfiles("test")
public class SubjectPathologyAndTherapyApiSecurityTest {

    private static final long USER_ID = 2L;
    private static final long SUBJECT_ID = 10L;
    private static final long STUDY_ID = 1L;
    private static final long OTHER_SUBJECT_ID = 11L;
    private static final long OTHER_STUDY_ID = 2L;
    /** Subject pathology / therapy of the subject SUBJECT_ID */
    private static final long ENTRY_ID = 5L;
    /** Subject pathology / therapy of the subject OTHER_SUBJECT_ID */
    private static final long OTHER_ENTRY_ID = 6L;

    @Autowired
    private SubjectPathologyApiController pathologies;

    @Autowired
    private SubjectTherapyApiController therapies;

    @MockitoBean
    private SubjectPathologyService subjectPathologyService;

    @MockitoBean
    private PathologyModelService pathologyModelService;

    @MockitoBean
    private PathologyService pathologyService;

    @MockitoBean
    private SubjectPathologyValidator subjectPathologyValidator;

    @MockitoBean
    private SubjectPathologyEditableByManager subjectPathologyEditableByManager;

    @MockitoBean
    private SubjectTherapyService subjectTherapyService;

    @MockitoBean
    private TherapyService therapyService;

    @MockitoBean
    private SubjectTherapyUniqueValidator subjectTherapyUniqueValidator;

    @MockitoBean
    private SubjectTherapyEditableByManager subjectTherapyEditableByManager;

    @MockitoBean
    private AnimalSubjectService animalSubjectService;

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
        AnimalSubject subject = animalSubject(SUBJECT_ID);
        AnimalSubject otherSubject = animalSubject(OTHER_SUBJECT_ID);
        given(animalSubjectService.getById(SUBJECT_ID)).willReturn(subject);
        given(animalSubjectService.getById(OTHER_SUBJECT_ID)).willReturn(otherSubject);
        given(subjectPathologyService.findById(ENTRY_ID)).willReturn(subjectPathology(subject));
        given(subjectPathologyService.findById(OTHER_ENTRY_ID)).willReturn(subjectPathology(otherSubject));
        given(subjectTherapyService.findById(ENTRY_ID)).willReturn(subjectTherapy(subject));
        given(subjectTherapyService.findById(OTHER_ENTRY_ID)).willReturn(subjectTherapy(otherSubject));
        given(subjectPathologyEditableByManager.validate(any())).willReturn(new FieldErrorMap());
        given(subjectPathologyValidator.validate(any())).willReturn(new FieldErrorMap());
        given(subjectTherapyEditableByManager.validate(any())).willReturn(new FieldErrorMap());
        given(subjectTherapyUniqueValidator.validate(any())).willReturn(new FieldErrorMap());
    }

    @Test
    @WithAnonymousUser
    public void testAsAnonymous() throws ShanoirException {
        checkReads(SUBJECT_ID, false);
        checkWrites(SUBJECT_ID, false);
        checkCatalogueUsage(false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithoutRights() throws ShanoirException {
        checkReads(SUBJECT_ID, false);
        checkWrites(SUBJECT_ID, false);
        checkCatalogueUsage(false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithSeeAll() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL);
        checkReads(SUBJECT_ID, true);
        checkReads(OTHER_SUBJECT_ID, false);
        checkWrites(SUBJECT_ID, false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithImport() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT);
        checkWrites(SUBJECT_ID, true);
        checkWrites(OTHER_SUBJECT_ID, false);
        checkCatalogueUsage(false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testAsUserWithAdministrate() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_ADMINISTRATE);
        checkWrites(SUBJECT_ID, false);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_EXPERT" })
    public void testAsExpertWithAdministrate() throws ShanoirException {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_ADMINISTRATE);
        checkReads(SUBJECT_ID, true);
        checkWrites(SUBJECT_ID, true);
        checkWrites(OTHER_SUBJECT_ID, false);
        checkCatalogueUsage(true);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_ADMIN" })
    public void testAsAdmin() throws ShanoirException {
        checkReads(SUBJECT_ID, true);
        checkWrites(SUBJECT_ID, true);
        checkCatalogueUsage(true);
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
    public void testEntriesOfAnotherSubjectCannotBeReachedThroughAnAllowedSubject() throws Exception {
        givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_IMPORT);

        assertEquals(HttpStatus.NOT_FOUND, pathologies.getSubjectPathologyById(SUBJECT_ID, OTHER_ENTRY_ID).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, pathologies.updateSubjectPathology(SUBJECT_ID, OTHER_ENTRY_ID,
                new SubjectPathology(), bindingResult()).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, pathologies.deleteSubjectPathology(SUBJECT_ID, OTHER_ENTRY_ID).getStatusCode());
        verify(subjectPathologyService, never()).update(any());
        verify(subjectPathologyService, never()).deleteById(anyLong());

        assertEquals(HttpStatus.NOT_FOUND, therapies.getSubjectTherapyById(SUBJECT_ID, OTHER_ENTRY_ID).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, therapies.updateSubjectTherapy(SUBJECT_ID, OTHER_ENTRY_ID,
                new SubjectTherapy(), bindingResult()).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, therapies.deleteSubjectTherapy(SUBJECT_ID, OTHER_ENTRY_ID).getStatusCode());
        verify(subjectTherapyService, never()).update(any());
        verify(subjectTherapyService, never()).deleteById(anyLong());

        // the entries of the subject itself are still reached
        assertEquals(HttpStatus.OK, pathologies.getSubjectPathologyById(SUBJECT_ID, ENTRY_ID).getStatusCode());
        assertEquals(HttpStatus.OK, therapies.getSubjectTherapyById(SUBJECT_ID, ENTRY_ID).getStatusCode());
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_EXPERT" })
    public void testCatalogueUsageListsKeepOnlyTheSubjectsOfTheStudiesTheUserCanSee() throws Exception {
        StudyUser studyUser = givenRights(STUDY_ID, StudyUserRight.CAN_SEE_ALL);
        given(studyUserRepository.findByUserIdAndStudyIdIn(USER_ID, Set.of(STUDY_ID, OTHER_STUDY_ID)))
                .willReturn(List.of(studyUser));
        given(rabbitTemplate.convertSendAndReceive(RabbitMQConfiguration.SUBJECTS_STUDY_IDS_QUEUE, "[10,11]"))
                .willReturn("{\"10\":1,\"11\":2}");
        givenCatalogueUsedBySubjects(SUBJECT_ID, OTHER_SUBJECT_ID);

        assertOnlySubject(SUBJECT_ID, pathologies.getSubjectPathologiesByPathology(1L).getBody());
        assertOnlySubject(SUBJECT_ID, pathologies.getSubjectPathologiesByPathologyModel(1L).getBody());
        assertOnlySubject(SUBJECT_ID, therapies.getSubjectTherapiesByTherapy(1L).getBody());
    }

    @Test
    @WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_ADMIN" })
    public void testCatalogueUsageListsAreCompleteForAnAdmin() throws Exception {
        givenCatalogueUsedBySubjects(SUBJECT_ID, OTHER_SUBJECT_ID);

        assertEquals(2, pathologies.getSubjectPathologiesByPathology(1L).getBody().size());
        assertEquals(2, pathologies.getSubjectPathologiesByPathologyModel(1L).getBody().size());
        assertEquals(2, therapies.getSubjectTherapiesByTherapy(1L).getBody().size());
    }

    private void givenCatalogueUsedBySubjects(Long... subjectIds) {
        Pathology pathology = new Pathology();
        PathologyModel model = new PathologyModel();
        Therapy therapy = new Therapy();
        given(pathologyService.findById(1L)).willReturn(pathology);
        given(pathologyModelService.findById(1L)).willReturn(model);
        given(therapyService.findById(1L)).willReturn(therapy);
        List<SubjectPathology> subjectPathologies = Arrays.stream(subjectIds)
                .map(id -> subjectPathology(animalSubject(id))).toList();
        List<SubjectTherapy> subjectTherapies = Arrays.stream(subjectIds)
                .map(id -> subjectTherapy(animalSubject(id))).toList();
        given(subjectPathologyService.findAllByPathology(pathology)).willReturn(subjectPathologies);
        given(subjectPathologyService.findByPathologyModel(model)).willReturn(subjectPathologies);
        given(subjectTherapyService.findAllByTherapy(therapy)).willReturn(subjectTherapies);
    }

    private static void assertOnlySubject(Long subjectId, List<?> entries) {
        assertEquals(1, entries.size());
        Object entry = entries.get(0);
        AnimalSubject subject = entry instanceof SubjectPathology pathos ? pathos.getAnimalSubject()
                : ((SubjectTherapy) entry).getAnimalSubject();
        assertEquals(subjectId, subject.getId());
    }

    private void checkReads(Long subjectId, boolean authorized) throws ShanoirException {
        check(authorized, () -> pathologies.getSubjectPathologyById(subjectId, ENTRY_ID));
        check(authorized, () -> pathologies.getSubjectPathologies(subjectId));
        check(authorized, () -> therapies.getSubjectTherapyById(subjectId, ENTRY_ID));
        check(authorized, () -> therapies.getSubjectTherapies(subjectId));
    }

    private void checkWrites(Long subjectId, boolean authorized) throws ShanoirException {
        check(authorized, () -> pathologies.addSubjectPathology(subjectId, new SubjectPathology(), bindingResult()));
        check(authorized, () -> pathologies.updateSubjectPathology(subjectId, ENTRY_ID, new SubjectPathology(), bindingResult()));
        check(authorized, () -> pathologies.deleteSubjectPathology(subjectId, ENTRY_ID));
        check(authorized, () -> pathologies.deleteSubjectPathologies(subjectId));
        check(authorized, () -> therapies.addSubjectTherapy(subjectId, new SubjectTherapy(), bindingResult()));
        check(authorized, () -> therapies.updateSubjectTherapy(subjectId, ENTRY_ID, new SubjectTherapy(), bindingResult()));
        check(authorized, () -> therapies.deleteSubjectTherapy(subjectId, ENTRY_ID));
        check(authorized, () -> therapies.deleteSubjectTherapies(subjectId));
    }

    private void checkCatalogueUsage(boolean authorized) throws ShanoirException {
        check(authorized, () -> pathologies.getSubjectPathologiesByPathology(1L));
        check(authorized, () -> pathologies.getSubjectPathologiesByPathologyModel(1L));
        check(authorized, () -> therapies.getSubjectTherapiesByTherapy(1L));
    }

    private static void check(boolean authorized, AccessCheckedFunction0Arg call)
            throws ShanoirException {
        if (authorized) {
            assertAccessAuthorized(call);
        } else {
            assertAccessDenied(call);
        }
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

    private static AnimalSubject animalSubject(Long id) {
        AnimalSubject animalSubject = new AnimalSubject();
        animalSubject.setId(id);
        return animalSubject;
    }

    private static SubjectPathology subjectPathology(AnimalSubject animalSubject) {
        SubjectPathology subjectPathology = new SubjectPathology();
        subjectPathology.setAnimalSubject(animalSubject);
        return subjectPathology;
    }

    private static SubjectTherapy subjectTherapy(AnimalSubject animalSubject) {
        SubjectTherapy subjectTherapy = new SubjectTherapy();
        subjectTherapy.setAnimalSubject(animalSubject);
        return subjectTherapy;
    }

    private static BeanPropertyBindingResult bindingResult() {
        return new BeanPropertyBindingResult(new Object(), "entry");
    }

}
