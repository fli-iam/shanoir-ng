package org.shanoir.ng.datasetacquisition.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.repository.DatasetAcquisitionRepository;
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Channel.ChannelType;
import org.shanoir.ng.eeg.model.Event;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.EegImportJob;
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
		
		Channel chan = new Channel();
		chan.setHighCutoff(1);
		chan.setLowCutoff(132);
		chan.setName("Charles Aznavourian");
		chan.setNotch((float) 2);
		chan.setReferenceType(ChannelType.EEG);
		chan.setReferenceUnits("Diam's");
		chan.setResolution((float) 2);
		chan.setX(1);
		chan.setX(2);
		chan.setX(3);
		
		Event event = new Event();
		event.setSample(1);
		event.setType("type");
		event.setValue("Louane");
		
		importJob.setChannels(Collections.singletonList(chan));
		importJob.setEvents(Collections.singletonList(event));
		importJob.setSubjectId(Long.valueOf(1));
		importJob.setFrontStudyId(Long.valueOf(1));
		importJob.setExaminationId(Long.valueOf(1));
		importJob.setFrontAcquisitionEquipmentId(Long.valueOf(1));
		importJob.setName("Charles Trenet");
		importJob.setWorkFolder("Julien Clerc");
		
		service.createEegDataset(importJob );
		ArgumentCaptor<DatasetAcquisition> datasetAcquisitionCapturer = ArgumentCaptor.forClass(DatasetAcquisition.class);
		
		// Check what we save at the end
		verify(datasetAcquisitionRepository).save(datasetAcquisitionCapturer.capture());
		DatasetAcquisition hack = datasetAcquisitionCapturer.getValue();
		
		EegDataset ds = (EegDataset) hack.getDatasets().get(0);
		assertEquals(chan, ds.getChannelList().get(0));
		assertNotNull(chan.getDataset());
		
		assertEquals(event, ds.getEventList().get(0));
		assertNotNull(event.getDataset());
		
		assertEquals(1, ds.getChannelCount());
		assertEquals(ds.getName(), importJob.getName());
		assertEquals(ds.getDatasetExpressions().get(0).getDatasetExpressionFormat(), DatasetExpressionFormat.EEG);

		DatasetMetadata metadata = ds.getOriginMetadata();
		assertNotNull(metadata);
		assertEquals(metadata.getDatasetModalityType(), DatasetModalityType.EEG_DATASET);
	}
}
