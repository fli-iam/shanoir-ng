package org.shanoir.ng.configuration.security.jwt;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.shanoir.ng.exception.ShanoirAuthenticationException;
import org.shanoir.ng.exception.error.ErrorModelCode;
import org.shanoir.ng.model.User;
import org.shanoir.ng.model.auth.UserContext;
import org.shanoir.ng.service.UserService;
import org.shanoir.ng.utils.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.lang.Assert;

/**
 * Shanoir authentication provider (username/password).
 * 
 * @author msimon
 *
 */
@Component
public class ShanoirAuthenticationProvider implements AuthenticationProvider {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ShanoirAuthenticationProvider.class);

	@Autowired
	private UserService userService;

	@Override
	public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
		Assert.notNull(authentication, "No authentication data provided");

		final String username = (String) authentication.getPrincipal();
		final String password = (String) authentication.getCredentials();

		// Retrieve security user after authentication
		User securityUser = null;
		try {
			if (username.contains("@")) {
				securityUser = (User) userService.findByEmail(username).get();
			} else {
				securityUser = (User) userService.findByUsername(username).get();
			}
		} catch (NoSuchElementException e) {
			LOG.error("No user found with username/email " + username);
			throw new UsernameNotFoundException("No user found with username/email " + username);
		}
		if (!passwordMatch(password, securityUser.getPassword())) {
			throw new BadCredentialsException("Authentication Failed. Username or Password not valid.");
		}

		if (securityUser.isAccountRequestDemand()) {
			LOG.error(
					"Connection forbidden for user " + securityUser.getUsername() + " (account request not validated)");
			throw new ShanoirAuthenticationException(
					"Connection forbidden for user " + securityUser.getUsername() + " (account request not validated)",
					ErrorModelCode.ACCOUNT_REQUEST_NOT_VALIDATED);
		}

		if (securityUser.getExpirationDate() != null && securityUser.getExpirationDate().before(new Date())) {
			LOG.error("Connection forbidden for user " + securityUser.getUsername() + " (date expired)");
			throw new ShanoirAuthenticationException(
					"Connection forbidden for user " + securityUser.getUsername() + " (date expired)",
					ErrorModelCode.DATE_EXPIRED);
		}

		// Update last login
		userService.updateLastLogin(securityUser);

		// Create user context
		final UserContext userContext = new UserContext();
		userContext.setId(securityUser.getId());
		userContext.setUsername(securityUser.getUsername());
		userContext.setAuthorities((List<GrantedAuthority>) securityUser.getAuthorities());

		return new UsernamePasswordAuthenticationToken(userContext, null, userContext.getAuthorities());
	}

	@Override
	public boolean supports(final Class<?> authentication) {
		return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
	}

	private boolean passwordMatch(final String requestPassword, final String dbPassword) {
		return PasswordUtils.getHash(requestPassword).equals(dbPassword);
	}

}
