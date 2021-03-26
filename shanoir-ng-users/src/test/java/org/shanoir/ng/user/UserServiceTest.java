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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.shanoir.ng.accountrequest.model.AccountRequestInfo;
import org.shanoir.ng.accountrequest.repository.AccountRequestInfoRepository;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.extensionrequest.model.ExtensionRequestInfo;
import org.shanoir.ng.role.repository.RoleRepository;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.AccountNotOnDemandException;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.ForbiddenException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.shared.exception.ShanoirUsersException;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * User detail service test.
 * 
 * @author msimon
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class UserServiceTest {

	private static final long USER_ID = 1L;
	private static final String UPDATED_USER_FIRSTNAME = "test";
	private static final String USER_USERNAME = "name";

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

	@Before
	public void setup() throws SecurityException {
		given(userRepository.findAll()).willReturn(Arrays.asList(ModelsUtil.createUser()));
		given(userRepository.findByUsername(Mockito.anyString())).willReturn(Optional.of(ModelsUtil.createUser()));
		given(userRepository.findByIdIn(Mockito.anyListOf(Long.class)))
				.willReturn(Arrays.asList(createUser()));
		given(userRepository.findOne(USER_ID)).willReturn(ModelsUtil.createUser(USER_ID));
		given(userRepository
				.findByExpirationDateLessThanAndFirstExpirationNotificationSentFalse(Mockito.any(LocalDate.class)))
						.willReturn(Arrays.asList(ModelsUtil.createUser()));
		given(userRepository
				.findByExpirationDateLessThanAndSecondExpirationNotificationSentFalse(Mockito.any(LocalDate.class)))
						.willReturn(Arrays.asList(ModelsUtil.createUser()));
		given(userRepository.save(Mockito.any(User.class))).willReturn(ModelsUtil.createUser());
		given(roleRepository.findByName(Mockito.anyString())).willReturn(ModelsUtil.createUserRole());
		given(keycloakClient.createUserWithPassword(Mockito.any(User.class), Mockito.anyString())).willReturn(RandomStringUtils.randomAlphanumeric(10));
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void confirmAccountRequestTest() throws AccountNotOnDemandException, EntityNotFoundException  {
		final User user = ModelsUtil.createUser();
		user.setAccountRequestDemand(true);
		given(userRepository.findOne(USER_ID)).willReturn(user);

		final User updatedUser = userService.confirmAccountRequest(createUser());
		Assert.assertNotNull(updatedUser);
		Assert.assertTrue(UPDATED_USER_FIRSTNAME.equals(updatedUser.getFirstName()));

		Mockito.verify(userRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		user.setAccountRequestDemand(false);
		Mockito.verify(userRepository, Mockito.times(1)).save(user);
	}

	@Test(expected = EntityNotFoundException.class)
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void confirmAccountRequestBadUserIdTest() throws EntityNotFoundException, AccountNotOnDemandException {
		given(userRepository.findOne(USER_ID)).willReturn(null);

		userService.confirmAccountRequest(new User());

		Mockito.verify(userRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(userRepository, Mockito.times(0)).save(Mockito.any(User.class));
	}

	@Test(expected = ShanoirUsersException.class)
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void confirmAccountRequestNoDemandTest() throws AccountNotOnDemandException, EntityNotFoundException {
		User user = ModelsUtil.createUser(USER_ID);
		given(userRepository.findOne(USER_ID)).willReturn(user);
		
		userService.confirmAccountRequest(user);

		Mockito.verify(userRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(userRepository, Mockito.times(0)).save(Mockito.any(User.class));
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void denyAccountRequestTest() throws AccountNotOnDemandException, EntityNotFoundException {
		final User user = ModelsUtil.createUser();
		user.setAccountRequestDemand(true);
		given(userRepository.findOne(USER_ID)).willReturn(user);

		userService.denyAccountRequest(USER_ID);
		Mockito.verify(userRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(userRepository, Mockito.times(1)).delete(USER_ID);
	}

	@Test(expected = EntityNotFoundException.class)
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void denyAccountRequestBadUserIdTest() throws AccountNotOnDemandException, EntityNotFoundException {
		given(userRepository.findOne(USER_ID)).willReturn(null);

		userService.denyAccountRequest(USER_ID);

		Mockito.verify(userRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(userRepository, Mockito.times(0)).delete(USER_ID);
	}

	@Test(expected = AccountNotOnDemandException.class)
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void denyAccountRequestNoDemandTest() throws EntityNotFoundException, AccountNotOnDemandException {
		given(userRepository.findOne(USER_ID)).willReturn(ModelsUtil.createUser());

		userService.denyAccountRequest(USER_ID);

		Mockito.verify(userRepository, Mockito.times(1)).findOne(Mockito.anyLong());
		Mockito.verify(userRepository, Mockito.times(0)).delete(USER_ID);
	}

	@Test
	@WithMockKeycloakUser(id = 2L, authorities = { "ROLE_ADMIN" })
	public void deleteByIdTest() throws EntityNotFoundException, ForbiddenException {
		userService.deleteById(USER_ID);
		Mockito.verify(userRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test(expected = AccessDeniedException.class)
	@WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_ADMIN" })
	public void deleteByIdByUserWithSameIdTest() throws EntityNotFoundException, ForbiddenException {
		userService.deleteById(USER_ID);
		Mockito.verify(userRepository, Mockito.times(1)).delete(Mockito.anyLong());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void findAllTest() {
		final List<User> users = userService.findAll();
		Assert.assertNotNull(users);
		Assert.assertTrue(users.size() == 1);

		Mockito.verify(userRepository, Mockito.times(1)).findAll();
	}

	@Test
	@WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
	public void findByIdTest() {
		final User user = userService.findById(USER_ID);
		Assert.assertNotNull(user);
		Assert.assertTrue(ModelsUtil.USER_FIRSTNAME.equals(user.getFirstName()));

		Mockito.verify(userRepository, Mockito.times(1)).findOne(Mockito.anyLong());
	}

	@Test
	@WithMockUser(authorities = { "ROLE_USER" })
	public void findByIdsTest() {
		final List<IdName> users = userService.findByIds(Arrays.asList(USER_ID));
		Assert.assertNotNull(users);
		Assert.assertTrue(USER_USERNAME.equals(users.get(0).getName()));

		Mockito.verify(userRepository, Mockito.times(1)).findByIdIn(Arrays.asList(USER_ID));
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void getUsersToReceiveFirstExpirationNotificationTest() {
		final List<User> users = userService.getUsersToReceiveFirstExpirationNotification();
		Assert.assertNotNull(users);
		Assert.assertTrue(users.size() == 1);
		Assert.assertTrue(ModelsUtil.USER_FIRSTNAME.equals(users.get(0).getFirstName()));

		Mockito.verify(userRepository, Mockito.times(1))
				.findByExpirationDateLessThanAndFirstExpirationNotificationSentFalse(Mockito.any(LocalDate.class));
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void getUsersToReceiveSecondExpirationNotificationTest() {
		final List<User> users = userService.getUsersToReceiveSecondExpirationNotification();
		Assert.assertNotNull(users);
		Assert.assertTrue(users.size() == 1);
		Assert.assertTrue(ModelsUtil.USER_FIRSTNAME.equals(users.get(0).getFirstName()));

		Mockito.verify(userRepository, Mockito.times(1))
				.findByExpirationDateLessThanAndSecondExpirationNotificationSentFalse(Mockito.any(LocalDate.class));
	}

	@Test
	public void requestExtensionTest() throws EntityNotFoundException {
		ExtensionRequestInfo requestInfo = new ExtensionRequestInfo();
		requestInfo.setExtensionDate(LocalDate.now());
		requestInfo.setExtensionMotivation("motivation");
		userService.requestExtension(USER_ID, requestInfo);

		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
		Mockito.verify(emailService, Mockito.times(1)).notifyAdminAccountExtensionRequest(Mockito.any(User.class));
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void saveTest() throws SecurityException  {
		User newUser = createUser();
		newUser.setId(null);
		userService.create(newUser);
		Mockito.verify(userRepository, Mockito.times(2)).save(Mockito.any(User.class));
		Mockito.verify(accountRequestInfoRepository, Mockito.times(0)).save(Mockito.any(AccountRequestInfo.class));
	}

	@Test
	@WithAnonymousUser
	public void saveWithAccountRequestTest() throws SecurityException {
		final User user = createUser();
		final AccountRequestInfo accountRequestInfo = new AccountRequestInfo();
		accountRequestInfo.setContact("contact");
		accountRequestInfo.setFunction("function");
		accountRequestInfo.setInstitution("institution");
		accountRequestInfo.setService("service");
		accountRequestInfo.setStudy("study");
		accountRequestInfo.setWork("work");
		user.setAccountRequestDemand(true);
		user.setAccountRequestInfo(accountRequestInfo);
		user.setId(null);
		user.setRole(null);
		userService.createAccountRequest(user);

		Mockito.verify(accountRequestInfoRepository, Mockito.times(1)).save(accountRequestInfo);
		Mockito.verify(userRepository, Mockito.times(2)).save(Mockito.any(User.class));
	}

	@Test
	@WithMockKeycloakUser(id = USER_ID, authorities = { "ROLE_USER" })
	public void updateTest() throws EntityNotFoundException {
		final User updatedUser = userService.update(createUser());
		Assert.assertNotNull(updatedUser);
		Assert.assertTrue(UPDATED_USER_FIRSTNAME.equals(updatedUser.getFirstName()));

		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void updateExpirationNotificationTrue() throws ShanoirUsersException {
		userService.updateExpirationNotification(createUser(), true);

		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
	}

	@Test
	@WithMockUser(authorities = { "ROLE_ADMIN" })
	public void updateExpirationNotificationFalse() throws ShanoirUsersException {
		userService.updateExpirationNotification(createUser(), false);

		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
	}

	private User createUser() {
		final User user = new User();
		user.setId(USER_ID);
		user.setFirstName(UPDATED_USER_FIRSTNAME);
		user.setUsername(USER_USERNAME);
		return user;
	}

}
