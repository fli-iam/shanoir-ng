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

package org.shanoir.ng.preclinical.pathologies.subject_pathologies;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.PathologyService;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.preclinical.subjects.AnimalSubjectService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.PathologyModelUtil;
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
 * Unit tests for subject pathology controller.
 *
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SubjectPathologyApiController.class)
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
@ActiveProfiles("test")
public class SubjectPathologyApiControllerTest {

	private static final String REQUEST_PATH_SUBJECT = "/subject";
	private static final String SUBJECT_ID = "/1";
	private static final String REQUEST_PATH_PATHOLOGY = "/pathology";
	private static final String REQUEST_PATH = REQUEST_PATH_SUBJECT + SUBJECT_ID + REQUEST_PATH_PATHOLOGY;
	private static final String REQUEST_PATH_ALL = REQUEST_PATH + "/all";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
	private static final String REQUEST_PATH_SUBJECT_BY_PATHO = REQUEST_PATH_SUBJECT + "/all" + REQUEST_PATH_PATHOLOGY
			+ "/1";
	private static final String REQUEST_PATH_PATHO_BY_SUBJECT = REQUEST_PATH_SUBJECT + SUBJECT_ID
			+ REQUEST_PATH_PATHOLOGY + "/all";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private SubjectPathologyService subPathosServiceMock;
	@MockBean
	private AnimalSubjectService subjectsServiceMock;
	@MockBean
	private PathologyService pathologiesServiceMock;
	@MockBean
	private RefsService refsServiceMock;

	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(subPathosServiceMock).deleteById(1L);
		given(subPathosServiceMock.findAll()).willReturn(Arrays.asList(new SubjectPathology()));
		given(subjectsServiceMock.findById(1L)).willReturn(new AnimalSubject());
		given(pathologiesServiceMock.findById(1L)).willReturn(new Pathology());
		given(refsServiceMock.findById(1L)).willReturn(new Reference());
		given(subPathosServiceMock.findByAnimalSubject(new AnimalSubject()))
				.willReturn(Arrays.asList(new SubjectPathology()));
		given(subPathosServiceMock.findAllByPathology(new Pathology()))
				.willReturn(Arrays.asList(new SubjectPathology()));
		given(subPathosServiceMock.findById(1L)).willReturn(new SubjectPathology());
		given(subPathosServiceMock.save(Mockito.mock(SubjectPathology.class))).willReturn(new SubjectPathology());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteSubjectPathologyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "adminRole" })
	public void deleteSubjectPathologiesTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_ALL).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findSubjectPathologyByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findSubjectPathologiesTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_ALL).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void saveNewSubjectPathologyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(PathologyModelUtil.createSubjectPathology()))).andExpect(status().isOk());
	}

	@Test
	@WithMockUser
	public void updateSubjectPathologyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(gson.toJson(PathologyModelUtil.createSubjectPathology()))).andExpect(status().isOk());
	}

	@Test
	public void findSubjectsByPathologyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_SUBJECT_BY_PATHO).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

}
