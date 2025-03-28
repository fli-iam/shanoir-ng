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

package org.shanoir.ng.examination;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.shanoir.ng.dicom.web.StudyInstanceUIDHandler;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationServiceImpl;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.BDDMockito.given;

/**
 * Examination service test.
 *
 * @author ifakhfakh
 *
 */

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ExaminationServiceTest {

    private static final Long EXAMINATION_ID = 1L;
    private static final String UPDATED_EXAMINATION_COMMENT = "examination 2";

    @Mock
    private ExaminationRepository examinationRepository;

    @Mock
    private MicroserviceRequestsService microservicesRequestsService;

    @InjectMocks
    private ExaminationServiceImpl examinationService;

    @MockBean
    private StudyRightsService rightsService;

    @Mock
    private ShanoirEventService eventService;

    @Mock
    private SubjectRepository subjectService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private StudyInstanceUIDHandler studyInstanceUIDHandler;

    @Mock
    private DICOMWebService dicomWebService;


    @BeforeEach
    public void setup() throws ShanoirException {
        // given(examinationRepository.findByStudy_IdIn(Mockito.anyListOf(Long.class), Mockito.any(Pageable.class)))
        //         .willReturn(Arrays.asList(ModelsUtil.createExamination()));
        given(examinationRepository.findById(EXAMINATION_ID)).willReturn(Optional.of(ModelsUtil.createExamination()));
        given(examinationRepository.save(Mockito.any(Examination.class))).willReturn(ModelsUtil.createExamination());
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void deleteByIdTest() throws ShanoirException, SolrServerException, IOException, RestServiceException {
        examinationService.deleteById(EXAMINATION_ID, null);
        Mockito.verify(examinationRepository, Mockito.times(1)).deleteById(Mockito.anyLong());
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void findByIdTest() throws ShanoirException {
        final Examination examination = examinationService.findById(EXAMINATION_ID);
        Assertions.assertNotNull(examination);
        Assertions.assertTrue(ModelsUtil.EXAMINATION_NOTE.equals(examination.getNote()));
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void saveTest() throws ShanoirException {
        Mockito.when(subjectService.findById(Mockito.anyLong())).thenReturn(Optional.of(new Subject(1L, "name")));
        examinationService.save(createExamination());

        Mockito.verify(examinationRepository, Mockito.times(1)).save(Mockito.any(Examination.class));
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void updateTest() throws ShanoirException {
        final Examination updatedExamination = examinationService.update(createExamination());
        Assertions.assertNotNull(updatedExamination);
        Assertions.assertTrue(UPDATED_EXAMINATION_COMMENT.equals(updatedExamination.getComment()));

        Mockito.verify(examinationRepository, Mockito.times(1)).save(Mockito.any(Examination.class));
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void updateAsAdminTest() throws ShanoirException {
        // We update the subject -> admin -> SUCCESS
        Examination updatedExam = createExamination();
        updatedExam.setSubject(new Subject(5L, "new name"));
        final Examination updatedExamination = examinationService.update(updatedExam);

        Assertions.assertNotNull(updatedExamination);
        Assertions.assertTrue(UPDATED_EXAMINATION_COMMENT.equals(updatedExamination.getComment()));
        Mockito.verify(examinationRepository, Mockito.times(1)).save(Mockito.any(Examination.class));
    }

    private Examination createExamination() {
        Examination oldExam  = ModelsUtil.createExamination();

        final Examination examination = new Examination();
        examination.setId(EXAMINATION_ID);
        examination.setComment(UPDATED_EXAMINATION_COMMENT);
        examination.setCenterId(oldExam.getCenterId());
        examination.setStudy(oldExam.getStudy());
        examination.setSubject(oldExam.getSubject());

        return examination;
    }

}
