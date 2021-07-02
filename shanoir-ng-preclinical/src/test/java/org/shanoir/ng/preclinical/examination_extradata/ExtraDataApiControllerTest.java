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

package org.shanoir.ng.preclinical.examination_extradata;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.configuration.ShanoirPreclinicalConfiguration;
import org.shanoir.ng.preclinical.extra_data.ExtraDataApiController;
import org.shanoir.ng.preclinical.extra_data.ExtraDataService;
import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraData;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.ExtraDataModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for examination extradata controller.
 *
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ExtraDataApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
@ActiveProfiles("test")
public class ExtraDataApiControllerTest {

	private static final String REQUEST_PATH_EXAMINATION = "/examination";
	private static final String EXAMINATION_ID = "/1";
	private static final String REQUEST_EXTRADATA = "/extradata";
	private static final String REQUEST_PHYSIOLOGICALDATA = "/physiologicaldata";
	private static final String REQUEST_BLOODGASDATA = "/bloodgasdata";
	private static final String REQUEST_UPLOAD = "/upload";
	private static final String REQUEST_PATH_EXTRADATA = REQUEST_PATH_EXAMINATION + EXAMINATION_ID + REQUEST_EXTRADATA;
	private static final String REQUEST_PATH_PHYSIOLOGICALDATA = REQUEST_PATH_EXAMINATION + EXAMINATION_ID
			+ REQUEST_PHYSIOLOGICALDATA;
	private static final String REQUEST_PATH_BLOODGASDATA = REQUEST_PATH_EXAMINATION + EXAMINATION_ID
			+ REQUEST_BLOODGASDATA;
	private static final String REQUEST_PATH_ALL = REQUEST_PATH_EXTRADATA + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH_EXTRADATA + "/1";
	private static final String REQUEST_PATH_PHYSIO_WITH_ID = REQUEST_PATH_PHYSIOLOGICALDATA + "/1";
	private static final String REQUEST_PATH_BLOODGAS_WITH_ID = REQUEST_PATH_BLOODGASDATA + "/1";
	private static final String REQUEST_PATH_UPLOAD = REQUEST_PATH_EXAMINATION + REQUEST_EXTRADATA + REQUEST_UPLOAD
			+ "/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ExtraDataService<ExaminationExtraData> extraDataServiceMock;
	@MockBean
	private ShanoirPreclinicalConfiguration preclinicalConfig;

	@ClassRule
	public static TemporaryFolder tempFolder = new TemporaryFolder();
	
	public static String tempFolderPath;
	@BeforeClass
	public static void beforeClass() {
		tempFolderPath = tempFolder.getRoot().getAbsolutePath() + "/tmp/";
	    System.setProperty("preclinical.uploadExtradataFolder", tempFolderPath);
	}

	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(extraDataServiceMock).deleteById(1L);
		given(extraDataServiceMock.findAllByExaminationId(1L)).willReturn(Arrays.asList(new ExaminationExtraData()));
		given(extraDataServiceMock.findById(1L)).willReturn(new ExaminationExtraData());
		given(extraDataServiceMock.save(Mockito.mock(ExaminationExtraData.class)))
				.willReturn(new ExaminationExtraData());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteExtraDataTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findExtraDataByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findExtraDatasTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_ALL).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void saveNewExtraDataTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH_EXTRADATA).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(ExtraDataModelUtil.createExaminationExtraData()))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void uploadExtraDataTest() throws Exception {
		MockMultipartFile firstFile = new MockMultipartFile("files", "filename.txt", "text/plain",
				"some xml".getBytes());
		mvc.perform(MockMvcRequestBuilders.fileUpload(REQUEST_PATH_UPLOAD).file(firstFile)).andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updatePhysiologicalDataTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_PHYSIO_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(ExtraDataModelUtil.createExaminationPhysiologicalData())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updateBloodGasDataTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_BLOODGAS_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(ExtraDataModelUtil.createExaminationBloodGasData()))).andExpect(status().isOk());
	}

}
