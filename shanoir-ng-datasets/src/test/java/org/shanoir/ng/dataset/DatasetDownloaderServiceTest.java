package org.shanoir.ng.dataset;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.service.DicomSEGAndSRImporterService;
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
import org.shanoir.ng.shared.security.ControllerSecurityService;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

@SpringBootTest
@ActiveProfiles("test")
public class DatasetDownloaderServiceTest {

    @Qualifier("datasetDownloaderServiceImpl")
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
	
	@MockBean(name = "controllerSecurityService")
	private ControllerSecurityService controllerSecurityService;
	
    @TempDir
    public File tempDir;

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
	private DicomSEGAndSRImporterService dicomSRImporterService;

	private Subject subject = new Subject(3L, "name");
	private Study study = new Study(1L, "studyName");

	private DatasetAcquisition dsAcq = new MrDatasetAcquisition();
	private DatasetMetadata updatedMetadata = new DatasetMetadata();
	private Examination exam = new Examination();

	@BeforeEach
	public void setup() throws ShanoirException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SolrServerException, IOException, RestServiceException {
		doNothing().when(datasetServiceMock).deleteById(1L);
		given(datasetServiceMock.findById(1L)).willReturn(new MrDataset());
		given(datasetServiceMock.create(Mockito.mock(MrDataset.class))).willReturn(new MrDataset());
		given(studyRepo.findById(Mockito.anyLong())).willReturn(Optional.of(study));
		given(controllerSecurityService.idMatches(Mockito.anyLong(), Mockito.any(Dataset.class))).willReturn(true);
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
		File datasetFile = new File(tempDir, "test.nii");
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
			this.datasetDownloaderService.massiveDownload("otherWRONG", Collections.singletonList(dataset), response, false, null);
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
		File datasetFile = new File(tempDir, "test.nii");
		datasetFile.createNewFile();
		FileUtils.write(datasetFile, "test");

		// Link it to datasetExpression in a dataset in a study
		Dataset dataset = new MrDataset();
		dataset.setId(1L);
		dataset.setDownloadable(true);
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
		this.datasetDownloaderService.massiveDownload("nii", Collections.singletonList(dataset), response, false, null);

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
		File datasetFile = new File(tempDir, "test.nii");
		datasetFile.createNewFile();
		FileUtils.write(datasetFile, "test");

		// Link it to datasetExpression in a dataset in a study
		Dataset dataset = new MrDataset();
		dataset.setDownloadable(true);
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
		this.datasetDownloaderService.massiveDownload("nii", Collections.singletonList(dataset), response, false, null);

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
