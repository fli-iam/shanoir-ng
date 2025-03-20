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

package org.shanoir.ng.dataset;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.shanoir.ng.bids.service.BIDSServiceImpl;
import org.shanoir.ng.dataset.controler.DatasetApiController;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.modality.EegDatasetMapper;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.CreateStatisticsService;
import org.shanoir.ng.dataset.service.DatasetDownloaderServiceImpl;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.importer.service.DicomSEGAndSRImporterService;
import org.shanoir.ng.importer.service.ImporterService;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.shared.security.ControlerSecurityService;
import org.shanoir.ng.solr.service.SolrService;
import org.shanoir.ng.tag.mapper.StudyTagMapper;
import org.shanoir.ng.tag.service.StudyTagService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for dataset controller.
 *
 * @author msimon
 *
 */
@WebMvcTest(controllers = DatasetApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class StudyTagApiControllerTest {

	private static final String REQUEST_PATH = "/datasets";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private DatasetService datasetServiceMock;
	
	@MockBean
	private CreateStatisticsService createStatisticsService;

	@MockBean
	private DatasetRepository datasetRepositoryMock;

	@MockBean
	private StudyTagService studyTagServiceMock;

	@MockBean
	private DatasetMapper datasetMapperMock;

	@MockBean
	private StudyTagMapper studyTagMapperMock;

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
	
    @TempDir
    private File tempDir;

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
	private RabbitTemplate rabbitTemplate;
	
	@MockBean
	private ImporterService importerService;
	
	@MockBean
	private DicomSEGAndSRImporterService dicomSRImporterService;
	
	@MockBean
	@Qualifier("datasetDownloaderServiceImpl")
	private DatasetDownloaderServiceImpl datasetDownloaderService;

	@MockBean
	private SolrService solrService;

	@Autowired
	private ObjectMapper mapper;

	private Subject subject = new Subject(3L, "name");
	private Study study = new Study(1L, "studyName");

	private DatasetAcquisition dsAcq = new MrDatasetAcquisition();
	private DatasetMetadata updatedMetadata = new DatasetMetadata();
	private Examination exam = new Examination();

	@BeforeEach
	public void setup() throws ShanoirException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, SolrServerException, RestServiceException {
		MrDataset datasetToReturn = new MrDataset();
		datasetToReturn.setDatasetAcquisition(dsAcq);
		datasetToReturn.getDatasetAcquisition().setExamination(exam);
		datasetToReturn.getDatasetAcquisition().getExamination().setStudy(study);

		given(datasetServiceMock.findById(1L)).willReturn(datasetToReturn);
		doNothing().when(datasetServiceMock).deleteById(1L);
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
	public void deleteDatasetTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isNoContent());
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void findDatasetByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void updateDatasetTest() throws Exception {
		Dataset ds = ModelsUtil.createMrDataset();
		ds.setId(1L);
		String json = mapper.writeValueAsString(ds);
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isNoContent());
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testMassiveDownloadByDatasetsIdNoIds() {
		// GIVEN a list of datasets to export

		// WHEN we export all the datasets with no datasets ID
		try {
			mvc.perform(MockMvcRequestBuilders.post("/datasets/massiveDownload")
					.param("format", "nii")
					.param("datasetIds", ""))
			.andExpect(status().isForbidden());
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Request processing failed; nested exception is {\"code\":403,\"message\":\"Please use a valid sets of dataset IDs.\",\"details\":null}");
		}


		// THEN we expect an error
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testMassiveDownloadByDatasetsIdToMuchIds() {
		// GIVEN a list of datasets to export
		StringBuilder strb = new StringBuilder();
		for (int i = 0; i < 505 ; i++) {
			strb.append(i).append(",");
		}
		String ids = strb.substring(0, strb.length() -1);

		// WHEN we export all the datasets with no datasets ID
		try {
			mvc.perform(MockMvcRequestBuilders.post("/datasets/massiveDownload")
					.param("format", "nii")
					.param("datasetIds", ids))
			.andExpect(status().isForbidden());
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Request processing failed; nested exception is {\"code\":403,\"message\":\"You can't download more than 50 datasets.\",\"details\":null}");
		}
		// THEN we expect an error
	}

	@Test
	@WithMockKeycloakUser(id = 3, username = "jlouis", authorities = { "ROLE_ADMIN" })
	public void testMassiveDownloadByStudyIdTooMuchDatasets() throws Exception {
		// GIVEN a study with more then 50 datasets to export

		List<Dataset> hugeList = new ArrayList<Dataset>();
		for (int i = 0; i < 501 ; i++) {
			hugeList.add(new MrDataset());
		}
		Mockito.when(datasetServiceMock.findByStudyId(1L)).thenReturn(hugeList);

		try {
		// WHEN we export all the datasets
		mvc.perform(MockMvcRequestBuilders.get("/datasets/massiveDownloadByStudy")
				.param("format", "nii")
				.param("studyId", "1"))
		.andExpect(status().isForbidden());
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Request processing failed; nested exception is {\"code\":403,\"message\":\"This study has more than 50 datasets, that is the limit. Please download them from solr search.\",\"details\":null}");
		}
	}

	@Test
	public void testCorrectDatasetDownloadName() {
		// We want to test this code:
		// datasetName = datasetName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
		assertEquals("abc_ABC___123.-tru____", "abc ABCé('123.-tru_ç&ù".replaceAll("[^a-zA-Z0-9\\.\\-]", "_"));
	}

}
