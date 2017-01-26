package org.shanoir.ng.configuration.security.jwt.extractor;

import org.springframework.security.core.AuthenticationException;

/**
 * Token extractor interface.
 * 
 * @author msimon
 *
 */
public interface TokenExtractor {

	/**
	 * Extract a token from a payload.
	 * 
	 * @param payload
	 *            payload.
	 * @return token token.
	 * @throws AuthenticationException
	 *             authentication exception.
	 */
	String extract(String payload) throws AuthenticationException;

}
