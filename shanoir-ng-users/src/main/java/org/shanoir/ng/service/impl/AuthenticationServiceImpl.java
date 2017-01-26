package org.shanoir.ng.service.impl;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.shanoir.ng.dto.LoginDTO;
import org.shanoir.ng.exception.ShanoirUsersException;
import org.shanoir.ng.exception.error.ErrorModelCode;
import org.shanoir.ng.model.User;
import org.shanoir.ng.model.auth.UserContext;
import org.shanoir.ng.repository.UserRepository;
import org.shanoir.ng.service.AuthenticationService;
import org.shanoir.ng.utils.PasswordUtils;
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
    private UserRepository userRepository;

	@Override
	public UserContext authenticate(final LoginDTO loginDTO) throws ShanoirUsersException {
		// SHA-1 digest used by Shanoir (same code than Shanoir)
		// TODO: replace it with SHA-256 digest : DigestUtils.sha256(loginDTO.getPassword())
		final String hashedPassword = PasswordUtils.getHash(loginDTO.getPassword());
		
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
		try {
			if (loginDTO.getLogin().contains("@")) {
				securityUser = (User) userRepository.findByEmail(loginDTO.getLogin()).get();
			} else {
				securityUser = (User) userRepository.findByUsername(loginDTO.getLogin()).get();
			}
		} catch (NoSuchElementException e) {
        	LOG.error("No user found with username/email " + loginDTO.getLogin());
        	throw new ShanoirUsersException(ErrorModelCode.USER_NOT_FOUND);
		}
        
        if (securityUser.isAccountRequestDemand()) {
        	LOG.error("Connection forbidden for user " + securityUser.getUsername() + " (account request not validated)");
        	throw new ShanoirUsersException(ErrorModelCode.ACCOUNT_REQUEST_NOT_VALIDATED);
        }

        if (securityUser.getExpirationDate() != null && securityUser.getExpirationDate().before(new Date())) {
        	LOG.error("Connection forbidden for user " + securityUser.getUsername() + " (date expired)");
        	throw new ShanoirUsersException(ErrorModelCode.DATE_EXPIRED);
        }

        // Update last login
        securityUser.setLastLogin(new Date());
        userRepository.save(securityUser);

        // Create user DTO
        final UserContext userContext = new UserContext();
		userContext.setId(securityUser.getId());
        userContext.setUsername(securityUser.getUsername());
        userContext.setAuthorities((List<GrantedAuthority>) securityUser.getAuthorities());

        return userContext;
	}

}
