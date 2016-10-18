package org.shanoir.ng.configuration.security.xauth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

/**
 * @author msimon
 *
 */
public class XAuthTokenFilter extends GenericFilterBean {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(XAuthTokenFilter.class);
	
	private final static String XAUTH_TOKEN_HEADER_NAME = "x-auth-token";
	
    private final UserDetailsService detailsService;
    private final TokenUtils tokenUtils = new TokenUtils();

    /**
     * Constructor.
     * 
     * @param userDetailsService user details service.
     */
    public XAuthTokenFilter(final UserDetailsService userDetailsService) {
        this.detailsService = userDetailsService;
    }

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
        try {
        	final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        	final String authToken = httpServletRequest.getHeader(XAUTH_TOKEN_HEADER_NAME);

            if (StringUtils.hasText(authToken)) {
            	final String username = this.tokenUtils.getUserNameFromToken(authToken);

            	final UserDetails details = this.detailsService.loadUserByUsername(username);

                if (this.tokenUtils.validateToken(authToken, details)) {
                	final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(details, details.getPassword(), details.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(token);
                }
            }
            chain.doFilter(request, response);
        } catch (Exception e) {
        	LOG.error("Error while authenticating request", e);
            ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
	}

}
