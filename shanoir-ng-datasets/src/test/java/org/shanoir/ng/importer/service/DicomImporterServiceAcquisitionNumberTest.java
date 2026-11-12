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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
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
import org.shanoir.ng.dataset.model.DatasetMetadata;
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
 * One CT series sent over STOW-RS is one acquisition. Its AcquisitionNumber may
 * step inside the series (one value per table segment, for example), and that
 * must not turn every instance after the first segment into a dataset of its
 * own: they all belong to the same stack.
 */
@ExtendWith(MockitoExtension.class)
class DicomImporterServiceAcquisitionNumberTest {

    private static final Long STUDY_ID = 1L;
    private static final Long SUBJECT_ID = 10L;
    private static final Long CENTER_ID = 5L;
    private static final Long EXAMINATION_ID = 7L;
    private static final String STUDY_INSTANCE_UID = "1.2.826.0.1.1";
    private static final String SERIES_INSTANCE_UID = "1.2.826.0.1.1.1";
    private static final String AXIAL = "1\\0\\0\\0\\1\\0";
    private static final double[] AXIAL_VALUES = new double[] {1, 0, 0, 0, 1, 0};
    private static final double[] CORONAL_VALUES = new double[] {1, 0, 0, 0, 0, -1};

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
    private DatasetAcquisitionContext acquisitionContext;

    @Mock
    private DatasetService datasetService;

    @Mock
    private DatasetExpressionRepository datasetExpressionRepository;

    @Mock
    private DICOMWebService dicomWebService;

    @Mock
    private SolrService solrService;

    @Mock
    private StowImportCounters stowImportCounters;

    @InjectMocks
    private DicomImporterService dicomImporterService;

    private final Attributes metaInformationAttributes = new Attributes();

