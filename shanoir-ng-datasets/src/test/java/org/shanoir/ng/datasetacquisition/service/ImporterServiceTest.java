package org.shanoir.ng.datasetacquisition.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.dcm4che3.data.Attributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.DatasetFile;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Patient;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.dto.Study;
import org.shanoir.ng.importer.service.DatasetAcquisitionContext;
import org.shanoir.ng.importer.service.DicomPersisterService;
import org.shanoir.ng.importer.service.ImporterMailService;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.importer.service.QualityService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.shared.service.SubjectService;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.dto.QualityCardResultEntry;
import org.shanoir.ng.studycard.model.ExaminationData;
import org.shanoir.ng.studycard.model.QualityCard;
import org.shanoir.ng.studycard.service.QualityCardService;
import org.shanoir.ng.utils.Utils;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ImporterServiceTest {

    @InjectMocks
    @Spy
    ImporterService service = new ImporterService();

    @Mock
    private ExaminationService examinationService;

    @Mock
    private ExaminationRepository examinationRepository;

    @Mock
    private DatasetAcquisitionContext datasetAcquisitionContext;

    @Mock
    private DatasetAcquisitionService datasetAcquisitionService;

    @Mock
    private DicomPersisterService dicomPersisterService;

    @Mock
    private DicomProcessing dicomProcessing;

    @Mock
    private ShanoirEventService taskService;

    @Mock
    StudyUserRightsRepository studyUserRightRepo;

    @Mock
    QualityCardService qualityCardService;

    @Mock
    QualityService qualityService;

    @Mock
    private DatasetAcquisitionRepository datasetAcquisitionRepository;

    @Mock
    private ImporterMailService importerServiceMail;

    @Mock
    private SubjectService subjectService;

    private Examination exam;

    @BeforeEach
    public void setUp() throws IOException {
        exam = new Examination();
        exam.setExaminationDate(LocalDate.now());
        exam.setId(1L);
        given(examinationService.findById(Mockito.anyLong())).willReturn(exam);
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
    public void createAllDatasetAcquisition() throws Exception {
        // GIVEN an importJob with series and patients
        List<Patient> patients = new ArrayList<Patient>();
        Patient patient = new Patient();
        List<Study> studies = new ArrayList<Study>();
        Study study = new Study();
        List<Serie> series = new ArrayList<Serie>();
        Serie serie = new Serie();
        serie.setSelected(Boolean.TRUE);
        serie.setModality("smthing");
        List<Dataset> datasets = new ArrayList<Dataset>();
        Dataset dataset = new Dataset();
        List<ExpressionFormat> expressionFormats = new ArrayList<ExpressionFormat>();
        ExpressionFormat expressionFormat = new ExpressionFormat();
        List<DatasetFile> datasetFiles = new ArrayList<DatasetFile>();
        DatasetFile datasetFile = new DatasetFile();
        datasetFile.setPath("/fakePath");

        datasetFiles.add(datasetFile);
        expressionFormat.setDatasetFiles(datasetFiles);
        expressionFormats.add(expressionFormat);
        dataset.setExpressionFormats(expressionFormats);
        datasets.add(dataset);
        serie.setDatasets(datasets);
        serie.setIsEnhanced(Boolean.FALSE);
        series.add(serie);
        study.setSeries(series);
        studies.add(study);
        patient.setStudies(studies);
        patients.add(patient);

        ImportJob importJob = new ImportJob();
        importJob.setPatients(patients);
        importJob.setArchive("/tmp/bruker/convert/brucker/blabla.zip");
        importJob.setExaminationId(Long.valueOf(2));
        importJob.setSubjectName("subjectName");
        importJob.setStudyName("studyName");
        importJob.setStudyId(1L);
        importJob.setStudyCardName("SCname");
        importJob.setShanoirEvent(new ShanoirEvent());
        importJob.setFromShanoirUploader(false);

        org.shanoir.ng.shared.model.Subject subject = new org.shanoir.ng.shared.model.Subject();

        Examination examination = new Examination();
        examination.setId(2L);
        examination.setExaminationDate(LocalDate.now());
        examination.setDatasetAcquisitions(new ArrayList<>());
        examination.setSubject(subject);
        examination.setStudy(new org.shanoir.ng.shared.model.Study());
        examination.getStudy().setId(1L);
        examination.getStudy().setSubjectStudyList(new ArrayList<>());
        DatasetAcquisition datasetAcq = new MrDatasetAcquisition();

        ExaminationData examData = new ExaminationData(examination);

        QualityCardResult qualityResult = new QualityCardResult();
        QualityCardResultEntry entry = new QualityCardResultEntry();
        entry.setTagSet(QualityTag.VALID);
        qualityResult.add(entry);

        //DatasetAcquisition datasetAcquisition = datasetAcquisitionContext.generateDatasetAcquisitionForSerie(serie, rank, importJob, dicomAttributes);

        try (MockedStatic<DicomProcessing> dicomProcessingMock = Mockito.mockStatic(DicomProcessing.class)) {
            dicomProcessingMock
                    .when(() -> DicomProcessing.getDicomObjectAttributes(serie.getFirstDatasetFileForCurrentSerie(), serie.getIsEnhanced()))
                    .thenReturn(new Attributes());

            when(datasetAcquisitionContext.generateDatasetAcquisitionForSerie(Mockito.eq(serie), Mockito.eq(0), Mockito.eq(importJob), Mockito.any())).thenReturn(datasetAcq);
            when(studyUserRightRepo.findByStudyId(importJob.getStudyId())).thenReturn(Collections.emptyList());
            when(examinationRepository.findById(importJob.getExaminationId())).thenReturn(Optional.of(examination));
            when(qualityCardService.findByStudy(examination.getStudyId())).thenReturn(Utils.toList(new QualityCard())); // TODO perform quality card tests
            when(qualityService.checkQuality(Mockito.eq(examData), Mockito.eq(importJob), any())).thenReturn(qualityResult);
            when(qualityService.retrieveQualityCardResult(importJob)).thenReturn(qualityResult);

            // WHEN we treat this importjob
            assertNotNull(qualityResult);
            service.createAllDatasetAcquisition(importJob, 1L);

            ArgumentCaptor<ShanoirEvent> argument = ArgumentCaptor.forClass(ShanoirEvent.class);
            Mockito.verify(taskService, Mockito.times(4)).publishEvent(argument.capture());

            List<ShanoirEvent> values = argument.getAllValues();
            ShanoirEvent task = values.get(0);
            assertTrue(task.getStatus() == 1);
            // NOTE: This test is important as we use the message to send an mail to study admin further.
            // PLEASE do not change sucess message OR change it accordingly in emailServiceImpl.
            assertEquals("[studyName (n°1)] Successfully created datasets for subject [subjectName] in examination [2]", task.getMessage());

            // THEN datasets are created
            // Check what we save at the end
            verify(datasetAcquisitionService).createAll(any());
            //verify(datasetAcquisitionService).create(datasetAcq);
            verify(dicomPersisterService).persistAllForSerie(any());

            assertNotNull(datasetAcq);

            // AN archive is not referenced in the examination (file not existing)
            List<String> extradata = datasetAcq.getExamination().getExtraDataFilePathList();
            assertNull(extradata);
        }
    }
}