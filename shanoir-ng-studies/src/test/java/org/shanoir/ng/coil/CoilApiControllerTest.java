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

package org.shanoir.ng.coil;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.coil.controler.CoilApiController;
import org.shanoir.ng.coil.dto.CoilDTO;
import org.shanoir.ng.coil.dto.mapper.CoilMapper;
import org.shanoir.ng.coil.model.Coil;
import org.shanoir.ng.coil.service.CoilService;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.jackson.JacksonUtils;
import org.shanoir.ng.shared.security.ControllerSecurityService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Unit tests for coil controller.
 *
 * @author msimon
 *
 */

@WebMvcTest(CoilApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockKeycloakUser(id = 123)
@ActiveProfiles("test")
public class CoilApiControllerTest {

	private static final String REQUEST_PATH = "/coils";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private CoilMapper coilMapperMock;

	@MockBean
	private CoilService coilServiceMock;

	@MockBean
	private ShanoirEventService eventService;

	@MockBean(name = "controllerSecurityService")
	private ControllerSecurityService controllerSecurityService;

	@BeforeEach
	public void setup() throws EntityNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		given(coilMapperMock.coilsToCoilDTOs(Mockito.anyList()))
				.willReturn(Arrays.asList(new CoilDTO()));
		given(coilMapperMock.coilToCoilDTO(Mockito.any(Coil.class))).willReturn(new CoilDTO());
		doNothing().when(coilServiceMock).deleteById(1L);
		given(coilServiceMock.findAll()).willReturn(Arrays.asList(new Coil()));
		given(coilServiceMock.findById(1L)).willReturn(Optional.of(new Coil()));
		Coil coil = new Coil();
		coil.setId(Long.valueOf(123));
		given(coilServiceMock.create(Mockito.any(Coil.class))).willReturn(coil );
		given(controllerSecurityService.idMatches(Mockito.anyLong(), Mockito.any(Coil.class))).willReturn(true);
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void deleteCoilTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	public void findCoilByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findCoilsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void saveNewCoilTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(ModelsUtil.createCoil())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockKeycloakUser(id = 12, username = "test", authorities = { "ROLE_ADMIN" })
	public void updateCoilTest() throws Exception {
		Coil coil = ModelsUtil.createCoil();
		coil.setId(1L);
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(coil)))
				.andExpect(status().isNoContent());
	}

}
