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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.examination.controler.ExaminationApiController;
import org.shanoir.ng.examination.dto.mapper.ExaminationMapper;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.exporter.service.BIDSService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
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
public class ExaminationApiControllerTest {

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
	private BIDSService bidsService;

	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(examinationServiceMock).deleteById(1L);
		given(examinationServiceMock.findPage(Mockito.any(Pageable.class))).willReturn(new PageImpl<Examination>(Arrays.asList(new Examination())));
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());
		given(examinationServiceMock.save(Mockito.mock(Examination.class))).willReturn(new Examination());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteExaminationTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	public void findExaminationByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findExaminationsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(new PageRequest(0, 10))))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void saveNewExaminationTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createExamination())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updateExaminationTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createExamination())))
				.andExpect(status().isNoContent());
	}

	@Test
	@WithMockUser
	public void exportExaminationTestFileNotExisting() throws Exception {
		Examination exam = new Examination();
		exam.setExtraDataFilePathList(Collections.singletonList("/var/datasets-data/preclinical/BusyFile"));
		given(examinationServiceMock.findById(1L)).willReturn(exam);

		mvc.perform(MockMvcRequestBuilders.get("/examinations/preclinical/examinationId/1/export").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isInternalServerError());
	}
	
	@Test
	@WithMockUser
	public void exportExaminationNoContentTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/examinations/preclinical/examinationId/1/export").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

}
