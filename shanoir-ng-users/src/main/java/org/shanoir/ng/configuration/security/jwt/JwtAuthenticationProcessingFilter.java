package org.shanoir.ng.configuration.security.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.shanoir.ng.configuration.security.SecurityConfiguration;
import org.shanoir.ng.configuration.security.jwt.extractor.TokenExtractor;
import org.shanoir.ng.configuration.security.jwt.token.AccessJwtToken;
import org.shanoir.ng.configuration.security.jwt.token.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Filter to process JWT authentication.
 * 
 * @author msimon
 *
 */
public class JwtAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

	private final AuthenticationFailureHandler failureHandler;
	private final TokenExtractor tokenExtractor;

	public JwtAuthenticationProcessingFilter(final AuthenticationFailureHandler failureHandler,
			final TokenExtractor tokenExtractor, final RequestMatcher matcher) {
		super(matcher);
		this.failureHandler = failureHandler;
		this.tokenExtractor = tokenExtractor;
	}

	@Override
	public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		final String tokenPayload = request.getHeader(SecurityConfiguration.JWT_TOKEN_HEADER_PARAM);
		final AccessJwtToken token = new AccessJwtToken(tokenExtractor.extract(tokenPayload));
		return getAuthenticationManager().authenticate(new JwtAuthenticationToken(token));
	}

	@Override
	protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain chain, final Authentication authResult) throws IOException, ServletException {
		final SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authResult);
		SecurityContextHolder.setContext(context);
		chain.doFilter(request, response);
	}

	@Override
	protected void unsuccessfulAuthentication(final HttpServletRequest request, final HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		SecurityContextHolder.clearContext();
		failureHandler.onAuthenticationFailure(request, response, failed);
	}

}
