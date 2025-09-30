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

package org.shanoir.ng.manufacturermodel;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.manufacturermodel.controler.ManufacturerModelApiController;
import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.manufacturermodel.service.ManufacturerModelService;
import org.shanoir.ng.shared.jackson.JacksonUtils;
import org.shanoir.ng.shared.security.ControllerSecurityService;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Unit tests for manufacturer model controller.
 *
 * @author msimon
 *
 */

@WebMvcTest(ManufacturerModelApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ManufacturerModelApiControllerTest {

	private static final String REQUEST_PATH = "/manufacturermodels";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ManufacturerModelService manufacturerModelServiceMock;

	@MockBean(name = "controllerSecurityService")
	private ControllerSecurityService controllerSecurityService;

	@BeforeEach
	public void setup() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		ManufacturerModel model = new ManufacturerModel();
		model.setId(1L);
		given(manufacturerModelServiceMock.findAll()).willReturn(Arrays.asList(model));
		given(manufacturerModelServiceMock.findById(1L)).willReturn(Optional.of(model));
		given(manufacturerModelServiceMock.create(Mockito.mock(ManufacturerModel.class)))
				.willReturn(model);
		given(controllerSecurityService.idMatches(Mockito.anyLong(), Mockito.any(ManufacturerModel.class))).willReturn(true);
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void findManufacturerModelByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void findManufacturerModelsTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void saveNewManufacturerModelTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(ModelsUtil.createManufacturerModel())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void updateManufacturerModelTTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(ModelsUtil.createManufacturerModel())))
				.andExpect(status().isNoContent());
	}

}
