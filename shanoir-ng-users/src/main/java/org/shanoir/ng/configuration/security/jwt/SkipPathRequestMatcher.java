package org.shanoir.ng.configuration.security.jwt;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import io.jsonwebtoken.lang.Assert;

/**
 * Request matcher used to skip some match.
 * 
 * @author msimon
 *
 */
public class SkipPathRequestMatcher implements RequestMatcher {

	private OrRequestMatcher matchers;
	private RequestMatcher processingMatcher;

	/**
	 * Constructor.
	 * 
	 * @param pathsToSkip
	 *            paths to skip.
	 * @param processingPath
	 *            processing path.
	 */
	public SkipPathRequestMatcher(final List<String> pathsToSkip, final String processingPath) {
		Assert.notNull(pathsToSkip);
		final List<RequestMatcher> m = pathsToSkip.stream().map(path -> new AntPathRequestMatcher(path))
				.collect(Collectors.toList());
		matchers = new OrRequestMatcher(m);
		processingMatcher = new AntPathRequestMatcher(processingPath);
	}

	@Override
	public boolean matches(final HttpServletRequest request) {
		if (matchers.matches(request)) {
			return false;
		}
		return processingMatcher.matches(request) ? true : false;
	}

}
