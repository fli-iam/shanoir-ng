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

import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.dicom.web.StudyInstanceUIDAndSubjectNameHandler;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.study.rights.ampq.RabbitMqStudyUserService;
import org.shanoir.ng.studycard.model.StudyCard;
import org.shanoir.ng.studycard.repository.StudyCardRepository;
import org.shanoir.ng.studycard.service.StudyCardServiceImpl;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Study card service test.
 *
 * @author msimon
 *
 */
@SpringBootTest
@ActiveProfiles("test")
public class StudyCardServiceTest {

    private static final Long TEMPLATE_ID = 1L;
    private static final String UPDATED_STUDYCARD_DATA = "StudyCard1";

    @MockitoBean
    private StudyCardRepository studyCardRepository;

    @MockitoBean
    private StudyInstanceUIDAndSubjectNameHandler studyInstanceUIDHandler;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @MockitoBean
    private RabbitMqStudyUserService rabbitMqStudyUserService;

    @Autowired
    private StudyCardServiceImpl studyCardService;

    @BeforeEach
    public void setup() {
        given(studyCardRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createStudyCard()));
        given(studyCardRepository.findById(TEMPLATE_ID)).willReturn(Optional.of(ModelsUtil.createStudyCard()));
        given(studyCardRepository.save(Mockito.any(StudyCard.class))).willReturn(ModelsUtil.createStudyCard());
    }

    @Test
    public void updateTest() throws EntityNotFoundException {
        studyCardService.update(createStudyCard());
        Mockito.verify(studyCardRepository, Mockito.times(1)).save(Mockito.any(StudyCard.class));
    }

    private StudyCard createStudyCard() {
        final StudyCard studyCard = new StudyCard();
        studyCard.setId(TEMPLATE_ID);
        studyCard.setName(UPDATED_STUDYCARD_DATA);
        return studyCard;
    }

}
