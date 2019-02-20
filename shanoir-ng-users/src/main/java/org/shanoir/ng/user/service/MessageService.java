package org.shanoir.ng.user.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author Rob Winch
 */
@Service
@PreAuthorize("hasAnyRole('ADMIN')")
public interface MessageService {
	String getMessage();
}
