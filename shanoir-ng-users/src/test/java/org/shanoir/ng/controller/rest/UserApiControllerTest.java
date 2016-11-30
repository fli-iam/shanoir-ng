package org.shanoir.ng.controller.rest;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.model.User;
import org.shanoir.ng.service.UserService;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for user controller.
 * 
 * @author msimon
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = UserApiController.class)
@AutoConfigureMockMvc(secure = false)
public class UserApiControllerTest {

	private Gson gson;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private UserService userServiceMock;

	@Before
	public void setup() {
		gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

		doNothing().when(userServiceMock).deleteById(1L);
		given(userServiceMock.findAll()).willReturn(Arrays.asList(new User()));
		given(userServiceMock.findById(1L)).willReturn(new User());
		doNothing().when(userServiceMock).save(Mockito.mock(User.class));
	}

	@Test
	public void deleteUserTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete("/user/1").header("X-XSRF-TOKEN", "test")
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isNoContent());
	}

	@Test
	public void findUserByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/user/1").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findUsersTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get("/user/all").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void saveNewUserTest() throws Exception {
		mvc.perform(
				MockMvcRequestBuilders.post("/user").header("X-XSRF-TOKEN", "test").accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createUser())))
				.andExpect(status().isOk());
	}

	@Test
	public void updateNewUserTest() throws Exception {
		mvc.perform(
				MockMvcRequestBuilders.put("/user/1").header("X-XSRF-TOKEN", "test").accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON).content(gson.toJson(ModelsUtil.createUser())))
				.andExpect(status().isNoContent());
	}

}
