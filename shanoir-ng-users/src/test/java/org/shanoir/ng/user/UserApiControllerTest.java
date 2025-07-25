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

package org.shanoir.ng.user;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.shanoir.ng.accessrequest.controller.AccessRequestService;
import org.shanoir.ng.shared.core.model.IdList;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.event.ShanoirEventService;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.jackson.JacksonUtils;
import org.shanoir.ng.shared.security.ControllerSecurityService;
import org.shanoir.ng.shared.validation.FindByRepository;
import org.shanoir.ng.user.controller.UserApiController;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.user.security.IsMeSecurityService;
import org.shanoir.ng.user.security.UserFieldEditionSecurityManager;
import org.shanoir.ng.user.security.UserPrivacySecurityService;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.user.service.UserUniqueConstraintManager;
import org.shanoir.ng.user.service.VIPUserService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
 * Unit tests for user controller.
 *S
 * @author msimon
 *
 */
@WebMvcTest(controllers = {UserApiController.class, ControllerSecurityService.class, UserPrivacySecurityService.class,
		IsMeSecurityService.class, UserFieldEditionSecurityManager.class, UserUniqueConstraintManager.class, UserRepository.class, 
		AccessRequestService.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class UserApiControllerTest {

	private static final String REQUEST_PATH = "/users";
	private static final String REQUEST_PATH_SEARCH = REQUEST_PATH + "/search";
	private static final String REQUEST_PATH_WITH_ID = REQUEST_PATH + "/1";

	@Autowired
	private MockMvc mvc;
	
	@MockBean
	private UserService userService;
	
	@MockBean
	private FindByRepository<User> findByRepositoryMock;
	
	@MockBean
	private UserRepository userRepository;

	@MockBean
	private RabbitTemplate rabbitTemplate;

	@MockBean
	private AccessRequestService accessRequestService;

	@MockBean
	private ShanoirEventService eventService;

	@MockBean
	private VIPUserService vipUserService;
	
	@BeforeAll
	public static void beforeClass() {
	    System.setProperty("vip.enabled", "false");
	}

	@BeforeEach
	public void setup() throws EntityNotFoundException, AccountNotOnDemandException, SecurityException  {
		User mockUser = ModelsUtil.createUser(1L);
		given(userService.confirmAccountRequest(Mockito.any(User.class))).willReturn(mockUser);
		doNothing().when(userService).deleteById(1L);
		doNothing().when(userService).denyAccountRequest(Mockito.anyLong());
		given(userService.findAll()).willReturn(Arrays.asList(mockUser));
		given(userService.findById(1L)).willReturn(mockUser);
		given(userService.findByIds(Arrays.asList(1L))).willReturn(Arrays.asList(new IdName()));
		given(userService.create(Mockito.mock(User.class))).willReturn(new User());
		given(findByRepositoryMock.findBy(Mockito.anyString(), Mockito.any(), (Class) Mockito.any())).willReturn(Arrays.asList(mockUser));
		given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));
	}

	@Test
	@WithMockKeycloakUser(authorities = { "ROLE_ADMIN" }, id = 0)
	public void deleteUserTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.delete(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNoContent());
	}

	@Test
	@WithMockKeycloakUser(authorities = { "ROLE_ADMIN" }, id = 0)
	public void findUserByIdTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_EXPERT" })
	public void findUsersTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(REQUEST_PATH).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void saveNewUserTest() throws Exception {
		given(findByRepositoryMock.findBy(Mockito.anyString(), Mockito.any(), Mockito.any())).willReturn(new ArrayList<User>());
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(ModelsUtil.createUser())))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_USER" })
	public void searchUsersTest() throws Exception {
		final IdList list = new IdList();
		list.getIdList().add(1L);
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH_SEARCH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(list)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void updateUserTest() throws Exception {
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(ModelsUtil.createUser(1L))))
				.andExpect(status().isNoContent());
	}
	
	@Test
	@WithMockKeycloakUser(authorities = { "ROLE_USER" }, id = 0)
	public void fieldAccessTest() throws Exception {
		User user = ModelsUtil.createAdmin(1L);
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(user)))
				.andExpect(status().isUnprocessableEntity());
		user = ModelsUtil.createUser(1L);
		user.setExpirationDate(LocalDate.now().plusYears(100));
		mvc.perform(MockMvcRequestBuilders.put(REQUEST_PATH_WITH_ID).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(user)))
				.andExpect(status().isUnprocessableEntity());
	}

}
