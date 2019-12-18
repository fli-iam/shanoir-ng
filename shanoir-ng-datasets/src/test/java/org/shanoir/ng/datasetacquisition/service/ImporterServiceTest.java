package org.shanoir.ng.datasetacquisition.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import org.shanoir.ng.examination.service.ExaminationService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ImporterServiceTest {

	@Autowired
	ImporterService service;
	
	@MockBean
	private ExaminationService examinationService;

	@MockBean
	private DatasetAcquisitionContext datasetAcquisitionContext;
	
	@MockBean
	private DatasetAcquisitionRepository datasetAcquisitionRepository;
	
	@MockBean
	private DicomPersisterService dicomPersisterService;

	@Test
	public void testCreateEegDataset() {
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
		dataset.setName("Charles Trenet");
		importJob.setWorkFolder("Julien Clerc");
		
		service.createEegDataset(importJob);
		ArgumentCaptor<DatasetAcquisition> datasetAcquisitionCapturer = ArgumentCaptor.forClass(DatasetAcquisition.class);
		
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
		assertEquals(ds.getDatasetExpressions().get(0).getDatasetExpressionFormat(), DatasetExpressionFormat.EEG);

		DatasetMetadata metadata = ds.getOriginMetadata();
		assertNotNull(metadata);
		assertEquals(metadata.getDatasetModalityType(), DatasetModalityType.EEG_DATASET);
	}

	@Test
	public void createAllDatasetAcquisition() {
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
		service.setImportJob(importJob);
		
		Examination examination = new Examination();
		when(examinationService.findById(importJob.getExaminationId())).thenReturn(examination);
		DatasetAcquisition datasetAcq = new MrDatasetAcquisition();
		when(datasetAcquisitionContext.generateDatasetAcquisitionForSerie(serie, 0, importJob)).thenReturn(datasetAcq );
		
		// WHEN we treat this importjob
		service.createAllDatasetAcquisition();
		
		// THEN datasets are created
		// Check what we save at the end
		verify(datasetAcquisitionRepository).save(datasetAcq);
		verify(dicomPersisterService).persistAllForSerie(any());

		assertNotNull(datasetAcq);
		
		// AN archive is not referenced in the examination (file not existing)
		List<String> extradata = datasetAcq.getExamination().getExtraDataFilePathList();
		assertNull(extradata);
	}
}
