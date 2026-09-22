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
        given(studyRepository.findByIdWithDatasetsAndDatasetFilePaths(STUDY_ID))
                .willReturn(Optional.of(createStudyWithAcquisition(acquisition)));

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
        given(studyRepository.findByIdWithDatasetsAndDatasetFilePaths(STUDY_ID))
                .willReturn(Optional.of(createStudyWithAcquisition(acquisition)));

        qualityCardService.applyQualityCardOnStudy(qualityCard, false);

        Assertions.assertEquals(QualityTag.ERROR, acquisition.getQualityTag());
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
        acquisition.setExamination(new Examination());
        return acquisition;
    }

    private QualityCardRule createRuleWithoutCondition(QualityTag tag) {
        final QualityCardRule rule = new QualityCardRule();
        rule.setQualityTag(tag);
        return rule;
    }

    private Study createStudyWithAcquisition(DatasetAcquisition acquisition) {
        final Study study = new Study();
        study.setId(STUDY_ID);
        acquisition.getExamination().setDatasetAcquisitions(new ArrayList<>(List.of(acquisition)));
        study.setExaminations(new ArrayList<>(List.of(acquisition.getExamination())));
        return study;
    }

}
