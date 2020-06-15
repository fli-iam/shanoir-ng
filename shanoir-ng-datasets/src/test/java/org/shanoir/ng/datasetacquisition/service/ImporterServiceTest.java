package org.shanoir.ng.datasetacquisition.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.EegDatasetDTO;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Channel.ChannelType;
import org.shanoir.ng.eeg.model.Event;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.exporter.service.BIDSService;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.EegImportJob;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Patient;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.importer.dto.Study;
import org.shanoir.ng.importer.service.DatasetAcquisitionContext;
import org.shanoir.ng.importer.service.DicomPersisterService;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.utils.KeycloakUtil;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@RunWith(PowerMockRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@PrepareForTest(KeycloakUtil.class)
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
	private DatasetAcquisitionRepository datasetAcquisitionRepository;
	
	@Mock
	private DicomPersisterService dicomPersisterService;

	@Mock
	private BIDSService bidsService;

	@Mock
	private ShanoirEventService taskService;

	@Before
	public void setUp() throws IOException {
        PowerMockito.mockStatic(KeycloakUtil.class);
        given(KeycloakUtil.getKeycloakHeader()).willReturn(null);
	}

	@Test
	public void testCreateEegDataset() throws IOException {
		// Create a complete import job with some files and channels and events...
		EegImportJob importJob = new EegImportJob();
		EegDatasetDTO dataset = new EegDatasetDTO();
		importJob.setDatasets(Collections.singletonList(dataset));
		
		Channel chan = new Channel();
		chan.setHighCutoff(1);
		chan.setLowCutoff(132);
		chan.setName("Charles Aznavourian");
		chan.setNotch(2);
		chan.setReferenceType(ChannelType.EEG);
		chan.setReferenceUnits("Diam's");
		chan.setResolution(2);
		chan.setX(1);
		chan.setX(2);
		chan.setX(3);
		
		Event event = new Event();
		event.setDescription("description");
		event.setType("type");
		event.setType("type");
		
		dataset.setChannels(Collections.singletonList(chan));
		dataset.setEvents(Collections.singletonList(event));
		importJob.setSubjectId(Long.valueOf(1));
		importJob.setFrontStudyId(Long.valueOf(1));
		importJob.setExaminationId(Long.valueOf(1));
		importJob.setFrontAcquisitionEquipmentId(Long.valueOf(1));
		importJob.setSubjectName("What about us");
		dataset.setName("Charles Trenet");
		importJob.setWorkFolder("Julien Clerc");
		importJob.setSubjectName("subjName");
		importJob.setStudyName("studname");
		
		service.createEegDataset(importJob);
		ArgumentCaptor<DatasetAcquisition> datasetAcquisitionCapturer = ArgumentCaptor.forClass(DatasetAcquisition.class);
		
		ArgumentCaptor<ShanoirEvent> argument = ArgumentCaptor.forClass(ShanoirEvent.class);
		Mockito.verify(taskService, Mockito.times(3)).publishEvent(argument.capture());
		
		List<ShanoirEvent> values = argument.getAllValues();
		ShanoirEvent task = values.get(0);
		assertTrue(task.getStatus() == 1);

		// Check what we save at the end
		verify(datasetAcquisitionRepository).save(datasetAcquisitionCapturer.capture());
		DatasetAcquisition hack = datasetAcquisitionCapturer.getValue();
		
		EegDataset ds = (EegDataset) hack.getDatasets().get(0);
		assertEquals(chan, ds.getChannels().get(0));
		assertNotNull(chan.getDataset());
		
		assertEquals(event, ds.getEvents().get(0));
		assertNotNull(event.getDataset());
		
		assertEquals(1, ds.getChannelCount());
		assertEquals(ds.getName(), dataset.getName());
		assertEquals(DatasetExpressionFormat.EEG, ds.getDatasetExpressions().get(0).getDatasetExpressionFormat());
		
		// Check that we save bids folder too
		verify(bidsService).addDataset(any(Examination.class), Mockito.eq(importJob.getSubjectName()), Mockito.eq(importJob.getStudyName()));

		DatasetMetadata metadata = ds.getOriginMetadata();
		assertNotNull(metadata);
		assertEquals(DatasetModalityType.EEG_DATASET, metadata.getDatasetModalityType());
	}

	@Test
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
		
		expressionFormats.add(expressionFormat );
		dataset.setExpressionFormats(expressionFormats);
		datasets.add(dataset );
		serie.setDatasets(datasets );
		series.add(serie);
		study.setSeries(series);
		studies.add(study );
		patient.setStudies(studies );
		patients.add(patient);
		
		ImportJob importJob = new ImportJob();
		importJob.setPatients(patients );
		importJob.setArchive("/tmp/bruker/convert/brucker/blabla.zip");
		importJob.setExaminationId(Long.valueOf(1));
		importJob.setSubjectName("subjectName");
		importJob.setStudyName("studyName");
		importJob.setFrontStudyId(1L);
		
		Examination examination = new Examination();
		examination.setId(2L);
		when(examinationRepository.findOne(importJob.getExaminationId())).thenReturn(examination);
		DatasetAcquisition datasetAcq = new MrDatasetAcquisition();
		when(datasetAcquisitionContext.generateDatasetAcquisitionForSerie(serie, 0, importJob)).thenReturn(datasetAcq );
		
		// WHEN we treat this importjob
		service.createAllDatasetAcquisition(importJob, 1L);
		
		ArgumentCaptor<ShanoirEvent> argument = ArgumentCaptor.forClass(ShanoirEvent.class);
		Mockito.verify(taskService, Mockito.times(3)).publishEvent(argument.capture());
		
		List<ShanoirEvent> values = argument.getAllValues();
		ShanoirEvent task = values.get(0);
		assertTrue(task.getStatus() == 1);
		// NOTE: This test is important as we use the message to send an mail to study admin further.
		// PLEASE do not change sucess message OR change it accordingly in emailServiceImpl.
		assertEquals("studyName(1): Successfully created datasets for subject subjectName in examination 2", task.getMessage());
		
		// THEN datasets are created
		// Check what we save at the end
		verify(datasetAcquisitionRepository).save(datasetAcq);
		verify(dicomPersisterService).persistAllForSerie(any());
		verify(bidsService).addDataset(any(Examination.class), Mockito.eq(importJob.getSubjectName()), Mockito.eq(importJob.getStudyName()));

		assertNotNull(datasetAcq);
		
		// AN archive is not referenced in the examination (file not existing)
		List<String> extradata = datasetAcq.getExamination().getExtraDataFilePathList();
		assertNull(extradata);
	}
}
