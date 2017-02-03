package org.shanoir.ng.configuration.security.jwt.extractor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * JWT token extractor.
 * 
 * @author msimon
 *
 */
//@Component
public class JwtHeaderTokenExtractor implements TokenExtractor {

    public static final String HEADER_PREFIX = "Bearer ";
    
	@Override
	public String extract(final String header) throws AuthenticationException {
        if (StringUtils.isBlank(header)) {
            throw new AuthenticationServiceException("Authorization header cannot be blank!");
        }

        if (header.length() < HEADER_PREFIX.length()) {
            throw new AuthenticationServiceException("Invalid authorization header size.");
        }

        return header.substring(HEADER_PREFIX.length(), header.length());
	}

}
