package org.shanoir.ng.datasetacquisition.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.shanoir.ng.dataset.modality.EegDataset;
import org.shanoir.ng.dataset.modality.EegDatasetDTO;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.model.DatasetModalityType;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.eeg.model.Channel;
import org.shanoir.ng.eeg.model.Event;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.dto.EegImportJob;
import org.shanoir.ng.importer.service.*;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.study.rights.StudyUserRightsRepository;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class EegImporterServiceTest {

    @InjectMocks
    @Spy
    EegImporterService service = new EegImporterService();

    @Mock
    private ImporterMailService mailService;

    @Mock
    private ExaminationService examinationService;

    @Mock
    private DatasetAcquisitionService datasetAcquisitionService;

    @Mock
    private ShanoirEventService taskService;

    private Examination exam;

    @Before
    public void setUp() throws IOException {
        exam = new Examination();
        exam.setExaminationDate(LocalDate.now());
        exam.setId(1l);
        given(examinationService.findById(Mockito.anyLong())).willReturn(exam);
    }

    @Test
    @WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
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
        chan.setReferenceType(Channel.ChannelType.EEG);
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
        importJob.setStudyId(Long.valueOf(1));
        importJob.setExaminationId(Long.valueOf(1));
        importJob.setAcquisitionEquipmentId(Long.valueOf(1));
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
        verify(datasetAcquisitionService).create(datasetAcquisitionCapturer.capture());
        DatasetAcquisition hack = datasetAcquisitionCapturer.getValue();

        EegDataset ds = (EegDataset) hack.getDatasets().get(0);
        assertEquals(chan, ds.getChannels().get(0));
        assertNotNull(chan.getDataset());

        assertEquals(event, ds.getEvents().get(0));
        assertNotNull(event.getDataset());

        assertEquals(1, ds.getChannelCount());
        assertEquals(ds.getName(), dataset.getName());
        assertEquals(DatasetExpressionFormat.EEG, ds.getDatasetExpressions().get(0).getDatasetExpressionFormat());

        DatasetMetadata metadata = ds.getOriginMetadata();
        assertNotNull(metadata);
        assertEquals(DatasetModalityType.EEG_DATASET, metadata.getDatasetModalityType());
    }

}
