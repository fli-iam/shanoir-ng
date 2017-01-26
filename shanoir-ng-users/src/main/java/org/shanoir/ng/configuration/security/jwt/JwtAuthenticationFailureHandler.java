package org.shanoir.ng.configuration.security.jwt;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.shanoir.ng.exception.JwtExpiredTokenException;
import org.shanoir.ng.exception.error.ErrorModel;
import org.shanoir.ng.exception.error.ErrorModelCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Authentication failure handler.
 * 
 * @author msimon
 *
 */
@Component
public class JwtAuthenticationFailureHandler implements AuthenticationFailureHandler {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public void onAuthenticationFailure(final HttpServletRequest request, final HttpServletResponse response,
			final AuthenticationException exception) throws IOException, ServletException {
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		if (exception instanceof BadCredentialsException) {
			mapper.writeValue(response.getWriter(), new ErrorModel(HttpStatus.UNAUTHORIZED.value(),
					"" + ErrorModelCode.BAD_CREDENTIALS, "Invalid username or password"));
		} else if (exception instanceof JwtExpiredTokenException) {
			mapper.writeValue(response.getWriter(), new ErrorModel(HttpStatus.UNAUTHORIZED.value(),
					"" + ErrorModelCode.JWT_TOKEN_EXPIRED, "Token has expired"));
		}

		mapper.writeValue(response.getWriter(), new ErrorModel(HttpStatus.UNAUTHORIZED.value(),
				"" + ErrorModelCode.AUTHENTICATION_FAILED, "Authentication failed"));
	}

}
