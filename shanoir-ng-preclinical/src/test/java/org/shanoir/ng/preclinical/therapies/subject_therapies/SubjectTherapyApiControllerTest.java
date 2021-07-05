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

package org.shanoir.ng.preclinical.therapies.subject_therapies;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.AnimalSubjectService;
import org.shanoir.ng.preclinical.therapies.Therapy;
import org.shanoir.ng.preclinical.therapies.TherapyService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.TherapyModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for subject therapy controller.
 *
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SubjectTherapyApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
@ActiveProfiles("test")
public class SubjectTherapyApiControllerTest {

	private static final String REQUEST_PATH_ANIMAL_SUBJECT = "/subject";
	private static final String SUBJECT_ID = "/1";
	private static final String REQUEST_PATH_THERAPY = "/therapy";
	private static final String REQUEST_PATH = REQUEST_PATH_ANIMAL_SUBJECT + SUBJECT_ID + REQUEST_PATH_THERAPY;
	private static final String REQUEST_PATH_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
	private static final String REQUEST_PATH_SUBJECT_BY_THERAPY = REQUEST_PATH_ANIMAL_SUBJECT + "/all"
			+ REQUEST_PATH_THERAPY + "/1";
	private static final String REQUEST_PATH_THERAPY_BY_SUBJECT = REQUEST_PATH_ANIMAL_SUBJECT + SUBJECT_ID
			+ REQUEST_PATH_THERAPY + "/all";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private SubjectTherapyService subTherapiesServiceMock;
	@MockBean
	private TherapyService therapiesServiceMock;
	@MockBean
	private AnimalSubjectService subjectsServiceMock;

	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(subTherapiesServiceMock).deleteById(1L);
		given(subTherapiesServiceMock.findAll()).willReturn(Arrays.asList(new SubjectTherapy()));
		given(subjectsServiceMock.findById(1L)).willReturn(new AnimalSubject());
		given(therapiesServiceMock.findById(1L)).willReturn(new Therapy());
		given(subTherapiesServiceMock.findById(1L)).willReturn(new SubjectTherapy());
		given(subTherapiesServiceMock.findAllByTherapy(new Therapy())).willReturn(Arrays.asList(new SubjectTherapy()));
		given(subTherapiesServiceMock.findAllByAnimalSubject(new AnimalSubject()))
				.willReturn(Arrays.asList(new SubjectTherapy()));
		given(subTherapiesServiceMock.save(Mockito.mock(SubjectTherapy.class))).willReturn(new SubjectTherapy());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteSubjectTherapyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteSubjectTherapiesTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_ALL).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findSubjectTherapyByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findSubjectTherapiesTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_ALL).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void saveNewSubjectTherapyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(TherapyModelUtil.createSubjectTherapy())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updateSubjectTherapyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(TherapyModelUtil.createSubjectTherapy())))
				.andExpect(status().isOk());
	}

	@Test
	public void findSubjectsByTherapyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_SUBJECT_BY_THERAPY).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

}
