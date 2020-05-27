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

package org.shanoir.ng.examination;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.examination.controler.ExaminationApiController;
import org.shanoir.ng.examination.dto.ExaminationDTO;
import org.shanoir.ng.examination.dto.mapper.ExaminationMapper;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.exporter.service.BIDSService;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for examination controller.
 *
 * @author ifakhfakh
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ExaminationApiController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration()
@EnableSpringDataWebSupport
public class ExaminationApiControllerTest {

	@ClassRule
	public static TemporaryFolder tempFolder = new TemporaryFolder();
	
	public static String tempFolderPath;
	@BeforeClass
	public static void beforeClass() {
		tempFolderPath = tempFolder.getRoot().getAbsolutePath() + "/tmp/";

	    System.setProperty("datasets-data", tempFolderPath);
	}

	@Configuration
    static class Config {

		@Bean
		public ExaminationApiController testExamApiController() {
			ExaminationApiController api = new ExaminationApiController(null);
			return api;
		}
    }

	private static final String REQUEST_PATH = "/examinations";
	private static final String REQUEST_PATH_COUNT = REQUEST_PATH + "/count";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ExaminationMapper examinationMapperMock;

	@MockBean
	private ExaminationService examinationServiceMock;

	@MockBean
	private Pageable pageable;

	@MockBean
	private BIDSService bidsService;

	@MockBean
	private ShanoirEventService eventService;

	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(examinationServiceMock).deleteById(1L);
		given(examinationServiceMock.findPage(Mockito.any(Pageable.class))).willReturn(new PageImpl<Examination>(Arrays.asList(new Examination())));
		Examination exam = new Examination();
		exam.setId(Long.valueOf(123));
		given(examinationServiceMock.save(Mockito.any(ExaminationDTO.class))).willReturn(exam);
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void deleteExaminationTest() throws Exception {
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());

		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isNoContent());
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void testDeleteExaminationWithExtraData() throws IOException {
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());

		// GIVEN an examination to delete with extra data files
		File extraData = new File(tempFolderPath + "examination-1");
		extraData.mkdirs();

		// WHEN we delete the examination
		try {
			mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

			// THEN both examination and files are deleted
			assertFalse(extraData.exists());
		} catch (Exception e) {
			System.err.println("ERROR:" + e.getMessage());
			fail();
		}
	}

	@Test
	public void findExaminationByIdTest() throws Exception {
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());

		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
	}

	@Test
	public void findExaminationsTest() throws Exception {
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());

		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(new PageRequest(0, 10))))
		.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void saveNewExaminationTest() throws Exception {
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());

		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createExamination())))
		.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void updateExaminationTest() throws Exception {
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());

		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createExamination())))
		.andExpect(status().isNoContent());
	}

	@Test
	@WithMockUser
	public void testAddExtraData() throws IOException {
		// GIVEN a file to add to an examination
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());

		File importZip = tempFolder.newFile("test-import-extra-data.zip");

		try {
			importZip.createNewFile();
			MockMultipartFile file = new MockMultipartFile("file", "test-import-extra-data.txt", MediaType.MULTIPART_FORM_DATA_VALUE, new FileInputStream(importZip.getAbsolutePath()));

			// WHEN The file is added to the examination

			mvc.perform(MockMvcRequestBuilders.fileUpload(REQUEST_PATH + "/extra-data-upload/1").file(file))
			.andExpect(status().isOk());

			// THEN the file is saved
			assertTrue(new File(tempFolderPath + "/examination-1/test-import-extra-data.txt").exists());
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	@WithMockUser
	public void testDownloadExtraDataNotExisting() throws IOException {
		// GIVEN an examination with no extra-data
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());

		// WHEN we download extra-data
		try {
			// THEN we have a "no content" answer.
			mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH + "/extra-data-download/1/file.pdf/"))
			.andExpect(status().isNoContent());
		} catch (Exception e) {
			System.out.println(e);
			fail();
		}
	}

	@Test
	@WithMockUser
	public void testDownloadExtraData() throws IOException {
		// GIVEN an examination with extra-data files
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());

		File todow = new File(tempFolderPath + "examination-1/file1.pdf");
		//File todow = new File("/var/datasets-data/examination-1/file.pdf");
		todow.getParentFile().mkdirs();

		// WHEN we download extra-data
		try {
			todow.createNewFile();
			FileUtils.write(todow, "test");
			MvcResult result = mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH + "/extra-data-download/1/file1.pdf/"))
					.andExpect(status().isOk())
					.andReturn();

			// THEN the file is downloaded
			assertNotNull(result.getResponse().getContentAsString());
			System.out.println(result.getResponse().getContentAsString());
		} catch (Exception e) {
			System.out.println(e);
			fail();
		}
	}

	@Test
	@WithMockUser
	public void exportExaminationTestFileNotExisting() throws Exception {
		Examination exam = new Examination();
		exam.setExtraDataFilePathList(Collections.singletonList(tempFolderPath + "/preclinical/BusyFile"));
		given(examinationServiceMock.findById(1L)).willReturn(exam);

		mvc.perform(MockMvcRequestBuilders.get("/examinations/preclinical/examinationId/1/export").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());
	}

	@Test
	@WithMockUser
	public void exportExaminationNoContentTest() throws Exception {
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());

		mvc.perform(MockMvcRequestBuilders.get("/examinations/preclinical/examinationId/1/export").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

}
