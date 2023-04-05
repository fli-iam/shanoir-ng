package org.shanoir.ng.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.http.entity.ContentType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.shanoir.ng.bids.service.BIDSServiceImpl;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.modality.EegDatasetMapper;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.dataset.service.DatasetServiceImpl;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.service.DicomSRImporterService;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.shared.security.ControlerSecurityService;
import org.shanoir.ng.utils.DatasetFileUtils;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class DatasetDownloaderServiceTest {
	
	@Autowired
	DatasetDownloaderServiceImpl datasetDownloaderService;
	
	@MockBean
	private DatasetService datasetServiceMock;

	@MockBean
	private DatasetMapper datasetMapperMock;

	@MockBean
	private MrDatasetMapper mrDatasetMapperMock;

	@MockBean
	private ExaminationService examinationService;

	@MockBean
	private WADODownloaderService downloader;
	
	@MockBean(name = "datasetSecurityService")
	private DatasetSecurityService datasetSecurityService;
	
	@MockBean(name = "controlerSecurityService")
	private ControlerSecurityService controlerSecurityService;
	
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

	@MockBean
	private EegDatasetMapper eegDatasetMapper;

	@MockBean
	private BIDSServiceImpl bidsService;

	@MockBean
	private SubjectRepository subjectRepository;

	@MockBean
	private ShanoirEventService eventService;
	
	@MockBean
	private StudyRepository studyRepo;
	
	@MockBean
	private ImporterService importerService;
	
	@MockBean
	private DicomSRImporterService dicomSRImporterService;

	private Subject subject = new Subject(3L, "name");
	private Study study = new Study(1L, "studyName");

	private DatasetAcquisition dsAcq = new MrDatasetAcquisition();
	private DatasetMetadata updatedMetadata = new DatasetMetadata();
	private Examination exam = new Examination();

	@Before
	public void setup() throws ShanoirException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		doNothing().when(datasetServiceMock).deleteById(1L);
		given(datasetServiceMock.findById(1L)).willReturn(new MrDataset());
		given(datasetServiceMock.create(Mockito.mock(MrDataset.class))).willReturn(new MrDataset());
		given(studyRepo.findById(Mockito.anyLong())).willReturn(Optional.of(study));
		given(controlerSecurityService.idMatches(Mockito.anyLong(), Mockito.any(Dataset.class))).willReturn(true);
		dsAcq.setRank(2);
		dsAcq.setSortingIndex(2);
		exam.setId(1L);
		exam.setStudy(new Study());
		exam.getStudy().setId(1L);
		dsAcq.setExamination(exam);
		updatedMetadata.setComment("comment");
		updatedMetadata.setName("test 1");
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testMassiveDownloadByStudyWrongFormat() throws Exception {
		// Create a file with some text
		File datasetFile = testFolder.newFile("test.nii");
		datasetFile.getParentFile().mkdirs();
		datasetFile.createNewFile();
		FileUtils.write(datasetFile, "test");

		// Link it to datasetExpression in a dataset in a study
		Dataset dataset = new MrDataset();
		dataset.setSubjectId(3L);
		given(subjectRepository.findById(3L)).willReturn(Optional.of(subject));
		dataset.setDatasetAcquisition(dsAcq);
		dataset.setUpdatedMetadata(updatedMetadata);

		DatasetExpression expr = new DatasetExpression();
		expr.setDatasetExpressionFormat(DatasetExpressionFormat.NIFTI_SINGLE_FILE);
		DatasetFile dsFile = new DatasetFile();
		dsFile.setPath(datasetFile.getAbsolutePath());
		expr.setDatasetFiles(Collections.singletonList(dsFile));
		List<DatasetExpression> datasetExpressions = Collections.singletonList(expr);
		dataset.setDatasetExpressions(datasetExpressions);

		// GIVEN a study with some datasets to export in nii format
		Mockito.when(datasetServiceMock.findByStudyId(1L)).thenReturn(Collections.singletonList(dataset));
		HttpServletResponse response =  new MockHttpServletResponse();

		// WHEN we export all the datasets
		try {
			this.datasetDownloaderService.massiveDownload("otherWRONG", Collections.singletonList(dataset), response, false);
		} catch (RestServiceException e) {
			assertEquals("Unexpected error while downloading dataset files",
					e.getErrorModel().getMessage());
		}
		// THEN we expect a failure
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testMassiveDownloadByExaminationIdNifti() throws Exception {
		// GIVEN a study with some datasets to export in nii format
		// Create a file with some text
		File datasetFile = testFolder.newFile("test.nii");
		datasetFile.getParentFile().mkdirs();
		datasetFile.createNewFile();
		FileUtils.write(datasetFile, "test");

		// Link it to datasetExpression in a dataset in a study
		Dataset dataset = new MrDataset();
		dataset.setId(1L);
		dataset.setSubjectId(3L);
		given(subjectRepository.findById(3L)).willReturn(Optional.of(subject));
		dataset.setDatasetAcquisition(dsAcq);
		dataset.setUpdatedMetadata(updatedMetadata);

		DatasetExpression expr = new DatasetExpression();
		expr.setDatasetExpressionFormat(DatasetExpressionFormat.NIFTI_SINGLE_FILE);
		DatasetFile dsFile = new DatasetFile();
		dsFile.setPath("file:///" + datasetFile.getAbsolutePath());
		expr.setDatasetFiles(Collections.singletonList(dsFile));
		List<DatasetExpression> datasetExpressions = Collections.singletonList(expr);
		dataset.setDatasetExpressions(datasetExpressions);

		HttpServletResponse response =  new MockHttpServletResponse();

		// WHEN we export all the datasets
		this.datasetDownloaderService.massiveDownload("nii", Collections.singletonList(dataset), response, false);

		assertEquals(response.getContentType(), "application/zip");
		// THEN all datasets are exported

		ArgumentCaptor<ShanoirEvent> eventCatcher = ArgumentCaptor.forClass(ShanoirEvent.class);
		Mockito.verify(eventService, times(1)).publishEvent(eventCatcher.capture());

		ShanoirEvent event = eventCatcher.getValue();
		assertNotNull(event);
		assertEquals(dataset.getId().toString()  + "." + "nii", event.getMessage());
		assertEquals(dataset.getId().toString(), event.getObjectId());
		assertEquals(ShanoirEventType.DOWNLOAD_DATASET_EVENT, event.getEventType());
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testMassiveDownloadByDatasetsId() throws Exception {
		// GIVEN a list of datasets to export
		// Create a file with some text
		File datasetFile = testFolder.newFile("test.nii");
		datasetFile.getParentFile().mkdirs();
		datasetFile.createNewFile();
		FileUtils.write(datasetFile, "test");

		// Link it to datasetExpression in a dataset in a study
		Dataset dataset = new MrDataset();
		dataset.setId(1L);
		dataset.setSubjectId(3L);
		given(subjectRepository.findById(3L)).willReturn(Optional.of(subject));
		dataset.setDatasetAcquisition(dsAcq);
		dataset.setUpdatedMetadata(updatedMetadata);

		DatasetExpression expr = new DatasetExpression();
		expr.setDatasetExpressionFormat(DatasetExpressionFormat.NIFTI_SINGLE_FILE);
		DatasetFile dsFile = new DatasetFile();
		dsFile.setPath("file:///" + datasetFile.getAbsolutePath());
		expr.setDatasetFiles(Collections.singletonList(dsFile));
		List<DatasetExpression> datasetExpressions = Collections.singletonList(expr);
		dataset.setDatasetExpressions(datasetExpressions);
	
		HttpServletResponse response =  new MockHttpServletResponse();
		this.datasetDownloaderService.massiveDownload("nii", Collections.singletonList(dataset), response, false);

		// THEN all datasets are exported
		
		ArgumentCaptor<ShanoirEvent> eventCatcher = ArgumentCaptor.forClass(ShanoirEvent.class);
		Mockito.verify(eventService, times(1)).publishEvent(eventCatcher.capture());
		
		ShanoirEvent event = eventCatcher.getValue();
		assertNotNull(event);
		assertEquals(dataset.getId().toString() + ".nii", event.getMessage());
		assertEquals(dataset.getId().toString(), event.getObjectId());
		assertEquals(ShanoirEventType.DOWNLOAD_DATASET_EVENT, event.getEventType());
	}
}
