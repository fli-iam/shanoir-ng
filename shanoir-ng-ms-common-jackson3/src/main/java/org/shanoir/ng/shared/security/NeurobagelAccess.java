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

package org.shanoir.ng.shared.security;

import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * Access rules for the token of the Neurobagel export job (Keycloak client 'neurobagel-export').
 *
 * That token has no user behind it and bypasses the per-study rights model, so it is confined to the
 * Neurobagel export endpoints: every service refuses it on any other authenticated endpoint, including
 * endpoints that forgot their @PreAuthorize.
 */
public final class NeurobagelAccess {

    /** Realm role held by the 'neurobagel-export' service account, and by nothing else. */
    public static final String ROLE = "ROLE_NEUROBAGEL";

    /** The only paths a Neurobagel token may call. */
    public static final String PATHS = "/neurobagel/**";

    private NeurobagelAccess() { }

    /**
     * Replaces anyRequest().authenticated(): any authenticated caller except the Neurobagel export job.
     */
    public static AuthorizationManager<RequestAuthorizationContext> authenticatedExceptNeurobagel() {
        return AuthorizationManagers.allOf(
                AuthenticatedAuthorizationManager.authenticated(),
                AuthorizationManagers.not(AuthorityAuthorizationManager.hasAuthority(ROLE)));
    }

}
