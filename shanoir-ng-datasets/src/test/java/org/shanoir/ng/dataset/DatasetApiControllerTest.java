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

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.matchers.JUnitMatchers.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.dataset.controler.DatasetApiController;
import org.shanoir.ng.dataset.dto.mapper.DatasetMapper;
import org.shanoir.ng.dataset.modality.EegDatasetMapper;
import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.dataset.modality.MrDatasetMapper;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.dataset.model.DatasetExpressionFormat;
import org.shanoir.ng.dataset.model.DatasetMetadata;
import org.shanoir.ng.dataset.security.DatasetSecurityService;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.exporter.service.BIDSServiceImpl;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.model.Subject;
import org.shanoir.ng.shared.repository.SubjectRepository;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for dataset controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = DatasetApiController.class)
@AutoConfigureMockMvc(secure = false)
@ActiveProfiles("test")
public class DatasetApiControllerTest {

	private static final String REQUEST_PATH = "/datasets";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	@Autowired
	private MockMvc mvc;

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
	
	@MockBean
	private DatasetSecurityService datasetSecurityService;
	
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

	@MockBean
	private EegDatasetMapper eegDatasetMapper;

	@MockBean
	private BIDSServiceImpl bidsService;

	@MockBean
	private SubjectRepository subjectRepository;
	private Subject subject = new Subject(3L, "name");
	private DatasetAcquisition dsAcq = new MrDatasetAcquisition();
	private DatasetMetadata updatedMetadata = new DatasetMetadata();

