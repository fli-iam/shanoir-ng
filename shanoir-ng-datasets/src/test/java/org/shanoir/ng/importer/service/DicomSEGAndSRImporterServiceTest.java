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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.dicom.web.SeriesInstanceUIDHandler;
import org.shanoir.ng.dicom.web.StudyInstanceUIDAndSubjectNameHandler;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.http.HttpStatus;

/**
 * Tests for the CAN_ANNOTATE rights check on study level in DicomSEGAndSRImporterService.
 *
 * @author afragkia
 */
@ExtendWith(MockitoExtension.class)
public class DicomSEGAndSRImporterServiceTest {

    private static final Long STUDY_ID = 1L;

    private static final String USER_NAME = "testUser";

    private static final String STUDY_INSTANCE_UID = "1.2.840.113619.2.1.1";

    @Mock
    private ExaminationRepository examinationRepository;

    @Mock
    private SolrService solrService;

    @Mock
    private DatasetService datasetService;

    @Mock
    private DatasetSecurityService datasetSecurityService;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private StudyInstanceUIDAndSubjectNameHandler studyInstanceUIDHandler;

    @Mock
    private SeriesInstanceUIDHandler seriesInstanceUIDHandler;

    @Mock
    private DicomImporterService dicomImporterService;

    @InjectMocks
    private DicomSEGAndSRImporterService dicomSEGAndSRImporterService;

    private Attributes metaInformationAttributes;

    private Attributes datasetAttributes;

    private Examination examination;

    @BeforeEach
    public void setup() {
        metaInformationAttributes = new Attributes();
        datasetAttributes = new Attributes();
        datasetAttributes.setString(Tag.StudyInstanceUID, VR.UI, STUDY_INSTANCE_UID);
        Study study = new Study();
        study.setId(STUDY_ID);
        examination = new Examination();
        examination.setStudy(study);
        // nonOhifRequest == true: examination is resolved directly from the repository
        when(examinationRepository.findByStudyInstanceUID(STUDY_INSTANCE_UID)).thenReturn(Optional.of(examination));
    }

    @Test
    public void importDicomSEGAndSRRefusedWithoutCanAnnotateRight() {
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(false);
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn(USER_NAME);
            RestServiceException exception = assertThrows(RestServiceException.class,
                    () -> dicomSEGAndSRImporterService.importDicomSEGAndSR(metaInformationAttributes, datasetAttributes, "SEG", true));
            assertEquals(HttpStatus.FORBIDDEN.value(), exception.getErrorModel().getCode());
        }
        // The import must be refused before any dataset is persisted
        verifyNoInteractions(datasetService);
    }

    @Test
    public void importDicomSEGAndSRProceedsWithCanAnnotateRight() throws Exception {
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(true);
        // No ReferencedSeriesSequence in the SEG: the import continues past the rights
        // check and only stops later, when the source dataset cannot be found (returns false)
        boolean result = dicomSEGAndSRImporterService.importDicomSEGAndSR(metaInformationAttributes, datasetAttributes, "SEG", true);
        assertFalse(result);
        verify(datasetSecurityService).hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name());
        // The source dataset was not found, so nothing is persisted
        verifyNoInteractions(datasetService);
    }

}
