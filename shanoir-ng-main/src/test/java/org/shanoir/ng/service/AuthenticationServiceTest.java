package org.shanoir.ng.service;

import java.util.ArrayList;
import java.util.List;

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
import org.shanoir.ng.service.impl.AuthenticationServiceImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;


/**
 * Authentication service test
 * Created by Michael DESIGAUD on 15/02/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletResponse httpResponse;

    private User getSecurityUser(String login, String password){
    	final List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return new User(login, password, authorities);
    }

    @Test
    public void authenticate() throws Exception {

    	final LoginDTO loginDTO = new LoginDTO();
        loginDTO.setLogin("user");
        loginDTO.setPassword("password");

        final User securityUser = getSecurityUser(loginDTO.getLogin(),loginDTO.getPassword());

        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginDTO.getLogin(),loginDTO.getPassword());

        Mockito.when(userDetailsService.loadUserByUsername(loginDTO.getLogin())).thenReturn(securityUser);
        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class))).thenReturn(token);

        final UserDTO userDTO = authenticationService.authenticate(loginDTO, httpResponse);
        Assert.assertNotNull(userDTO);
        Assert.assertEquals(userDTO.getLogin(),loginDTO.getLogin());
        Assert.assertNotNull(userDTO.getAuthorities());
        Assert.assertTrue(!userDTO.getAuthorities().isEmpty());

        Mockito.verify(authenticationManager,Mockito.times(1)).authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class));
        Mockito.verify(userDetailsService,Mockito.times(1)).loadUserByUsername(loginDTO.getLogin());
    }

}
