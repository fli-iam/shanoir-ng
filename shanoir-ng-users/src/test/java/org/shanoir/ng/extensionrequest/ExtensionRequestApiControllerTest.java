package org.shanoir.ng.extensionrequest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Optional;

import org.mockito.Mockito;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.extensionrequest.controller.ExtensionRequestApiController;
import org.shanoir.ng.extensionrequest.model.ExtensionRequestInfo;
import org.shanoir.ng.shared.jackson.JacksonUtils;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.user.security.UserFieldEditionSecurityManager;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.user.service.UserUniqueConstraintManager;
import org.shanoir.ng.user.utils.KeycloakClient;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Test class for ExtensionRequestApiController
 * @author fli
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {ExtensionRequestApiController.class, UserUniqueConstraintManager.class, UserRepository.class})
@AutoConfigureMockMvc(addFilters = false)
public class ExtensionRequestApiControllerTest {

	private static final String REQUEST_PATH = "/extensionrequest";

	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private KeycloakClient keycloakClient;
	
	@MockBean
	private EmailService emailService;

	@MockBean
	private UserService userService;

	@MockBean
	private UserRepository userRepository;
	
	@MockBean
	private UserFieldEditionSecurityManager fieldEditionSecurityManager;
	
	@MockBean
	private UserUniqueConstraintManager uniqueConstraintManager;
	
	final static String EMAIL = "test@gmail.com";

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void extensionRequestTest() throws Exception {
		// GIVEN a disabled user with no current extension request
		User mockUser = ModelsUtil.createUser(1L);
		mockUser.setExpirationDate(LocalDate.now().minusDays(1));
		mockUser.setExtensionRequestDemand(Boolean.FALSE);
		Mockito.when(userService.findByEmailForExtension(EMAIL)).thenReturn(Optional.of(mockUser));

		// WHEN we request for an extension
		ExtensionRequestInfo extensionRequest = new ExtensionRequestInfo();
		extensionRequest.setEmail(EMAIL);
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH)
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
				.content(JacksonUtils.serialize(extensionRequest)))
				.andExpect(status().isOk());
	
		// THEN an extension is requested and password is changed
		Mockito.verify(keycloakClient).resetPassword(mockUser.getKeycloakId());
		Mockito.verify(emailService).notifyUserResetPassword(Mockito.eq(mockUser), Mockito.anyString());
		Mockito.verify(userService).requestExtension(Mockito.eq(mockUser.getId()), Mockito.any(ExtensionRequestInfo.class));
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void extensionRequestNoUserTest() throws Exception {
		// GIVEN a non existing user
		Mockito.when(userService.findByEmailForExtension(EMAIL)).thenReturn(Optional.ofNullable(null));
		
		// WHEN we request for an extension
		ExtensionRequestInfo extensionRequest = new ExtensionRequestInfo();
		extensionRequest.setEmail(EMAIL);
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH)
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
				.content(JacksonUtils.serialize(extensionRequest)))
				.andExpect(status().isBadRequest());
	
		// THEN a 404 is returned
		Mockito.verifyZeroInteractions(keycloakClient);
		Mockito.verifyZeroInteractions(emailService);
	}
	
	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void extensionRequestUserEnabledTest() throws Exception {
		// GIVEN an ENABLED user with no current extension request
		User mockUser = ModelsUtil.createUser(1L);
		mockUser.setExpirationDate(LocalDate.now().plusDays(1));
		Mockito.when(userService.findByEmailForExtension(EMAIL)).thenReturn(Optional.of(mockUser));

		// WHEN we request for an extension
		ExtensionRequestInfo extensionRequest = new ExtensionRequestInfo();
		extensionRequest.setEmail(EMAIL);
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH)
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
				.content(JacksonUtils.serialize(extensionRequest)))
				.andExpect(status().isNotAcceptable());
	
		// THEN a NOT acceptable error is sent
		Mockito.verifyZeroInteractions(keycloakClient);
		Mockito.verifyZeroInteractions(emailService);	}
	
	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void extensionRequestUserAreadyHaveARequestTest() throws Exception {
		// GIVEN a disabled user with a current extension request
		User mockUser = ModelsUtil.createUser(1L);
		mockUser.setExpirationDate(LocalDate.now().minusDays(1));
		mockUser.setExtensionRequestDemand(Boolean.TRUE);
		Mockito.when(userService.findByEmailForExtension(EMAIL)).thenReturn(Optional.of(mockUser));

		// WHEN we request for an extension
		ExtensionRequestInfo extensionRequest = new ExtensionRequestInfo();
		extensionRequest.setEmail(EMAIL);
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH)
				.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
				.content(JacksonUtils.serialize(extensionRequest)))
				.andExpect(status().isNotAcceptable());
	
		// THEN a NOT acceptable error is sent
		Mockito.verifyZeroInteractions(keycloakClient);
		Mockito.verifyZeroInteractions(emailService);
	}
}