    private DatasetExpression stackExpression;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dicomImporterService, "dcm4cheeProtocol", "http://");
        ReflectionTestUtils.setField(dicomImporterService, "dcm4cheeHost", "pacs");
        ReflectionTestUtils.setField(dicomImporterService, "dcm4cheePortWeb", "8080");
        ReflectionTestUtils.setField(dicomImporterService, "dicomWebRS", "/rs");
        ReflectionTestUtils.setField(dicomImporterService, "seriesProperties", "seriesDescription==*ASL*");
        dicomImporterService.init();
        metaInformationAttributes.setString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian);
        metaInformationAttributes.setString(Tag.MediaStorageSOPClassUID, VR.UI, UID.CTImageStorage);
        Study study = new Study();
        study.setId(STUDY_ID);
        when(studyRepository.findById(STUDY_ID)).thenReturn(Optional.of(study));
        when(datasetSecurityService.hasRightOnStudy(STUDY_ID, StudyUserRight.CAN_IMPORT.name())).thenReturn(true);
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
        examination.setId(EXAMINATION_ID);
        examination.setStudyInstanceUID(STUDY_INSTANCE_UID);
        when(examinationService.findBySubjectIdStudyId(SUBJECT_ID, STUDY_ID)).thenReturn(List.of(examination));
    }

    @Test
    void instancesOfOtherAcquisitionNumbersJoinTheStackOfTheSeries() throws Exception {
        // the series was opened by its first segment: acquisition number 1, one stack
        CtDatasetAcquisition acquisition = acquisitionWithStack();
        when(acquisitionService.findByExaminationAndSeriesInstanceUIDWithDatasets(EXAMINATION_ID, SERIES_INSTANCE_UID))
                .thenReturn(Optional.of(acquisition));
        lenient().when(acquisitionContext.generateFlatDataset(any(), any(), anyInt(), any(), any()))
                .thenAnswer(invocation -> new CtDataset());
        lenient().when(datasetService.create(any(Dataset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn("testUser");
            importInstance("2", 2, AXIAL_VALUES); // second table segment
            importInstance("3", 3, AXIAL_VALUES);
            importInstance("4", 3, AXIAL_VALUES); // a second slice of the third segment
        }

        // no dataset was opened: every instance went into the one stack of the series
        verify(datasetService, never()).create(any(Dataset.class));
        assertEquals(1, acquisition.getDatasets().size());
        assertEquals(4, stackExpression.getDatasetFiles().size());
    }

    @Test
    void instanceWithAnotherOrientationStillOpensItsOwnDataset() throws Exception {
        CtDatasetAcquisition acquisition = acquisitionWithStack();
        when(acquisitionService.findByExaminationAndSeriesInstanceUIDWithDatasets(EXAMINATION_ID, SERIES_INSTANCE_UID))
                .thenReturn(Optional.of(acquisition));
        when(acquisitionContext.generateFlatDataset(any(), any(), anyInt(), any(), any()))
                .thenAnswer(invocation -> newDatasetWithMetadata());
        when(datasetService.create(any(Dataset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<KeycloakUtil> keycloakUtilMock = Mockito.mockStatic(KeycloakUtil.class)) {
            keycloakUtilMock.when(KeycloakUtil::getTokenUserName).thenReturn("testUser");
            importInstance("2", 1, CORONAL_VALUES);
        }

        // the orientation is still part of the key: a reformatted slice is not part of the axial stack
        verify(datasetService, times(1)).create(any(Dataset.class));
        assertEquals(2, acquisition.getDatasets().size());
        assertEquals(1, stackExpression.getDatasetFiles().size());
    }

    private void importInstance(String sopSuffix, int acquisitionNumber, double[] orientation) throws Exception {
        Attributes attributes = new Attributes();
        attributes.setString(Tag.DeidentificationMethod, VR.LO, "Karnak");
        attributes.setString(Tag.ClinicalTrialProtocolID, VR.LO, STUDY_ID.toString());
        attributes.setString(Tag.Modality, VR.CS, "CT");
        attributes.setString(Tag.PatientName, VR.PN, "subject01");
        attributes.setString(Tag.SeriesDescription, VR.LO, "ARTERIEL THORAX ABDO");
        attributes.setString(Tag.StudyInstanceUID, VR.UI, STUDY_INSTANCE_UID);
        attributes.setString(Tag.SeriesInstanceUID, VR.UI, SERIES_INSTANCE_UID);
        attributes.setString(Tag.SOPInstanceUID, VR.UI, SERIES_INSTANCE_UID + "." + sopSuffix);
        attributes.setInt(Tag.AcquisitionNumber, VR.IS, acquisitionNumber);
        attributes.setDouble(Tag.ImageOrientationPatient, VR.DS, orientation);
        dicomImporterService.importDicom(metaInformationAttributes, attributes);
    }

    private CtDatasetAcquisition acquisitionWithStack() {
        DatasetFile first = new DatasetFile();
        first.setPath("http://pacs:8080/rs/" + STUDY_INSTANCE_UID + "/series/" + SERIES_INSTANCE_UID
                + "/instances/" + SERIES_INSTANCE_UID + ".1");
        stackExpression = new DatasetExpression();
        stackExpression.setDatasetExpressionFormat(DatasetExpressionFormat.DICOM);
        stackExpression.setDatasetFiles(new ArrayList<>(List.of(first)));
        Dataset stack = newDatasetWithMetadata();
        stack.setId(100L);
        stack.setDatasetExpressions(new ArrayList<>(List.of(stackExpression)));
        CtDatasetAcquisition acquisition = new CtDatasetAcquisition();
        acquisition.setAcquisitionNumber(1);
        acquisition.setDatasets(new ArrayList<>(List.of(stack)));
        return acquisition;
    }

    private static Dataset newDatasetWithMetadata() {
        DatasetMetadata metadata = new DatasetMetadata();
        metadata.setImageOrientationPatient(AXIAL);
        Dataset dataset = new CtDataset();
        dataset.setOriginMetadata(metadata);
        return dataset;
    }

}
