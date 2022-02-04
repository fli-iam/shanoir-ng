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

package org.shanoir.ng.dicom.web;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.dicom.web.dto.mapper.ExaminationToStudyDTOMapper;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.examination.service.ExaminationService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.paging.PageImpl;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = StudiesApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration()
@EnableSpringDataWebSupport
public class StudiesApiControllerTest {

	private static final String REQUEST_PATH = "/studies";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ExaminationToStudyDTOMapper examinationToStudyDTOMapperMock;

	@MockBean
	private ExaminationService examinationServiceMock;

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
	public void findStudiesTest() throws Exception {
		given(examinationServiceMock.findById(1L)).willReturn(new Examination());
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(PageRequest.of(0, 10))))
		.andExpect(status().isNoContent());
	}

}
