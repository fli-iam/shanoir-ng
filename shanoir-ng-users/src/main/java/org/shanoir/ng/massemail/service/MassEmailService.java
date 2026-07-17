/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.massemail.service;

import java.util.List;

import org.shanoir.ng.massemail.model.RecipientGroup;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.user.model.User;
import org.springframework.stereotype.Service;

/**
 * Resolution of the users targeted by an administrator mass email.
 *
 * @author afragkiadakis
 */
@Service
public interface MassEmailService {

    /**
     * Resolve the users belonging to the given recipient group. Users without
     * an email address and users whose account request was never approved are
     * excluded from every group.
     *
     * @param recipientGroup
     *            the group of users to resolve.
     * @return the users to email.
     * @throws SecurityException
     *             if the Keycloak enabled status could not be read.
     */
    List<User> resolveRecipients(RecipientGroup recipientGroup) throws SecurityException;

    /**
     * Count the users belonging to the given recipient group.
     *
     * @param recipientGroup
     *            the group of users to count.
     * @return the number of users that would receive the email.
     * @throws SecurityException
     *             if the Keycloak enabled status could not be read.
     */
    int countRecipients(RecipientGroup recipientGroup) throws SecurityException;

}
