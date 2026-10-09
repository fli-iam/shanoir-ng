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

package org.shanoir.ng.studycard;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.GenericDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.dicom.web.StudyInstanceUIDAndSubjectNameHandler;
import org.shanoir.ng.download.AcquisitionAttributes;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.ng.studycard.model.rule.QualityCardRule;
import org.shanoir.ng.studycard.repository.QualityCardRepository;
import org.shanoir.ng.studycard.service.QualityCardServiceImpl;
import org.shanoir.ng.utils.SecurityContextUtil;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;

/**
 * Quality card service test.
 */
@SpringBootTest
@ActiveProfiles("test")
public class QualityCardServiceTest {

    private static final Long QUALITY_CARD_ID = 1L;
    private static final Long STUDY_ID = 2L;
    private static final String QUALITY_CARD_NAME = "QualityCard1";
    private static final String UPDATED_QUALITY_CARD_NAME = "QualityCard1Updated";

    @Mock
    private QualityCardRepository qualityCardRepository;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private ExaminationRepository examinationRepository;

    @Mock
    private WADODownloaderService downloader;

    @Mock
    private ShanoirEventService eventService;

    @Mock
    private DatasetAcquisitionService datasetAcquisitionService;

    @MockBean
    private StudyInstanceUIDAndSubjectNameHandler studyInstanceUIDHandler;

    @InjectMocks
    private QualityCardServiceImpl qualityCardService;

    @BeforeEach
    public void setup() throws Exception {
        // applyQualityCardOnStudy() reads the authenticated user via KeycloakUtil.getTokenUserId() to
        // build its ShanoirEvent; in production this comes from the request's JWT, but here there's no
        // real HTTP request, so the security context needs to be seeded manually.
        SecurityContextUtil.initAuthenticationContext("ROLE_ADMIN");
        given(qualityCardRepository.findAll()).willReturn(Arrays.asList(createQualityCard()));
        given(qualityCardRepository.findById(QUALITY_CARD_ID)).willReturn(Optional.of(createQualityCard()));
        given(qualityCardRepository.findByStudyId(STUDY_ID)).willReturn(Arrays.asList(createQualityCard()));
        given(qualityCardRepository.findByStudyIdIn(Collections.singletonList(STUDY_ID)))
                .willReturn(Arrays.asList(createQualityCard()));
        given(qualityCardRepository.findByName(QUALITY_CARD_NAME)).willReturn(createQualityCard());
        given(qualityCardRepository.save(Mockito.any(QualityCard.class))).willReturn(createQualityCard());
        given(downloader.getDicomAttributesForAcquisition(Mockito.any())).willReturn(new AcquisitionAttributes<>());
        given(studyRepository.findById(STUDY_ID)).willReturn(Optional.of(createStudy()));
        ReflectionTestUtils.setField(qualityCardService, "self", qualityCardService);
    }

    @Test
    public void updateTest() throws EntityNotFoundException {
        final QualityCard update = createQualityCard();
        update.setName(UPDATED_QUALITY_CARD_NAME);
        qualityCardService.update(update);
        Mockito.verify(qualityCardRepository, Mockito.times(1)).save(Mockito.any(QualityCard.class));
    }

