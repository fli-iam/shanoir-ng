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

package org.shanoir.ng.role;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.role.controller.RoleApiController;
import org.shanoir.ng.role.model.Role;
import org.shanoir.ng.role.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Unit tests for role controller.
 *
 * @author msimon
 *
 */
@WebMvcTest(controllers = RoleApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class RoleApiControllerTest {

	private static final String REQUEST_PATH = "/roles";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private RoleService roleServiceMock;

	@BeforeEach
	public void setup() {
		given(roleServiceMock.findAll()).willReturn(Arrays.asList(new Role()));
	}

	@Test
	public void findRolesTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

}
