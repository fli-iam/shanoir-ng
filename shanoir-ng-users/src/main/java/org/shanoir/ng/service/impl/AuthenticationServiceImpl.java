package org.shanoir.ng.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.shanoir.ng.configuration.security.xauth.TokenUtils;
import org.shanoir.ng.dto.LoginDTO;
import org.shanoir.ng.dto.UserDTO;
import org.shanoir.ng.exception.ShanoirUsersException;
import org.shanoir.ng.exception.error.ErrorModelCode;
import org.shanoir.ng.model.User;
import org.shanoir.ng.service.AuthenticationService;
import org.shanoir.ng.service.UserService;
import org.shanoir.ng.utils.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private UserService userService;

    private final TokenUtils tokenUtils = new TokenUtils();

	@Override
	public UserDTO authenticate(final LoginDTO loginDTO, final HttpServletResponse response) throws ShanoirUsersException {
		// SHA-1 digest used by Shanoir (same code than Shanoir)
		// TODO: replace it with SHA-256 digest : DigestUtils.sha256(loginDTO.getPassword())
		final String hashedPassword = HashUtil.getHash(loginDTO.getPassword());
		
		final UsernamePasswordAuthenticationToken authenticationToken =
				new UsernamePasswordAuthenticationToken(loginDTO.getLogin(), hashedPassword);
        Authentication authentication = null;
        try {
        	authentication = authenticationManager.authenticate(authenticationToken);
        } catch (AuthenticationException e) {
        	LOG.error("Error while authenticating", e);
        	throw new ShanoirUsersException(ErrorModelCode.BAD_CREDENTIALS);
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Retrieve security user after authentication
        User securityUser = null;
        if (loginDTO.getLogin().contains("@")) {
        	securityUser = (User) userService.findByEmail(loginDTO.getLogin());
        } else {
        	securityUser = (User) userService.findByUsername(loginDTO.getLogin());
        }
        if (securityUser == null) {
        	LOG.error("No user found with username/email " + loginDTO.getLogin());
        	throw new ShanoirUsersException(ErrorModelCode.USER_NOT_FOUND);
        }
        if (securityUser.getExpirationDate() != null && securityUser.getExpirationDate().before(new Date())) {
        	LOG.error("Connection forbidden for user " + securityUser.getUsername() + " (date expired)");
        	throw new ShanoirUsersException(ErrorModelCode.DATE_EXPIRED);
        }

        // Update last login
        securityUser.setLastLogin(new Date());
        userService.save(securityUser);

        // Parse Granted authorities to a list of string authorities
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
