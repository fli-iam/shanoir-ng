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
import org.shanoir.ng.dicom.web.StudyInstanceUIDAndSubjectNameHandler;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.ng.studycard.model.rule.QualityCardRule;
import org.shanoir.ng.studycard.repository.QualityCardRepository;
import org.shanoir.ng.studycard.service.QualityCardServiceImpl;
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

    @MockBean
    private StudyInstanceUIDAndSubjectNameHandler studyInstanceUIDHandler;

    @InjectMocks
    private QualityCardServiceImpl qualityCardService;

    @BeforeEach
    public void setup() {
        given(qualityCardRepository.findAll()).willReturn(Arrays.asList(createQualityCard()));
        given(qualityCardRepository.findById(QUALITY_CARD_ID)).willReturn(Optional.of(createQualityCard()));
        given(qualityCardRepository.findByStudyId(STUDY_ID)).willReturn(Arrays.asList(createQualityCard()));
        given(qualityCardRepository.findByStudyIdIn(Collections.singletonList(STUDY_ID)))
                .willReturn(Arrays.asList(createQualityCard()));
        given(qualityCardRepository.findByName(QUALITY_CARD_NAME)).willReturn(createQualityCard());
        given(qualityCardRepository.save(Mockito.any(QualityCard.class))).willReturn(createQualityCard());
    }

    @Test
    public void deleteByIdTest() throws EntityNotFoundException, MicroServiceCommunicationException {
        qualityCardService.deleteById(QUALITY_CARD_ID);
        Mockito.verify(qualityCardRepository, Mockito.times(1)).deleteById(QUALITY_CARD_ID);
    }

    @Test
    public void deleteByIdNotFoundTest() {
        given(qualityCardRepository.findById(99L)).willReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> qualityCardService.deleteById(99L));
        Mockito.verify(qualityCardRepository, Mockito.never()).deleteById(Mockito.anyLong());
    }

    @Test
    public void findAllTest() {
        final List<QualityCard> qualityCards = qualityCardService.findAll();
        Assertions.assertNotNull(qualityCards);
        Assertions.assertEquals(1, qualityCards.size());

        Mockito.verify(qualityCardRepository, Mockito.times(1)).findAll();
    }

    @Test
    public void findByIdTest() {
        final QualityCard qualityCard = qualityCardService.findById(QUALITY_CARD_ID);
        Assertions.assertNotNull(qualityCard);
        Assertions.assertEquals(QUALITY_CARD_NAME, qualityCard.getName());

        Mockito.verify(qualityCardRepository, Mockito.times(1)).findById(QUALITY_CARD_ID);
    }

    @Test
    public void findByIdNotFoundTest() {
        given(qualityCardRepository.findById(99L)).willReturn(Optional.empty());

        final QualityCard qualityCard = qualityCardService.findById(99L);
        Assertions.assertNull(qualityCard);
    }

    @Test
    public void saveTest() throws MicroServiceCommunicationException {
        final QualityCard saved = qualityCardService.save(createQualityCard());
        Assertions.assertNotNull(saved);

        Mockito.verify(qualityCardRepository, Mockito.times(1)).save(Mockito.any(QualityCard.class));
    }

    @Test
    public void searchTest() {
        final List<QualityCard> qualityCards = qualityCardService.search(Collections.singletonList(STUDY_ID));
        Assertions.assertNotNull(qualityCards);
        Assertions.assertEquals(1, qualityCards.size());

        Mockito.verify(qualityCardRepository, Mockito.times(1)).findByStudyIdIn(Collections.singletonList(STUDY_ID));
    }

    @Test
    public void updateTest() throws EntityNotFoundException, MicroServiceCommunicationException {
        final QualityCard update = createQualityCard();
        update.setName(UPDATED_QUALITY_CARD_NAME);

        final QualityCard updatedQualityCard = qualityCardService.update(update);
        Assertions.assertNotNull(updatedQualityCard);
        Assertions.assertEquals(UPDATED_QUALITY_CARD_NAME, updatedQualityCard.getName());
        Assertions.assertEquals(STUDY_ID, updatedQualityCard.getStudyId());

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

    @Test
    public void findByStudyTest() {
        final List<QualityCard> qualityCards = qualityCardService.findByStudy(STUDY_ID);
        Assertions.assertNotNull(qualityCards);
        Assertions.assertEquals(1, qualityCards.size());

        Mockito.verify(qualityCardRepository, Mockito.times(1)).findByStudyId(STUDY_ID);
    }

    @Test
    public void findByNameTest() {
        final QualityCard qualityCard = qualityCardService.findByName(QUALITY_CARD_NAME);
        Assertions.assertNotNull(qualityCard);
        Assertions.assertEquals(QUALITY_CARD_NAME, qualityCard.getName());

        Mockito.verify(qualityCardRepository, Mockito.times(1)).findByName(QUALITY_CARD_NAME);
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

}
