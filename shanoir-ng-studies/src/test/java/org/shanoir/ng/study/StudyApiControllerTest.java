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

package org.shanoir.ng.study;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.SecurityContextTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for study controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = StudyApiController.class)
@AutoConfigureMockMvc(secure = false)
public class StudyApiControllerTest {

	private static final String REQUEST_PATH = "/studies";
	private static final String REQUEST_PATH_FOR_NAMES = REQUEST_PATH + "/names";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
	private static final String REQUEST_PATH_FOR_MEMBERS = REQUEST_PATH_WITH_ID + "/members";
	private static final String REQUEST_PATH_FOR_MEMBER_WITH_ID = REQUEST_PATH_FOR_MEMBERS + "/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private StudyMapper studyMapperMock;

	@MockBean
	private StudyService studyServiceMock;

	@Before
	public void setup() throws ShanoirStudiesException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		given(studyMapperMock.studiesToStudyDTOs(Mockito.anyListOf(Study.class)))
				.willReturn(Arrays.asList(new StudyDTO()));
		given(studyMapperMock.studyToStudyDTO(Mockito.any(Study.class))).willReturn(new StudyDTO());

		doNothing().when(studyServiceMock).deleteById(1L);
		given(studyServiceMock.findAll()).willReturn(Arrays.asList(new Study()));
		given(studyServiceMock.findById(1L, 1L)).willReturn(new Study());
		given(studyServiceMock.findIdsAndNames()).willReturn(Arrays.asList(new IdNameDTO()));
		given(studyServiceMock.save(Mockito.mock(Study.class))).willReturn(new Study());
	}

	// TODO: manage keycloak token
	// @Test
	public void addMember() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_FOR_MEMBERS).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createStudyUser())))
				.andExpect(status().isNoContent());
	}

	// TODO: manage keycloak token
	// @Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void deleteStudyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	public void findStudiesTest() throws Exception {
		SecurityContextTestUtil.initAuthenticationContext();

		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findStudiesNamesTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_FOR_NAMES).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	// TODO: manage keycloak token
	// @Test
	public void findStudyByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	// TODO: manage keycloak token
	// @Test
	public void removeMemberTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_FOR_MEMBER_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void saveNewStudyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createStudy())))
				.andExpect(status().isOk());
	}

	// TODO: manage keycloak token
	// @Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void updateStudyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createStudy())))
				.andExpect(status().isNoContent());
	}

}
