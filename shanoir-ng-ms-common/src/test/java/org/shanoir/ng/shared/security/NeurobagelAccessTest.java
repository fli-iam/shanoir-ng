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

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

class NeurobagelAccessTest {

    private static boolean granted(Authentication authentication) {
        return NeurobagelAccess.authenticatedExceptNeurobagel()
                .authorize(() -> authentication, (RequestAuthorizationContext) null)
                .isGranted();
    }

    private static Authentication user(String... roles) {
        return UsernamePasswordAuthenticationToken.authenticated("user", null, AuthorityUtils.createAuthorityList(roles));
    }

    @Test
    void anonymousIsRefused() {
        Assertions.assertFalse(granted(new AnonymousAuthenticationToken("key", "anonymous",
                List.copyOf(AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")))));
    }

    @Test
    void shanoirUsersAreAllowed() {
        Assertions.assertTrue(granted(user("ROLE_USER")));
        Assertions.assertTrue(granted(user("ROLE_EXPERT")));
        Assertions.assertTrue(granted(user("ROLE_ADMIN")));
    }

    @Test
    void neurobagelTokenIsRefused() {
        Assertions.assertFalse(granted(user(NeurobagelAccess.ROLE)));
    }

    @Test
    void neurobagelRoleWinsOverOtherRoles() {
        Assertions.assertFalse(granted(user("ROLE_ADMIN", NeurobagelAccess.ROLE)));
    }

}
