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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shanoir.ng.accountrequest.repository.AccountRequestInfoRepository;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.role.repository.RoleRepository;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.user.model.ExtensionRequestInfo;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.user.service.UserService;
import org.shanoir.ng.user.utils.KeycloakClient;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * User security service test.
 * 
 * @author jlouis
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class UserServiceSecurityTest {

	private static final long USER_ID = 1L;
	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private static final String UPDATED_USER_FIRSTNAME = "test";
	private static final String USER_USERNAME = "name";
	private static final String USER_EMAIL = "test@shanoir.fr";

	@MockBean
	private AccountRequestInfoRepository accountRequestInfoRepository;

	@MockBean
	private EmailService emailService;

	@MockBean
	private KeycloakClient keycloakClient;

	@MockBean
	private RabbitTemplate rabbitTemplate;

	@MockBean
	private RoleRepository roleRepository;

	@MockBean
	private UserRepository userRepository;
	
	@MockBean
    private ApplicationEventPublisher publisher;

	@Autowired
	private UserService userService;
	
	private User mockUser;
	private User mockNewUser;
	private User mockAccountReqUser;
	private User mockMe;

	@Before
	public void setup() {
		mockUser = ModelsUtil.createUser(USER_ID);
		mockNewUser = ModelsUtil.createUser(null);
		mockAccountReqUser = ModelsUtil.createUser(null);
			mockAccountReqUser.setAccountRequestDemand(true);
			mockAccountReqUser.setRole(null);
		mockMe = ModelsUtil.createAdmin(LOGGED_USER_ID);
		
//		given(userRepository.findOne(USER_ID)).willReturn(mockUser);
//		given(userRepository.findOne(LOGGED_USER_ID)).willReturn(mockMe);
		given(userRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createUser()));
//		given(userRepository.findByUsername(Mockito.anyString())).willReturn(Optional.of(ModelsUtil.createUser()));
//		given(userRepository.findByIdIn(Mockito.anyListOf(Long.class)))
//				.willReturn(Arrays.asList(createUser()));
//		given(userRepository
//				.findByExpirationDateLessThanAndFirstExpirationNotificationSentFalse(Mockito.any(LocalDate.class)))
//						.willReturn(Arrays.asList(ModelsUtil.createUser()));
//		given(userRepository
//				.findByExpirationDateLessThanAndSecondExpirationNotificationSentFalse(Mockito.any(LocalDate.class)))
//						.willReturn(Arrays.asList(ModelsUtil.createUser()));
//		given(userRepository.findByEmail(USER_EMAIL)).willReturn(Optional.of(ModelsUtil.createUser()));
//		given(userRepository.save(Mockito.any(User.class))).willReturn(ModelsUtil.createUser());
//		given(roleRepository.findByName(Mockito.anyString())).willReturn(ModelsUtil.createUserRole());
//		given(keycloakClient.createUserWithPassword(Mockito.any(User.class), Mockito.anyString())).willReturn(RandomStringUtils.randomAlphanumeric(10));
	}
	
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException {

		assertAccessDenied(userService::confirmAccountRequest, mockUser);
		assertAccessDenied(userService::deleteById, USER_ID);
		assertAccessDenied(userService::denyAccountRequest, USER_ID);
		assertAccessDenied(userService::findAll);
		assertAccessDenied(userService::findByEmail, USER_EMAIL);
		assertAccessDenied(userService::findById, USER_ID);
		assertAccessDenied(userService::findByUsername, USER_USERNAME);
		assertAccessDenied(userService::getUsersToReceiveFirstExpirationNotification);
		assertAccessDenied(userService::getUsersToReceiveSecondExpirationNotification);
		assertAccessAuthorized(userService::requestExtension, USER_ID, new ExtensionRequestInfo());
		assertAccessDenied(userService::create, mockNewUser);
		
		assertAccessAuthorized(userService::createAccountRequest, mockAccountReqUser);
		User mockBadAccountReqUser = ModelsUtil.createUser(666L);
		mockBadAccountReqUser.setAccountRequestDemand(true);
		mockBadAccountReqUser.setRole(null);
		assertAccessDenied(userService::createAccountRequest, mockBadAccountReqUser);
		mockBadAccountReqUser.setId(null);
		mockBadAccountReqUser.setAccountRequestDemand(false);
		assertAccessDenied(userService::createAccountRequest, mockBadAccountReqUser);
		mockBadAccountReqUser.setAccountRequestDemand(true);
		mockBadAccountReqUser.setRole(ModelsUtil.createAdminRole());
		assertAccessDenied(userService::createAccountRequest, mockBadAccountReqUser);
		
		assertAccessDenied(userService::findByIds, Arrays.asList(USER_ID));
		assertAccessDenied(userService::update, mockUser);
		assertAccessDenied(userService::updateExpirationNotification, mockUser, true);
		assertAccessDenied(userService::updateLastLogin, USER_USERNAME);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException {

		assertAccessDenied(userService::confirmAccountRequest, mockUser);
		assertAccessDenied(userService::deleteById, USER_ID);
		assertAccessDenied(userService::denyAccountRequest, USER_ID);
		assertAccessDenied(userService::findAll);
		assertAccessDenied(userService::findByEmail, USER_EMAIL);
		assertAccessDenied(userService::findById, USER_ID);
		assertAccessAuthorized(userService::findById, LOGGED_USER_ID);
		assertAccessDenied(userService::findByUsername, USER_USERNAME);
		assertAccessAuthorized(userService::findByUsername, LOGGED_USER_USERNAME);
		assertAccessDenied(userService::getUsersToReceiveFirstExpirationNotification);
		assertAccessDenied(userService::getUsersToReceiveSecondExpirationNotification);
		assertAccessAuthorized(userService::requestExtension, USER_ID, new ExtensionRequestInfo());
		assertAccessDenied(userService::create, mockNewUser);
		assertAccessAuthorized(userService::createAccountRequest, mockAccountReqUser);
		assertAccessAuthorized(userService::findByIds, Arrays.asList(USER_ID));
		assertAccessDenied(userService::update, mockUser);
		assertAccessAuthorized(userService::update, mockMe);
		assertAccessDenied(userService::updateExpirationNotification, mockUser, true);
		assertAccessAuthorized(userService::updateLastLogin, USER_USERNAME);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException {

		assertAccessDenied(userService::confirmAccountRequest, mockUser);
		assertAccessDenied(userService::deleteById, USER_ID);
		assertAccessDenied(userService::denyAccountRequest, USER_ID);
		assertAccessAuthorized(userService::findAll);
		for (User user : userService.findAll()) {
			assertNull(user.getEmail());
			assertNull(user.getLastLogin());
			assertNull(user.getCreationDate());
		}
		assertAccessDenied(userService::findByEmail, USER_EMAIL);
		assertAccessDenied(userService::findById, USER_ID);
		assertAccessAuthorized(userService::findById, LOGGED_USER_ID);
		assertAccessDenied(userService::findByUsername, USER_USERNAME);
		assertAccessAuthorized(userService::findByUsername, LOGGED_USER_USERNAME);
		assertAccessDenied(userService::getUsersToReceiveFirstExpirationNotification);
		assertAccessDenied(userService::getUsersToReceiveSecondExpirationNotification);
		assertAccessAuthorized(userService::requestExtension, USER_ID, new ExtensionRequestInfo());
		assertAccessDenied(userService::create, mockNewUser);
		assertAccessAuthorized(userService::createAccountRequest, mockAccountReqUser);
		assertAccessAuthorized(userService::findByIds, Arrays.asList(USER_ID));
		assertAccessDenied(userService::update, mockUser);
		assertAccessAuthorized(userService::update, mockMe);
		assertAccessDenied(userService::updateExpirationNotification, mockUser, true);
		assertAccessAuthorized(userService::updateLastLogin, USER_USERNAME);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException {

		mockUser.setAccountRequestDemand(true);
		assertAccessAuthorized(userService::confirmAccountRequest, mockUser);
		mockUser.setAccountRequestDemand(false);
		assertAccessAuthorized(userService::deleteById, USER_ID);
		mockUser.setAccountRequestDemand(true);
		assertAccessAuthorized(userService::denyAccountRequest, USER_ID);
		mockUser.setAccountRequestDemand(false);
		assertAccessAuthorized(userService::findAll);
		for (User user : userService.findAll()) {
			assertNotNull(user.getEmail());
			assertNotNull(user.getLastLogin());
			assertNotNull(user.getCreationDate());
		}
		assertAccessAuthorized(userService::findByEmail, USER_EMAIL);
		assertAccessAuthorized(userService::findById, USER_ID);
		assertAccessAuthorized(userService::findById, LOGGED_USER_ID);
		assertAccessAuthorized(userService::findByUsername, USER_USERNAME);
		assertAccessAuthorized(userService::findByUsername, LOGGED_USER_USERNAME);
		assertAccessAuthorized(userService::getUsersToReceiveFirstExpirationNotification);
		assertAccessAuthorized(userService::getUsersToReceiveSecondExpirationNotification);
		assertAccessAuthorized(userService::requestExtension, USER_ID, new ExtensionRequestInfo());
		assertAccessAuthorized(userService::create, mockNewUser);
		assertAccessAuthorized(userService::createAccountRequest, mockAccountReqUser);
		assertAccessAuthorized(userService::findByIds, Arrays.asList(USER_ID));
		assertAccessAuthorized(userService::update, mockUser);
		assertAccessAuthorized(userService::update, mockMe);
		assertAccessAuthorized(userService::updateExpirationNotification, mockUser, true);
		assertAccessAuthorized(userService::updateLastLogin, USER_USERNAME);
	}

}
