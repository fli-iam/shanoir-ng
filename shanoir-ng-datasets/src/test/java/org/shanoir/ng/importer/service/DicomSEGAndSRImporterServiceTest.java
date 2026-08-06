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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.dicom.web.SeriesInstanceUIDHandler;
import org.shanoir.ng.dicom.web.StudyInstanceUIDAndSubjectNameHandler;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.http.HttpStatus;

/**
 * Tests for the CAN_ANNOTATE / CAN_ANNOTATE_REVIEW rights check on study level in DicomSEGAndSRImporterService.
 *
 * @author Adam Fragkiadakis
 */
@ExtendWith(MockitoExtension.class)
public class DicomSEGAndSRImporterServiceTest {

    private static final Long STUDY_ID = 1L;

    private static final String USER_NAME = "testUser";

    private static final String STUDY_INSTANCE_UID = "1.2.840.113619.2.1.1";

    private static final String SERIES_INSTANCE_UID = "1.2.840.113619.2.1.2";

    private static final String SOP_INSTANCE_UID = "1.2.840.113619.2.1.3";

    private static final Long SUBJECT_ID = 10L;

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
    public void importDicomSEGAndSRRefusedWithoutCanAnnotateOrCanImportRight() {
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE_REVIEW.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_IMPORT.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ADMINISTRATE.name())).thenReturn(false);
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

    @Test
    public void importDicomSEGAndSRProceedsWithCanAnnotateReviewRight() throws Exception {
        // CAN_ANNOTATE_REVIEW grants the annotation rights of CAN_ANNOTATE
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE_REVIEW.name())).thenReturn(true);
        // No ReferencedSeriesSequence in the SEG: the import continues past the rights
        // check and only stops later, when the source dataset cannot be found (returns false)
        boolean result = dicomSEGAndSRImporterService.importDicomSEGAndSR(metaInformationAttributes, datasetAttributes, "SEG", true);
        assertFalse(result);
        verify(datasetSecurityService).hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE_REVIEW.name());
        // The source dataset was not found, so nothing is persisted
        verifyNoInteractions(datasetService);
    }

    @Test
    public void importDicomSEGAndSRProceedsWithCanImportRightOnly() throws Exception {
        // A user with CAN_IMPORT but not CAN_ANNOTATE is also allowed to import annotations
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE_REVIEW.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_IMPORT.name())).thenReturn(true);
        // No ReferencedSeriesSequence in the SEG: the import continues past the rights
        // check and only stops later, when the source dataset cannot be found (returns false)
        boolean result = dicomSEGAndSRImporterService.importDicomSEGAndSR(metaInformationAttributes, datasetAttributes, "SEG", true);
        assertFalse(result);
        verify(datasetSecurityService).hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_IMPORT.name());
        // The source dataset was not found, so nothing is persisted
        verifyNoInteractions(datasetService);
    }

    @Test
    public void importDicomSEGAndSRProceedsWithCanAdministrateRightOnly() throws Exception {
        // a study administrator (study-scoped admin) is also allowed to import annotations
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE_REVIEW.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_IMPORT.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ADMINISTRATE.name())).thenReturn(true);
        // No ReferencedSeriesSequence in the SEG: the import continues past the rights
        // check and only stops later, when the source dataset cannot be found (returns false)
        boolean result = dicomSEGAndSRImporterService.importDicomSEGAndSR(metaInformationAttributes, datasetAttributes, "SEG", true);
        assertFalse(result);
        verify(datasetSecurityService).hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ADMINISTRATE.name());
        // The source dataset was not found, so nothing is persisted
        verifyNoInteractions(datasetService);
    }

    @Test
    public void importDicomSEGAndSRAssignsUsernameWithCanAnnotateRight() throws Exception {
        wireSourceDatasetAndSegReferences();
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(true);
        when(datasetService.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Dataset> datasetCaptor = ArgumentCaptor.forClass(Dataset.class);
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn(USER_NAME);
            boolean result = dicomSEGAndSRImporterService.importDicomSEGAndSR(metaInformationAttributes, datasetAttributes, "SEG", true);
            assertTrue(result);
        }
        verify(datasetService).create(datasetCaptor.capture());
        // The annotator is recorded as the owner of the created annotation dataset
        assertEquals(USER_NAME, datasetCaptor.getValue().getUsername());
    }

    @Test
    public void importDicomSEGAndSRAssignsUsernameWithCanAnnotateReviewRight() throws Exception {
        wireSourceDatasetAndSegReferences();
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE_REVIEW.name())).thenReturn(true);
        when(datasetService.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Dataset> datasetCaptor = ArgumentCaptor.forClass(Dataset.class);
        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn(USER_NAME);
            boolean result = dicomSEGAndSRImporterService.importDicomSEGAndSR(metaInformationAttributes, datasetAttributes, "SEG", true);
            assertTrue(result);
        }
        verify(datasetService).create(datasetCaptor.capture());
        // The reviewer-annotator is recorded as the owner of the created annotation dataset
        assertEquals(USER_NAME, datasetCaptor.getValue().getUsername());
    }

    @Test
    public void importDicomSEGAndSRDoesNotAssignUsernameWithCanImportRightOnly() throws Exception {
        wireSourceDatasetAndSegReferences();
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE_REVIEW.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_IMPORT.name())).thenReturn(true);
        when(datasetService.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Dataset> datasetCaptor = ArgumentCaptor.forClass(Dataset.class);
        boolean result = dicomSEGAndSRImporterService.importDicomSEGAndSR(metaInformationAttributes, datasetAttributes, "SEG", true);
        assertTrue(result);
        verify(datasetService).create(datasetCaptor.capture());
        // A CAN_IMPORT-only import does not record an annotator
        assertNull(datasetCaptor.getValue().getUsername());
    }

    @Test
    public void importDicomSEGAndSRDoesNotAssignUsernameWithCanAdministrateRightOnly() throws Exception {
        wireSourceDatasetAndSegReferences();
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ANNOTATE_REVIEW.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_IMPORT.name())).thenReturn(false);
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_ADMINISTRATE.name())).thenReturn(true);
        when(datasetService.create(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Dataset> datasetCaptor = ArgumentCaptor.forClass(Dataset.class);
        boolean result = dicomSEGAndSRImporterService.importDicomSEGAndSR(metaInformationAttributes, datasetAttributes, "SEG", true);
        assertTrue(result);
        verify(datasetService).create(datasetCaptor.capture());
        // A study-administrator import behaves like CAN_IMPORT: no annotator is recorded
        assertNull(datasetCaptor.getValue().getUsername());
    }

    /**
     * Wires the examination with a source dataset and adds the matching SEG
     * references to the DICOM attributes, so the import reaches dataset creation.
     */
    private void wireSourceDatasetAndSegReferences() {
        Subject subject = new Subject();
        subject.setId(SUBJECT_ID);
        examination.setSubject(subject);

        DatasetFile file = new DatasetFile();
        file.setPacs(true);
        file.setPath("pacs/" + STUDY_INSTANCE_UID + "/" + SERIES_INSTANCE_UID + "/" + SOP_INSTANCE_UID);
        DatasetExpression expression = new DatasetExpression();
        expression.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);
        expression.setDatasetFiles(new ArrayList<>(List.of(file)));
        MrDataset sourceDataset = new MrDataset();
        sourceDataset.setDatasetExpressions(new ArrayList<>(List.of(expression)));
        MrDatasetAcquisition acquisition = new MrDatasetAcquisition();
        acquisition.setDatasets(new ArrayList<>(List.of(sourceDataset)));
        sourceDataset.setDatasetAcquisition(acquisition);
        examination.setDatasetAcquisitions(new ArrayList<>(List.of(acquisition)));

        // DICOM SEG: ReferencedSeriesSequence -> ReferencedInstanceSequence, matching the source file UIDs
        Attributes instanceItem = new Attributes();
        instanceItem.setString(Tag.ReferencedSOPInstanceUID, VR.UI, SOP_INSTANCE_UID);
        Attributes seriesItem = new Attributes();
        seriesItem.setString(Tag.SeriesInstanceUID, VR.UI, SERIES_INSTANCE_UID);
        Sequence referencedInstanceSequence = seriesItem.newSequence(Tag.ReferencedInstanceSequence, 1);
        referencedInstanceSequence.add(instanceItem);
        Sequence referencedSeriesSequence = datasetAttributes.newSequence(Tag.ReferencedSeriesSequence, 1);
        referencedSeriesSequence.add(seriesItem);
    }

}
