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

package org.shanoir.ng.user.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.role.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link KeycloakClient}.
 *
 * @author afragkiadakis
 */
@ExtendWith(MockitoExtension.class)
public class KeycloakClientTest {

    private static final String REALM = "shanoir-ng";

    private static final int PAGE_SIZE = 100;

    @Value("${kc.admin.client.server.url}")
    private String serverUrl;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    private KeycloakClient keycloakClient;

    @BeforeEach
    public void setup() {
        keycloakClient = new KeycloakClient(roleRepository, serverUrl);
        ReflectionTestUtils.setField(keycloakClient, "keycloakRealm", REALM);
        given(realmResource.users()).willReturn(usersResource);
    }

    @Test
    public void getUsersEnabledStatusTest() throws SecurityException {
        final List<UserRepresentation> page = List.of(
                userRepresentation("enabled-id", Boolean.TRUE),
                userRepresentation("disabled-id", Boolean.FALSE),
                userRepresentation("undefined-id", null));
        given(usersResource.list(0, PAGE_SIZE)).willReturn(page);

        final Map<String, Boolean> enabledByKeycloakId = keycloakClient.getUsersEnabledStatus();

        assertEquals(3, enabledByKeycloakId.size());
        assertEquals(Boolean.TRUE, enabledByKeycloakId.get("enabled-id"));
        assertEquals(Boolean.FALSE, enabledByKeycloakId.get("disabled-id"));
        assertEquals(Boolean.FALSE, enabledByKeycloakId.get("undefined-id"));
    }

    @Test
    public void getUsersEnabledStatusPaginationTest() throws SecurityException {
        final List<UserRepresentation> firstPage = new ArrayList<>();
        for (int i = 0; i < PAGE_SIZE; i++) {
            firstPage.add(userRepresentation("first-page-id-" + i, Boolean.TRUE));
        }
        final List<UserRepresentation> secondPage = List.of(userRepresentation("second-page-id", Boolean.FALSE));
        given(usersResource.list(0, PAGE_SIZE)).willReturn(firstPage);
        given(usersResource.list(PAGE_SIZE, PAGE_SIZE)).willReturn(secondPage);

        final Map<String, Boolean> enabledByKeycloakId = keycloakClient.getUsersEnabledStatus();

        assertEquals(PAGE_SIZE + 1, enabledByKeycloakId.size());
        assertEquals(Boolean.TRUE, enabledByKeycloakId.get("first-page-id-0"));
        assertEquals(Boolean.FALSE, enabledByKeycloakId.get("second-page-id"));
        Mockito.verify(usersResource, Mockito.times(2)).list(Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void getUsersEnabledStatusKeycloakErrorTest() {
        given(usersResource.list(0, PAGE_SIZE)).willThrow(new RuntimeException("keycloak unreachable"));
        assertThrows(SecurityException.class, () -> keycloakClient.getUsersEnabledStatus());
    }

    private UserRepresentation userRepresentation(final String keycloakId, final Boolean enabled) {
        final UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(keycloakId);
        userRepresentation.setEnabled(enabled);
        return userRepresentation;
    }

}
