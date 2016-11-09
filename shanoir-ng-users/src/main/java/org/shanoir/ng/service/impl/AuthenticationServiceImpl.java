package org.shanoir.ng.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.shanoir.ng.configuration.security.xauth.TokenUtils;
import org.shanoir.ng.dto.LoginDTO;
import org.shanoir.ng.dto.UserDTO;
import org.shanoir.ng.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Authentication service implementation.
 * 
 * @author msimon
 *
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
	
	@Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;
    
    private final TokenUtils tokenUtils = new TokenUtils();

	@Override
	public UserDTO authenticate(final LoginDTO loginDTO, final HttpServletResponse response) throws Exception {
		final UsernamePasswordAuthenticationToken authenticationToken = 
				new UsernamePasswordAuthenticationToken(loginDTO.getLogin(), loginDTO.getPassword());
        Authentication authentication = null;
        try {
        	authentication = authenticationManager.authenticate(authenticationToken);
        } catch (AuthenticationException e) {
        	LOG.error("Error while authenticating", e);
        	throw e;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //Retrieve security user after authentication
        final User securityUser = (User) userDetailsService.loadUserByUsername(loginDTO.getLogin());

        //Parse Granted authorities to a list of string authorities
        final List<String> authorities = new ArrayList<>();
        for(GrantedAuthority authority : securityUser.getAuthorities()){
            authorities.add(authority.getAuthority());
        }

        // Create user DTO
        final UserDTO userDTO = new UserDTO();
        userDTO.setId(1);
        userDTO.setLogin(securityUser.getUsername());
        userDTO.setAuthorities(authorities);
        userDTO.setToken(tokenUtils.createToken(securityUser));
        
        return userDTO;
	}

    @Override
    public void logout() {
    	SecurityContextHolder.clearContext();
    }

}
