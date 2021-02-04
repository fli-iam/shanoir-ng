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

package org.shanoir.ng.accountrequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;
import org.shanoir.ng.accountrequest.controller.AccountRequestApiController;
import org.shanoir.ng.accountrequest.model.AccountRequestInfo;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.shanoir.ng.shared.event.ShanoirEventService;
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

	@MockBean
	ShanoirEventService eventService;

	@Before
	public void setup() throws SecurityException {
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
		
		given(userServiceMock.createAccountRequest(Mockito.mock(User.class))).willReturn(new User());

		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(user)))
				.andExpect(status().isNoContent());
	}

	@Test
	public void saveNewAccountRequestChallengeTest() throws Exception {
		final User user = ModelsUtil.createUser(null);
		user.setEmail("test@te.st");
		user.setUsername("test");
		user.setId(2L);
		final AccountRequestInfo info = new AccountRequestInfo();
		info.setContact("contact");
		info.setFunction("function");
		info.setInstitution("institution");
		info.setChallenge(1L);
		info.setService("service");
		info.setStudy("study");
		info.setWork("work");
		user.setAccountRequestInfo(info);

		given(userServiceMock.createAccountRequest(Mockito.any(User.class))).willReturn(user);
		
		mvc.perform(MockMvcRequestBuilders.post(REQUEST_PATH).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(JacksonUtils.serialize(user)))
				.andExpect(status().isNoContent());

		ArgumentCaptor<ShanoirEvent> eventCaptor = new ArgumentCaptor();
		Mockito.verify(eventService).publishEvent(eventCaptor.capture());
		ShanoirEvent event = eventCaptor.getValue();
		assertEquals("1", event.getObjectId());
		assertEquals("2", ""+event.getUserId());
		assertEquals(user.getUsername(), event.getMessage());
	}

}
