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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
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
import org.shanoir.ng.dataset.modality.CtDataset;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.repository.DatasetExpressionRepository;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.ct.CtDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.service.DatasetAcquisitionService;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.web.service.DICOMWebService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.model.Center;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.StudyCenter;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.CenterRepository;
import org.shanoir.ng.shared.repository.StudyCenterRepository;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.shared.service.SubjectService;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * A STOW-RS sender repeats instances, after a timeout or when an extraction is
 * restarted: that may neither make the request fail nor change what is stored.
 */
@ExtendWith(MockitoExtension.class)
class DicomImporterServiceRepeatedInstanceTest {

    private static final Long STUDY_ID = 1L;
    private static final Long SUBJECT_ID = 10L;
    private static final Long CENTER_ID = 5L;
    private static final String STUDY_INSTANCE_UID = "1.2.826.0.1.1";
    private static final String SERIES_INSTANCE_UID = "1.2.826.0.1.1.1";
    private static final String SOP_INSTANCE_UID = "1.2.826.0.1.1.1.1";
    private static final String PACS_PATH = "http://pacs:8080/rs/" + STUDY_INSTANCE_UID
            + "/series/" + SERIES_INSTANCE_UID + "/instances/" + SOP_INSTANCE_UID;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private DatasetSecurityService datasetSecurityService;

    @Mock
    private SubjectService subjectService;

    @Mock
    private CenterRepository centerRepository;

    @Mock
    private StudyCenterRepository studyCenterRepository;

    @Mock
    private ExaminationService examinationService;

    @Mock
    private DatasetAcquisitionService acquisitionService;

    @Mock
    private DatasetService datasetService;

    @Mock
    private DatasetExpressionRepository datasetExpressionRepository;

    @Mock
    private DICOMWebService dicomWebService;

    @Mock
    private SolrService solrService;

    @InjectMocks
    private DicomImporterService dicomImporterService;

    private Attributes metaInformationAttributes = new Attributes();

    private Attributes attributes = new Attributes();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dicomImporterService, "dcm4cheeProtocol", "http://");
        ReflectionTestUtils.setField(dicomImporterService, "dcm4cheeHost", "pacs");
        ReflectionTestUtils.setField(dicomImporterService, "dcm4cheePortWeb", "8080");
        ReflectionTestUtils.setField(dicomImporterService, "dicomWebRS", "/rs");
        attributes.setString(Tag.DeidentificationMethod, VR.LO, "Karnak");
        attributes.setString(Tag.ClinicalTrialProtocolID, VR.LO, STUDY_ID.toString());
        attributes.setString(Tag.PatientName, VR.PN, "subject01");
        attributes.setString(Tag.StudyInstanceUID, VR.UI, STUDY_INSTANCE_UID);
        attributes.setString(Tag.SeriesInstanceUID, VR.UI, SERIES_INSTANCE_UID);
        attributes.setString(Tag.SOPInstanceUID, VR.UI, SOP_INSTANCE_UID);
        Study study = new Study();
        study.setId(STUDY_ID);
        when(studyRepository.findById(STUDY_ID)).thenReturn(Optional.of(study));
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_IMPORT.name())).thenReturn(true);
    }

    @Test
    void instanceAlreadyImportedIsSkippedWithoutError() throws Exception {
        attributes.setString(Tag.Modality, VR.CS, "CT");
        Subject subject = new Subject();
        subject.setId(SUBJECT_ID);
        when(subjectService.findByNameAndStudyId(anyString(), any())).thenReturn(subject);
        Center center = new Center();
        center.setId(CENTER_ID);
        StudyCenter studyCenter = new StudyCenter();
        studyCenter.setCenter(center);
        when(centerRepository.findFirstByNameContainingOrderByIdAsc(any())).thenReturn(Optional.of(center));
        when(studyCenterRepository.findByStudy_IdAndCenter_Id(STUDY_ID, CENTER_ID)).thenReturn(Optional.of(studyCenter));
        Examination examination = new Examination();
        examination.setId(7L);
        examination.setStudyInstanceUID(STUDY_INSTANCE_UID);
        when(examinationService.findBySubjectIdStudyId(SUBJECT_ID, STUDY_ID)).thenReturn(List.of(examination));
        CtDatasetAcquisition acquisition = new CtDatasetAcquisition();
        acquisition.setDatasets(List.of(datasetHoldingTheInstance()));
        when(acquisitionService.findByExaminationAndSeriesInstanceUIDWithDatasets(7L, SERIES_INSTANCE_UID))
                .thenReturn(Optional.of(acquisition));

        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn("testUser");
            assertTrue(dicomImporterService.importDicom(metaInformationAttributes, attributes));
        }

        // nothing is created or sent to the PACS a second time
        verifyNoInteractions(datasetService, datasetExpressionRepository, dicomWebService, solrService);
    }

    private static Dataset datasetHoldingTheInstance() {
        DatasetFile file = new DatasetFile();
        file.setPath(PACS_PATH);
        DatasetExpression expression = new DatasetExpression();
        expression.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);
        expression.setDatasetFiles(List.of(file));
        Dataset dataset = new CtDataset();
        dataset.setDatasetExpressions(List.of(expression));
        return dataset;
    }

}
