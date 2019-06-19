package org.shanoir.ng.accountrequest;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.accountrequest.controller.AccountRequestApiController;
import org.shanoir.ng.accountrequest.model.AccountRequestInfo;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.jackson.JacksonUtils;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.user.security.UserFieldEditionSecurityManager;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.user.service.UserUniqueConstraintManager;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
@WebMvcTest(controllers = AccountRequestApiController.class)
@AutoConfigureMockMvc(secure = false)
public class AccountRequestApiControllerTest {

	private static final String REQUEST_PATH = "/accountrequest";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private UserService userServiceMock;
	
	@MockBean
	private UserRepository userRepositoryMock;
	
	@MockBean
	private UserFieldEditionSecurityManager fieldEditionSecurityManager;
	
	@MockBean
	private UserUniqueConstraintManager uniqueConstraintManager;

	@Before
	public void setup() throws SecurityException {
		given(userServiceMock.createAccountRequest(Mockito.mock(User.class))).willReturn(new User());
		given(fieldEditionSecurityManager.validate(Mockito.any(User.class))).willReturn(new FieldErrorMap());
		given(uniqueConstraintManager.validate(Mockito.any(User.class))).willReturn(new FieldErrorMap());
	}

	@Test
	public void saveNewAccountRequestTest() throws Exception {
		final User user = ModelsUtil.createUser(null);
		user.setEmail("test@te.st");
		user.setUsername("test");
		final AccountRequestInfo info = new AccountRequestInfo();
		info.setContact("contact");
		info.setFunction("function");
		info.setInstitution("institution");
		info.setService("service");
		info.setStudy("study");
		info.setWork("work");
		user.setAccountRequestInfo(info);

		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(user)))
				.andExpect(status().isNoContent());
	}

}
