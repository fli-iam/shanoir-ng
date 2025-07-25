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

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.manufacturermodel.controler.ManufacturerApiController;
import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.manufacturermodel.service.ManufacturerService;
import org.shanoir.ng.manufacturermodel.service.ManufacturerUniqueConstraintManager;
import org.shanoir.ng.shared.error.FieldErrorMap;
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
 * Unit tests for manufacturer controller.
 *
 * @author msimon
 *
 */

@WebMvcTest(controllers = {ManufacturerApiController.class, ControllerSecurityService.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ManufacturerApiControllerTest {

	private static final String REQUEST_PATH = "/manufacturers";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ManufacturerService manufacturerServiceMock;
	
	@MockBean
	private ManufacturerUniqueConstraintManager uniqueConstraintManager;

	@BeforeEach
	public void setup() {
		Manufacturer manuf = new Manufacturer();
		manuf.setId(1L);
		given(manufacturerServiceMock.findAll()).willReturn(Arrays.asList(manuf));
		given(manufacturerServiceMock.findById(1L)).willReturn(Optional.of(manuf));
		given(manufacturerServiceMock.create(Mockito.mock(Manufacturer.class))).willReturn(manuf);
		given(uniqueConstraintManager.validate(Mockito.any(Manufacturer.class))).willReturn(new FieldErrorMap());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void findManufacturerByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void findManufacturersTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void saveNewManufacturerTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(ModelsUtil.createManufacturer())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void updateManufacturerTest() throws Exception {
		Manufacturer manuf = ModelsUtil.createManufacturer();
		manuf.setId(1L);
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(manuf)))
				.andExpect(status().isNoContent());
	}

}
