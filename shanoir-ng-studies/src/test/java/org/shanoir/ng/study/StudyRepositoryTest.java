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

package org.shanoir.ng.study;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.center.repository.CenterRepository;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyExtraDetails;
import org.shanoir.ng.study.model.StudyStatus;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.tag.model.StudyTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;


@WebAppConfiguration
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class StudyRepositoryTest {

    private static final Long STUDY_TEST_1_ID = 1L;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private CenterRepository centerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void create() {
        final Center center = new Center();
        center.setName("test");
        centerRepository.save(center);

        final Study study = new Study();
        study.setName("StudyTest");
        study.setStartDate(LocalDate.now());
        study.setEndDate(LocalDate.now().plusMonths(6));

        final StudyExtraDetails studyExtraDetails = new StudyExtraDetails();
        studyExtraDetails.setExpectedNbOfCenters(1L);
        studyExtraDetails.setExpectedNbOfSubjects(100L);
        studyExtraDetails.setPrincipalInvestigator("test");
        studyExtraDetails.setSponsor("test");
        study.setExtraDetails(studyExtraDetails);

        StudyCenter studyCenter = new StudyCenter();
        studyCenter.setStudy(study);
        studyCenter.setCenter(center);
        List<StudyCenter> studyCenters = new ArrayList<StudyCenter>();
        studyCenters.add(studyCenter);
        study.setStudyCenterList(studyCenters);
        study.setStudyStatus(StudyStatus.IN_PROGRESS);

        final Study newStudy = studyRepository.save(study);
        assertEquals("StudyTest", newStudy.getName());
    }

    @Test
    public void delete() {
        studyRepository.deleteById(3L);

        final List<Study> studyList = (List<Study>) studyRepository.findAll();
        assertEquals(2, studyList.size());
    }

    @Test
    public void findAll() {
        final List<Study> studyList = (List<Study>) studyRepository.findAll();
        assertEquals("shanoirStudy1", studyList.get(0).getName());
    }

    @Test
    public void findByIdTest() {
        final Study s = studyRepository.findById(1L).orElseThrow();
        assertEquals("shanoirStudy1", s.getName());
    }

    @Test
    public void update() {
        final Study study = new Study();
        study.setName("StudyTest");
        study.setId(3L);

        final Study studyDb = studyRepository.findById(study.getId()).orElseThrow();
        studyDb.setName(study.getName());
        studyDb.setEndDate(study.getEndDate());
        studyDb.setClinical(study.isClinical());
        studyDb.setWithExamination(study.isWithExamination());
        studyDb.setVisibleByDefault(study.isVisibleByDefault());
        studyDb.setDownloadableByDefault(study.isDownloadableByDefault());
        studyDb.setStudyStatus(study.getStudyStatus());

        studyRepository.save(studyDb);

        final Study studyFound = studyRepository.findById(Long.valueOf(3)).orElseThrow();

        assertEquals("StudyTest", studyFound.getName());
    }

    @Test
    public void findStudiesOfUserLoadsAllMembers() {
        // user 2 has CAN_SEE_ALL (4) on study 1, which has 2 members: the whole member list must be
        // loaded, not only the queried user's StudyUser ("Members" column of the study list)
        final List<Study> studies = studyRepository
                .findByStudyUserList_UserIdAndStudyUserList_StudyUserRightsAndStudyUserList_Confirmed_OrderByNameAsc(2L, 4, true);
        assertEquals(1, studies.size());
        assertEquals(STUDY_TEST_1_ID, studies.get(0).getId());
        assertEquals(2, studies.get(0).getStudyUserList().size());
    }

    @Test
    public void testRights() {
        Study studyFound = studyRepository.findById(1L).orElseThrow();
        assertEquals(2, studyFound.getStudyUserList().size());
        assertEquals(2, studyFound.getStudyUserList().get(1).getStudyUserRights().size());
    }

//    @Test
//    public void testUpdateStudyUsers() {
//        int indexToDelete = 1;
//        Study studyFound = studyRepository.findById(1L);
//        assertEquals(2, studyFound.getStudyUserList().size());
//        Long deletedId = studyFound.getStudyUserList().get(indexToDelete).getId();
//
//        assertNotNull(studyUserRepository.findById(deletedId));
//        studyFound.getStudyUserList().remove(indexToDelete);
//        studyRepository.save(studyFound);
//        assertNull(studyUserRepository.findById(deletedId));
//    }

    @Test
    public void noStudyIsExportedToNeurobagelByDefault() {
        assertTrue(studyRepository.findByNeurobagelExportTrueAndIsDraftFalseOrderByIdAsc().isEmpty());
        assertFalse(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(1L));
    }

    @Test
    public void onlyFlaggedApprovedStudiesAreExportedToNeurobagel() {
        // Test data: studies 1, 2 and 3 are approved and not flagged
        Study flagged = studyRepository.findById(1L).orElseThrow();
        flagged.setNeurobagelExport(true);
        studyRepository.save(flagged);
        Study flaggedDraft = studyRepository.findById(3L).orElseThrow();
        flaggedDraft.setNeurobagelExport(true);
        flaggedDraft.setIsDraft(true);
        studyRepository.save(flaggedDraft);

        assertEquals(List.of(1L), studyRepository.findByNeurobagelExportTrueAndIsDraftFalseOrderByIdAsc()
                .stream().map(Study::getId).toList());
        assertTrue(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(1L));
        assertFalse(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(2L)); // not flagged
        assertFalse(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(3L)); // draft
        assertFalse(studyRepository.existsByIdAndNeurobagelExportTrueAndIsDraftFalse(999L)); // unknown
    }

    @Test
    public void studyTagsAreLoadedForTheNeurobagelDatasetDescription() {
        StudyTag tag = new StudyTag();
        tag.setName("mri");
        tag.setStudy(entityManager.find(Study.class, 1L));
        entityManager.persist(tag);
        entityManager.flush();
        entityManager.clear();

        Study study = studyRepository.findStudyWithStudyTagsById(1L);
        // ms studies runs without open-in-view: a lazy studyTags could not be read by the caller
        entityManager.clear();

        assertTrue(Persistence.getPersistenceUtil().isLoaded(study, "studyTags"));
        assertEquals(List.of("mri"), study.getStudyTags().stream().map(StudyTag::getName).toList());
    }

}
