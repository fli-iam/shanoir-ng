package org.shanoir.ng.controller.rest;

import java.util.Arrays;

import org.shanoir.ng.configuration.security.jwt.token.AccessJwtToken;
import org.shanoir.ng.configuration.security.jwt.token.JwtTokenFactory;
import org.shanoir.ng.model.auth.UserContext;
import org.shanoir.ng.utils.ModelsUtil;
import org.springframework.http.HttpHeaders;

/**
 * Utility class for API controller tests.
 * 
 * @author msimon
 *
 */
public final class ApiControllerTestUtil {

	/**
	 * Generate headers with JWT token for administrator.
	 * 
	 * @param tokenFactory
	 *            Token factory bean.
	 * @return HTTP headers.
	 */
	public static HttpHeaders generateHeadersWithTokenForAdmin(final JwtTokenFactory tokenFactory) {
		final HttpHeaders headers = new HttpHeaders();
		final UserContext userContext = new UserContext();
		userContext.setId(ModelsUtil.USER_ID);
		userContext.setUsername(ModelsUtil.USER_LOGIN);
		userContext.setAuthorities(Arrays.asList(ModelsUtil.createAdminRole()));
		AccessJwtToken token = tokenFactory.createAccessJwtToken(userContext);
		headers.set("X-Authorization", "Bearer " + token.getToken());

		return headers;
	}

	/**
	 * Generate headers with JWT token for guest.
	 * 
	 * @param tokenFactory
	 *            Token factory bean.
	 * @return HTTP headers.
	 */
	public static HttpHeaders generateHeadersWithTokenForGuest(final JwtTokenFactory tokenFactory) {
		final HttpHeaders headers = new HttpHeaders();
		final UserContext userContext = new UserContext();
		userContext.setUsername(ModelsUtil.USER_LOGIN_GUEST);
		userContext.setAuthorities(Arrays.asList(ModelsUtil.createGuestRole()));
		AccessJwtToken token = tokenFactory.createAccessJwtToken(userContext);
		headers.set("X-Authorization", "Bearer " + token.getToken());

		return headers;
	}

}
