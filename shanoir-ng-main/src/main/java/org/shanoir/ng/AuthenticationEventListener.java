package org.shanoir.ng;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventListener implements ApplicationListener<AbstractAuthenticationEvent> {

	static final Logger LOG = LoggerFactory.getLogger(AuthenticationEventListener.class);

	@Override
	public void onApplicationEvent(AbstractAuthenticationEvent authenticationEvent) {
		if (authenticationEvent instanceof InteractiveAuthenticationSuccessEvent) {
			// ignores to prevent duplicate logging with
			// AuthenticationSuccessEvent
			return;
		}
		Authentication authentication = authenticationEvent.getAuthentication();
		String auditMessage = "Login attempt with username: " + authentication.getName() + "\t\tSuccess: "
				+ authentication.isAuthenticated();
		LOG.info(auditMessage);
	}

}