    @Test
    public void updateNotFoundTest() {
        final QualityCard update = createQualityCard();
        update.setId(99L);
        given(qualityCardRepository.findById(99L)).willReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> qualityCardService.update(update));
        Mockito.verify(qualityCardRepository, Mockito.never()).save(Mockito.any(QualityCard.class));
    }

    /**
     * When two rules of a quality card are both applicable to the same dataset acquisition, the
     * resulting quality tag must be the most severe one (ERROR > WARNING > VALID), regardless of
     * the order in which the rules were evaluated - not just "last rule wins".
     */
    @Test
    public void applyQualityCardOnStudyKeepsMostSevereTagWhenErrorAppliedFirstTest() throws Exception {
        final DatasetAcquisition acquisition = createAcquisition();
        final QualityCard qualityCard = createQualityCard();
        qualityCard.setRules(Arrays.asList(
                createRuleWithoutCondition(QualityTag.ERROR),
                createRuleWithoutCondition(QualityTag.WARNING)));
        mockExaminations(acquisition.getExamination());

        qualityCardService.applyQualityCardOnStudy(qualityCard, false);

        Assertions.assertEquals(QualityTag.ERROR, acquisition.getQualityTag());
    }

    @Test
    public void applyQualityCardOnStudyKeepsMostSevereTagWhenErrorAppliedLastTest() throws Exception {
        final DatasetAcquisition acquisition = createAcquisition();
        final QualityCard qualityCard = createQualityCard();
        qualityCard.setRules(Arrays.asList(
                createRuleWithoutCondition(QualityTag.WARNING),
                createRuleWithoutCondition(QualityTag.ERROR)));
        mockExaminations(acquisition.getExamination());

        qualityCardService.applyQualityCardOnStudy(qualityCard, false);

        Assertions.assertEquals(QualityTag.ERROR, acquisition.getQualityTag());
    }

    /**
     * The study tree must not be loaded at once (one dataset file per DICOM instance): each
     * examination is loaded on its own, when processed.
     */
    @Test
    public void applyQualityCardOnStudyLoadsOneExaminationAtATimeTest() throws Exception {
        final QualityCard qualityCard = createQualityCard();
        qualityCard.setRules(List.of(createRuleWithoutCondition(QualityTag.WARNING)));
        mockExaminations(createExamination(30L), createExamination(10L), createExamination(20L));

        qualityCardService.applyQualityCardOnStudy(qualityCard, true);

        Mockito.verify(studyRepository).findById(STUDY_ID);
        Mockito.verifyNoMoreInteractions(studyRepository);
        for (Long examinationId : List.of(10L, 20L, 30L)) {
            Mockito.verify(examinationRepository).findByIdWithAcquisitions(examinationId);
        }
        Mockito.verify(datasetAcquisitionService, Mockito.times(3)).update(Mockito.anyList());
    }

    @Test
    public void testQualityCardOnSampleOnlyLoadsSampledExaminationsTest() throws Exception {
        final QualityCard qualityCard = createQualityCard();
        qualityCard.setRules(List.of(createRuleWithoutCondition(QualityTag.WARNING)));
        mockExaminations(createExamination(30L), createExamination(10L), createExamination(20L));

        qualityCardService.applyQualityCardOnStudy(qualityCard, false, 1, 2);

        // examinations are sorted by id: [10, 20, 30], the sample [1, 2[ is examination 20
        Mockito.verify(examinationRepository).findByIdWithAcquisitions(20L);
        Mockito.verify(examinationRepository, Mockito.never()).findByIdWithAcquisitions(10L);
        Mockito.verify(examinationRepository, Mockito.never()).findByIdWithAcquisitions(30L);
        Mockito.verify(datasetAcquisitionService, Mockito.never()).update(Mockito.anyList());
    }

    /**
     * Testing a quality card must never save the quality tags set on the loaded acquisitions:
     * its transaction is read-only, so Hibernate does not flush them.
     */
    @Test
    public void examinationTransactionsTest() throws Exception {
        Transactional apply = QualityCardServiceImpl.class.getMethod("applyQualityCardOnExamination",
                QualityCard.class, Long.class, ShanoirEvent.class).getAnnotation(Transactional.class);
        Transactional test = QualityCardServiceImpl.class.getMethod("testQualityCardOnExamination",
                QualityCard.class, Long.class, ShanoirEvent.class).getAnnotation(Transactional.class);
        Assertions.assertNotNull(apply);
        Assertions.assertFalse(apply.readOnly());
        Assertions.assertNotNull(test);
        Assertions.assertTrue(test.readOnly());
    }

    private QualityCard createQualityCard() {
        final QualityCard qualityCard = new QualityCard();
        qualityCard.setId(QUALITY_CARD_ID);
        qualityCard.setName(QUALITY_CARD_NAME);
        qualityCard.setStudyId(STUDY_ID);
        qualityCard.setToCheckAtImport(true);
        qualityCard.setRules(new ArrayList<QualityCardRule>());
        return qualityCard;
    }

    private DatasetAcquisition createAcquisition() {
        final DatasetAcquisition acquisition = new GenericDatasetAcquisition();
        acquisition.setId(1L);
        final Examination examination = new Examination();
        examination.setId(1L);
        examination.setDatasetAcquisitions(new ArrayList<>(List.of(acquisition)));
        acquisition.setExamination(examination);
        return acquisition;
    }

    private QualityCardRule createRuleWithoutCondition(QualityTag tag) {
        final QualityCardRule rule = new QualityCardRule();
        rule.setQualityTag(tag);
        return rule;
    }

    private Study createStudy() {
        final Study study = new Study();
        study.setId(STUDY_ID);
        return study;
    }

    private Examination createExamination(Long id) {
        final Examination examination = new Examination();
        examination.setId(id);
        final DatasetAcquisition acquisition = new GenericDatasetAcquisition();
        acquisition.setId(id * 100);
        acquisition.setExamination(examination);
        examination.setDatasetAcquisitions(new ArrayList<>(List.of(acquisition)));
        return examination;
    }

    private void mockExaminations(Examination... examinations) {
        List<Long> ids = new ArrayList<>();
        for (Examination examination : examinations) {
            ids.add(examination.getId());
            given(examinationRepository.findByIdWithAcquisitions(examination.getId())).willReturn(Optional.of(examination));
        }
        given(examinationRepository.findIdsByStudyId(STUDY_ID)).willReturn(ids);
    }

}
