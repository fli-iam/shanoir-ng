package org.shanoir.ng.service;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.dto.LoginDTO;
import org.shanoir.ng.model.User;
import org.shanoir.ng.model.auth.UserContext;
import org.shanoir.ng.repository.UserRepository;
import org.shanoir.ng.service.impl.AuthenticationServiceImpl;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * Authentication service test.
 * 
 * @author msimon
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {

	@InjectMocks
	private AuthenticationServiceImpl authenticationService;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private UserRepository userRepository;

	@Test
	public void authenticateTest() throws Exception {
		final User securityUser = ModelsUtil.createUser();

		final LoginDTO loginDTO = new LoginDTO();
		loginDTO.setLogin(securityUser.getUsername());
		loginDTO.setPassword(securityUser.getPassword());

		final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginDTO.getLogin(),
				loginDTO.getPassword());

		Mockito.when(userRepository.findByUsername(loginDTO.getLogin())).thenReturn(Optional.of(securityUser));
		Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(token);

		final UserContext userContext = authenticationService.authenticate(loginDTO);
		Assert.assertNotNull(userContext);
		Assert.assertEquals(userContext.getUsername(), loginDTO.getLogin());
		Assert.assertNotNull(userContext.getAuthorities());
		Assert.assertTrue(!userContext.getAuthorities().isEmpty());

		Mockito.verify(authenticationManager, Mockito.times(1))
				.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
		Mockito.verify(userRepository, Mockito.times(1)).findByUsername(loginDTO.getLogin());
	}

}
