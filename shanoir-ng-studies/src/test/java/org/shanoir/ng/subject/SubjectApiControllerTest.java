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

package org.shanoir.ng.subject;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.subject.controler.SubjectApiController;
import org.shanoir.ng.subject.dto.SubjectDTO;
import org.shanoir.ng.subject.dto.mapper.SubjectMapper;
import org.shanoir.ng.subject.dto.mapper.SubjectMappingUtilsService;
import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.service.SubjectService;
import org.shanoir.ng.subject.service.SubjectUniqueConstraintManager;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for subject controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SubjectApiController.class)
@ContextConfiguration(classes = {SubjectApiController.class, SubjectMappingUtilsService.class, RestTemplate.class, MicroserviceRequestsService.class})
@AutoConfigureMockMvc(secure = false)
public class SubjectApiControllerTest {

	private static final String REQUEST_PATH = "/subjects";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private SubjectService subjectServiceMock;
	
	@MockBean
	private StudyService studyService;

	@MockBean
	private SubjectMapper subjectMapperMock;
	
	@MockBean
	private SubjectUniqueConstraintManager uniqueConstraintManager;

	@Before
	public void setup() throws EntityNotFoundException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		given(subjectMapperMock.subjectsToSubjectDTOs(Mockito.anyListOf(Subject.class)))
		.willReturn(Arrays.asList(new SubjectDTO()));
		
		doNothing().when(subjectServiceMock).deleteById(1L);
		given(subjectServiceMock.findAll()).willReturn(Arrays.asList(new Subject()));
		given(subjectServiceMock.findById(1L)).willReturn(new Subject());
		given(subjectServiceMock.create(Mockito.mock(Subject.class))).willReturn(new Subject());
		given(uniqueConstraintManager.validate(Mockito.any(Subject.class))).willReturn(new FieldErrorMap());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteSubjectTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	public void findSubjectByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findSubjectsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void saveNewSubjectTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createSubject())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updateSubjectTest() throws Exception {
		Subject subject = ModelsUtil.createSubject();
		subject.setId(1L);
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(subject)))
				.andExpect(status().isNoContent());
	}

}
