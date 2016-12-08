package org.shanoir.ng.service;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.shanoir.ng.dto.LoginDTO;
import org.shanoir.ng.dto.UserDTO;
import org.shanoir.ng.model.User;
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
    private UserService userService;

    @Mock
    private HttpServletResponse httpResponse;

    @Test
    public void authenticateTest() throws Exception {
    	final User securityUser = ModelsUtil.createUser();
    	
    	final LoginDTO loginDTO = new LoginDTO();
        loginDTO.setLogin(securityUser.getUsername());
        loginDTO.setPassword(securityUser.getPassword());

        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginDTO.getLogin(),loginDTO.getPassword());

        Mockito.when(userService.findByUsername(loginDTO.getLogin())).thenReturn(securityUser);
        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenReturn(token);

        final UserDTO userDTO = authenticationService.authenticate(loginDTO, httpResponse);
        Assert.assertNotNull(userDTO);
        Assert.assertEquals(userDTO.getLogin(),loginDTO.getLogin());
        Assert.assertNotNull(userDTO.getAuthorities());
        Assert.assertTrue(!userDTO.getAuthorities().isEmpty());

        Mockito.verify(authenticationManager,Mockito.times(1)).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
        Mockito.verify(userService,Mockito.times(1)).findByUsername(loginDTO.getLogin());
    }

}
