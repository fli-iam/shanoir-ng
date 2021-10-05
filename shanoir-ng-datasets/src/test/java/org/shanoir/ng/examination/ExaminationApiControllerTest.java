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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.shanoir.ng.examination.controler.ExaminationApiController;
import org.shanoir.ng.examination.dto.mapper.ExaminationMapper;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.repository.ExaminationRepository;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.exporter.service.BIDSService;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.event.ShanoirEventType;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

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
@AutoConfigureMockMvc(addFilters = false)
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

	@MockBean
	private StudyRepository studyRepository;

	@MockBean
	ExaminationRepository examRepo;

	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(examinationServiceMock).deleteById(1L);
		given(examinationServiceMock.findPage(Mockito.any(Pageable.class), Mockito.eq(false))).willReturn(new PageImpl<Examination>(Arrays.asList(new Examination())));
		Examination exam = new Examination();
		exam.setId(Long.valueOf(123));
		given(examinationServiceMock.save(Mockito.any(Examination.class))).willReturn(exam);
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void deleteExaminationTest() throws Exception {
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());
		given(examinationServiceMock.getExtraDataFilePath(1L, "")).willReturn("nonExisting");

		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isNoContent());
		
		// Test event here
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void testDeleteExaminationWithExtraData() throws IOException {
		Examination exam = new Examination();
		exam.setStudyId(3L);
		exam.setId(1L);
		given(examinationServiceMock.findById(1L)).willReturn(exam);

		// GIVEN an examination to delete with extra data files
		File extraData = new File(tempFolderPath + "examination-1");
		extraData.mkdirs();

		given(examinationServiceMock.getExtraDataFilePath(1L, "")).willReturn(extraData.getPath());

		// WHEN we delete the examination
		try {
			mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());

			// Test events
			ArgumentCaptor<ShanoirEvent> eventCatcher = ArgumentCaptor.forClass(ShanoirEvent.class);
			Mockito.verify(eventService).publishEvent(eventCatcher.capture());
			
			ShanoirEvent event = eventCatcher.getValue();
			assertNotNull(event);
			assertEquals(exam.getStudyId().toString(), event.getMessage());
			assertEquals(exam.getId().toString(), event.getObjectId());
			assertEquals(ShanoirEventType.DELETE_EXAMINATION_EVENT, event.getEventType());

			// THEN both examination and files are deleted
			assertFalse(extraData.exists());
		} catch (Exception e) {
			System.err.println("ERROR:" + e.getMessage());
			fail();
		}
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void findExaminationByIdTest() throws Exception {
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());

		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void findExaminationsTest() throws Exception {
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());

		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(PageRequest.of(0, 10))))
		.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void saveNewExaminationTest() throws Exception {
		Examination exam = new Examination();
		exam.setId(Long.valueOf(123));
		exam.setStudyId(3L);
		given(examinationServiceMock.findById(1L)).willReturn(exam);
		given(examinationServiceMock.save(Mockito.any())).willReturn(exam);

		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createExamination())))
		.andExpect(status().isOk());
		
		// Check event here to verify that the message is well set to event
		ArgumentCaptor<ShanoirEvent> eventCatcher = ArgumentCaptor.forClass(ShanoirEvent.class);
		Mockito.verify(eventService).publishEvent(eventCatcher.capture());
		
		ShanoirEvent event = eventCatcher.getValue();
		assertNotNull(event);
		assertEquals(exam.getStudyId().toString(), event.getMessage());
		assertEquals(exam.getId().toString(), event.getObjectId());
		assertEquals(ShanoirEventType.CREATE_EXAMINATION_EVENT, event.getEventType());
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
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void testAddExtraData() throws IOException {
		// GIVEN a file to add to an examination
		File importZip = tempFolder.newFile("test-import-extra-data.zip");

		try {
			importZip.createNewFile();
			MockMultipartFile file = new MockMultipartFile("file", "test-import-extra-data.txt", MediaType.MULTIPART_FORM_DATA_VALUE, new FileInputStream(importZip.getAbsolutePath()));

			// WHEN The file is added to the examination
			mvc.perform(MockMvcRequestBuilders.fileUpload(REQUEST_PATH + "/extra-data-upload/1").file(file))
			.andExpect(status().isNotAcceptable());

			Mockito.verify(examinationServiceMock).addExtraData(Mockito.any(Long.class), Mockito.any(MultipartFile.class));

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void testDownloadExtraDataNotExisting() throws IOException {
		// GIVEN an examination with no extra-data
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());
		given(examinationServiceMock.getExtraDataFilePath(1L, "file.pdf")).willReturn("notExisting");
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
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void testDownloadExtraData() throws IOException {
		// GIVEN an examination with extra-data files
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());

		File todow = new File(tempFolderPath + "examination-1/file1.pdf");
		//File todow = new File("/var/datasets-data/examination-1/file.pdf");
		todow.getParentFile().mkdirs();

		given(examinationServiceMock.getExtraDataFilePath(1L, "file1.pdf")).willReturn(todow.getPath());

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

}
