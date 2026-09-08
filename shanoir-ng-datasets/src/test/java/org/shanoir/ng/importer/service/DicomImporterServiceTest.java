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

package org.shanoir.ng.importer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.CenterRepository;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.shared.service.StudyService;
import org.shanoir.ng.shared.service.SubjectService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for the CAN_IMPORT rights check on study level in DicomImporterService.
 *
 * @author Adam Fragkiadakis
 */
@ExtendWith(MockitoExtension.class)
public class DicomImporterServiceTest {

    private static final Long STUDY_ID = 1L;

    private static final String USER_NAME = "testUser";

    @Mock
    private StudyService studyService;

    @Mock
    private DatasetSecurityService datasetSecurityService;

    @Mock
    private SubjectService subjectService;

    @Mock
    private CenterRepository centerRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DicomImporterService dicomImporterService;

    private Attributes metaInformationAttributes;

    private Attributes attributes;

    private Study study;

    @BeforeEach
    public void setup() {
        metaInformationAttributes = new Attributes();
        attributes = new Attributes();
        attributes.setString(Tag.DeidentificationMethod, VR.LO, "Karnak");
        attributes.setString(Tag.ClinicalTrialProtocolID, VR.LO, STUDY_ID.toString());
        attributes.setString(Tag.Modality, VR.CS, "MR");
        attributes.setString(Tag.PatientName, VR.PN, "subject01");
        study = new Study();
        study.setId(STUDY_ID);
        when(studyService.findById(STUDY_ID)).thenReturn(study);
    }

    @Test
    public void importDicomRefusedWithoutCanImportRight() {
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_IMPORT.name())).thenReturn(false);
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn(USER_NAME);
            RestServiceException exception = assertThrows(RestServiceException.class,
                    () -> dicomImporterService.importDicom(metaInformationAttributes, attributes, "MR"));
            assertEquals(HttpStatus.FORBIDDEN.value(), exception.getErrorModel().getCode());
        }
        verifyNoInteractions(subjectService);
    }

    @Test
    public void importDicomProceedsWithCanImportRight() throws Exception {
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_IMPORT.name())).thenReturn(true);
        Subject subject = new Subject();
        subject.setId(10L);
        when(subjectService.findByNameAndStudyId(anyString(), any())).thenReturn(subject);
        when(centerRepository.findFirstByNameContainingOrderByIdAsc(any())).thenReturn(Optional.empty());
        // No center found and RabbitMQ answers null: proves the import continued
        // after the rights check, until the center creation
        RestServiceException exception = assertThrows(RestServiceException.class,
                () -> dicomImporterService.importDicom(metaInformationAttributes, attributes, "MR"));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.getErrorModel().getCode());
        verify(subjectService).findByNameAndStudyId("subject01", STUDY_ID);
    }

}
