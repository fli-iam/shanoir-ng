package org.shanoir.ng.configuration.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * Logout success handler.
 * 
 * @author msimon
 *
 */
@Component
public class ShanoirLogoutSuccess implements LogoutSuccessHandler {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ShanoirLogoutSuccess.class);
	
	/* (non-Javadoc)
	 * @see org.springframework.security.web.authentication.logout.LogoutSuccessHandler#onLogoutSuccess(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.security.core.Authentication)
	 */
	@Override
	public void onLogoutSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication)
			throws IOException, ServletException {
	    if (authentication != null && authentication.getDetails() != null) {
	        try {
	        	request.getSession().invalidate();
	            // you can add more codes here when the user successfully logs
	            // out,
	            // such as updating the database for last active.
	        	
	        	SecurityContextHolder.clearContext();
	        } catch (Exception e) {
	        	LOG.error("Error while invalidating session", e);
	        }
	    }

	    response.setStatus(HttpServletResponse.SC_OK);
	}

}
