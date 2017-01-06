package org.shanoir.ng.controller.rest;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.model.Role;
import org.shanoir.ng.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Unit tests for role controller.
 *
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = RoleApiController.class)
@AutoConfigureMockMvc(secure = false)
public class RoleApiControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private RoleService roleServiceMock;

	@Before
	public void setup() {
		given(roleServiceMock.findAll()).willReturn(Arrays.asList(new Role()));
	}

	@Test
	public void findRolesTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/role/all").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

}
