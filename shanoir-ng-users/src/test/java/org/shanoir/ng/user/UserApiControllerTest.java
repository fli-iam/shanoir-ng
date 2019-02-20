package org.shanoir.ng.user;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.shared.dto.IdListDTO;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ForbiddenException;
import org.shanoir.ng.shared.exception.PasswordPolicyException;
import org.shanoir.ng.shared.jackson.JacksonUtils;
import org.shanoir.ng.user.controller.UserApiController;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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

	private static final String REQUEST_PATH = "/users";
	private static final String REQUEST_PATH_SEARCH = REQUEST_PATH + "/search";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private UserService userServiceMock;

	@Before
	public void setup() throws EntityNotFoundException, AccountNotOnDemandException, PasswordPolicyException, ForbiddenException  {
		User mockUser = ModelsUtil.createUser(1L);
		given(userServiceMock.confirmAccountRequest(mockUser)).willReturn(mockUser);
		doNothing().when(userServiceMock).deleteById(1L);
		doNothing().when(userServiceMock).denyAccountRequest(Mockito.anyLong());
		given(userServiceMock.findAll()).willReturn(Arrays.asList(mockUser));
		given(userServiceMock.findById(1L)).willReturn(mockUser);
		given(userServiceMock.findByIds(Arrays.asList(1L))).willReturn(Arrays.asList(new IdNameDTO()));
		given(userServiceMock.save(Mockito.mock(User.class))).willReturn(new User());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void confirmAccountRequestTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID + "/confirmaccountrequest")
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
				.content(JacksonUtils.serialize(ModelsUtil.createUser()))).andExpect(status().isNoContent());
	}

	@Test
	public void deleteUserTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void denyAccountRequestTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID + "/denyaccountrequest"))
				.andExpect(status().isNoContent());
	}

	@Test
	public void findUserByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void findUsersTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void saveNewUserTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(ModelsUtil.createUser())))
				.andExpect(status().isOk());
	}

	@Test
	public void searchUsersTest() throws Exception {
		final IdListDTO list = new IdListDTO();
		list.getIdList().add(1L);
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH_SEARCH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(list)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void updateUserTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(ModelsUtil.createUser())))
				.andExpect(status().isNoContent());
	}

}
