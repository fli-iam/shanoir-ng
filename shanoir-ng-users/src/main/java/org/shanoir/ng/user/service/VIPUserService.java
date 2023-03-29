package org.shanoir.ng.user.service;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.PasswordPolicyException;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.user.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * @author Alae ES-SAKI
 */
@Service
public interface VIPUserService {

    /**
     * Create a new VIP account request.
     *
     * @param user the user to create.
     * @return the created user
     */
    User createVIPAccountRequest(User user) throws SecurityException, MicroServiceCommunicationException, PasswordPolicyException;
}
