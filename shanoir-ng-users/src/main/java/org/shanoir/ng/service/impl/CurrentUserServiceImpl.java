package org.shanoir.ng.service.impl;

import org.shanoir.ng.model.User;
import org.shanoir.ng.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

	private static final Logger LOG = LoggerFactory.getLogger(CurrentUserServiceImpl.class);

	@Override
	public boolean canAccessUser(Long userId) {
		if (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken ) {
			LOG.warn("anonymous user is connected, access refused to user " + userId);
			return false;
		}
		User currentUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return currentUser != null && (currentUser.getRole().getName().equals("adminRole") || currentUser.getId().equals(userId));
	}

}
