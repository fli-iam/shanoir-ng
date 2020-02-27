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

package org.shanoir.ng.preclinical.therapies;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.ShanoirPreclinicalApplication;
import org.shanoir.ng.preclinical.references.RefsService;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.utils.TherapyModelUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for therapies controller.
 *
 * @author sloury
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = TherapyApiController.class)
@ContextConfiguration(classes = ShanoirPreclinicalApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(secure = false)
public class TherapyApiControllerTest {

	private static final String REQUEST_PATH = "/therapy";
	private static final String REQUEST_PATH_ALL = REQUEST_PATH ;
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";
	private static final String REQUEST_PATH_TYPE = REQUEST_PATH + "/type/Drug";

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private TherapyService therapyServiceMock;

	@MockBean
	private RefsService refsServiceMock;

	@MockBean
	private ShanoirEventService eventService;

	@Before
	public void setup() throws ShanoirException {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(therapyServiceMock).deleteById(1L);
		given(therapyServiceMock.findAll()).willReturn(Arrays.asList(new Therapy()));
		given(therapyServiceMock.findById(1L)).willReturn(new Therapy());
		given(therapyServiceMock.findByTherapyType(TherapyType.DRUG)).willReturn(Arrays.asList(new Therapy()));
		Therapy therapy = new Therapy();
		therapy.setId(Long.valueOf(123));
		given(therapyServiceMock.save(Mockito.any(Therapy.class))).willReturn(therapy );
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void deleteTherapyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findTherapyByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findTherapiesTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_ALL).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findTherapiesByTypeTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_TYPE).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void saveNewTherapyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(TherapyModelUtil.createTherapyBrain())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void updateTherapyTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(TherapyModelUtil.createTherapyBrain())))
				.andExpect(status().isOk());
	}

}