	@Before
	public void setup() throws ShanoirException {
		doNothing().when(datasetServiceMock).deleteById(1L);
		given(datasetServiceMock.findById(1L)).willReturn(new MrDataset());
		given(datasetServiceMock.create(Mockito.mock(MrDataset.class))).willReturn(new MrDataset());
		dsAcq.setRank(2);
		dsAcq.setSortingIndex(2);
		updatedMetadata.setComment("comment");
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteDatasetTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isNoContent());
	}

	@Test
	public void findDatasetByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
	}

	@Test
	public void getDatasetUrlsByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH + "/urls/1").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updateDatasetTest() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		Dataset ds = ModelsUtil.createMrDataset();
		ds.setId(1L);
		String json = mapper.writeValueAsString(ds);
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isNoContent());
	}

	@Test
	public void testMassiveDownloadByStudyIdNull() throws Exception {
		// GIVEN a study with some datasets to export in nii format

		// WHEN we export all the datasets but with null studyId
		try {
			mvc.perform(MockMvcRequestBuilders.get("/datasets/massiveDownloadByStudy")
					.param("format", "nii")
					.param("studyId", ""))
			.andExpect(status().isForbidden());
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Request processing failed; nested exception is {\"code\":403,\"message\":\"Please use a valid study ID.\",\"details\":null}");
		}

		// THEN we have a forbidden http status
	}

	@Test
	public void testMassiveDownloadByStudyIdNifti() throws Exception {
		// GIVEN a study with some datasets to export in nii format
		// Create a file with some text
		File datasetFile = testFolder.newFile("test.nii");
		datasetFile.getParentFile().mkdirs();
		datasetFile.createNewFile();
		FileUtils.write(datasetFile, "test");

		// Link it to datasetExpression in a dataset in a study
		Dataset dataset = new MrDataset();
		dataset.setSubjectId(3L);
		given(subjectRepository.findOne(3L)).willReturn(subject);
		dataset.setDatasetAcquisition(dsAcq);
		dataset.setUpdatedMetadata(updatedMetadata);

		DatasetExpression expr = new DatasetExpression();
		expr.setDatasetExpressionFormat(DatasetExpressionFormat.NIFTI_SINGLE_FILE);
		DatasetFile dsFile = new DatasetFile();
		dsFile.setPath("file:///" + datasetFile.getAbsolutePath());
		expr.setDatasetFiles(Collections.singletonList(dsFile));
		List<DatasetExpression> datasetExpressions = Collections.singletonList(expr);
		dataset.setDatasetExpressions(datasetExpressions);

		Mockito.when(datasetSecurityService.hasRightOnAtLeastOneDataset(Mockito.anyList(), Mockito.eq("CAN_DOWNLOAD"))).thenReturn(Collections.singletonList(dataset));
		Mockito.when(datasetServiceMock.findByStudyId(1L)).thenReturn(Collections.singletonList(dataset));

		// WHEN we export all the datasets
		mvc.perform(MockMvcRequestBuilders.get("/datasets/massiveDownloadByStudy")
				.param("format", "nii")
				.param("studyId", "1"))
		.andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.MULTIPART_FORM_DATA))
		.andExpect(content().string(containsString("name_comment_2_2.nii")));
		// THEN all datasets are exported
	}

	@Test
	public void testMassiveDownloadByDatasetsId() throws Exception {
		// GIVEN a list of datasets to export
		// Create a file with some text
		File datasetFile = testFolder.newFile("test.nii");
		datasetFile.getParentFile().mkdirs();
		datasetFile.createNewFile();
		FileUtils.write(datasetFile, "test");

		// Link it to datasetExpression in a dataset in a study
		Dataset dataset = new MrDataset();
		dataset.setSubjectId(3L);
		given(subjectRepository.findOne(3L)).willReturn(subject);
		dataset.setDatasetAcquisition(dsAcq);
		dataset.setUpdatedMetadata(updatedMetadata);

		DatasetExpression expr = new DatasetExpression();
		expr.setDatasetExpressionFormat(DatasetExpressionFormat.NIFTI_SINGLE_FILE);
		DatasetFile dsFile = new DatasetFile();
		dsFile.setPath("file:///" + datasetFile.getAbsolutePath());
		expr.setDatasetFiles(Collections.singletonList(dsFile));
		List<DatasetExpression> datasetExpressions = Collections.singletonList(expr);
		dataset.setDatasetExpressions(datasetExpressions);

		Mockito.when(datasetSecurityService.hasRightOnAtLeastOneDataset(Mockito.anyList(), Mockito.eq("CAN_DOWNLOAD"))).thenReturn(Collections.singletonList(dataset));
		Mockito.when(datasetServiceMock.findByIdIn(Mockito.anyList())).thenReturn(Collections.singletonList(dataset));

		// WHEN we export all the datasets
		mvc.perform(MockMvcRequestBuilders.get("/datasets/massiveDownload")
				.param("format", "nii")
				.param("datasetIds", "1"))
		.andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.MULTIPART_FORM_DATA))
		.andExpect(content().string(containsString("name_comment_2_2.nii")));


		// THEN all datasets are exported
	}

	@Test
	public void testMassiveDownloadByDatasetsIdNoIds() {
		// GIVEN a list of datasets to export

		// WHEN we export all the datasets with no datasets ID
		try {
			mvc.perform(MockMvcRequestBuilders.get("/datasets/massiveDownload")
					.param("format", "nii")
					.param("datasetIds", ""))
			.andExpect(status().isForbidden());
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Request processing failed; nested exception is {\"code\":403,\"message\":\"Please use a valid sets of dataset IDs.\",\"details\":null}");
		}


		// THEN we expect an error
	}


	@Test
	public void testMassiveDownloadByDatasetsIdToMuchIds() {
		// GIVEN a list of datasets to export
		StringBuilder strb = new StringBuilder();
		for (int i = 0; i < 55 ; i++) {
			strb.append(i).append(",");
		}
		String ids = strb.substring(0, strb.length() -1);

		// WHEN we export all the datasets with no datasets ID
		try {
			mvc.perform(MockMvcRequestBuilders.get("/datasets/massiveDownload")
					.param("format", "nii")
					.param("datasetIds", ids))
			.andExpect(status().isForbidden());
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Request processing failed; nested exception is {\"code\":403,\"message\":\"You can download less than 50 datasets.\",\"details\":null}");
		}


		// THEN we expect an error
	}

	@Test
	public void testMassiveDownloadByStudyIdTooMuchDatasets() throws Exception {
		// GIVEN a study with more then 50 datasets to export

		List<Dataset> hugeList = new ArrayList<Dataset>();
		for (int i = 0; i < 51 ; i++) {
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
	public void testMassiveDownloadByStudyWrongFormat() throws Exception {
		// Create a file with some text
		File datasetFile = testFolder.newFile("test.nii");
		datasetFile.getParentFile().mkdirs();
		datasetFile.createNewFile();
		FileUtils.write(datasetFile, "test");

		// Link it to datasetExpression in a dataset in a study
		Dataset dataset = new MrDataset();
		dataset.setSubjectId(3L);
		given(subjectRepository.findOne(3L)).willReturn(subject);
		dataset.setDatasetAcquisition(dsAcq);
		dataset.setUpdatedMetadata(updatedMetadata);

		DatasetExpression expr = new DatasetExpression();
		expr.setDatasetExpressionFormat(DatasetExpressionFormat.NIFTI_SINGLE_FILE);
		DatasetFile dsFile = new DatasetFile();
		dsFile.setPath(datasetFile.getAbsolutePath());
		expr.setDatasetFiles(Collections.singletonList(dsFile));
		List<DatasetExpression> datasetExpressions = Collections.singletonList(expr);
		dataset.setDatasetExpressions(datasetExpressions);
		Mockito.when(datasetSecurityService.hasRightOnAtLeastOneDataset(Mockito.anyList(), Mockito.eq("CAN_DOWNLOAD"))).thenReturn(Collections.singletonList(dataset));

		// GIVEN a study with some datasets to export in nii format
		Mockito.when(datasetServiceMock.findByStudyId(1L)).thenReturn(Collections.singletonList(dataset));
	try {
		// WHEN we export all the datasets
		mvc.perform(MockMvcRequestBuilders.get("/datasets/massiveDownloadByStudy")
				.param("format", "otherWRONG")
				.param("studyId", "1"))
		.andExpect(status().isForbidden());
	} catch (Exception e) {
		assertEquals("Request processing failed; nested exception is {\"code\":422,\"message\":\"Bad arguments.\",\"details\":null}", e.getMessage());
	}

		// THEN we expect a failure
	}
}
