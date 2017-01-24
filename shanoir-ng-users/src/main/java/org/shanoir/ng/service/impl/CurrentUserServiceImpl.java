package org.shanoir.ng.service.impl;

import org.shanoir.ng.model.auth.UserContext;
import org.shanoir.ng.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

	private static final Logger LOG = LoggerFactory.getLogger(CurrentUserServiceImpl.class);

	@Override
	public boolean canAccessUser(final Long userId) {
		if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken) {
			LOG.warn("anonymous user is connected, access refused to user " + userId);
			return false;
		}
		final UserContext currentUser = (UserContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (currentUser != null) {
			if (currentUser.getId().equals(userId)) {
				return true;
			}
			if (currentUser.getAuthorities() != null) {
				for (final GrantedAuthority authority : currentUser.getAuthorities()) {
					if ("adminRole".equals(authority.getAuthority())) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
