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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.shanoir.ng.role.repository.RoleRepository;
import org.shanoir.ng.shared.exception.SecurityException;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Unit tests for {@link KeycloakClient}.
 *
 * @author afragkiadakis
 */
@ExtendWith(MockitoExtension.class)
public class KeycloakClientTest {

    private static final String ADMIN_REALM = "master";

    private static final String USER_REALM = "shanoir-ng";

    private static final int PAGE_SIZE = 100;

    @Mock
    private RoleRepository roleRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private HttpServer server;

    private KeycloakClient keycloakClient;

    // Configurable behaviour of the fake Keycloak "list users" endpoint, set per-test.
    private List<Map<String, Object>> pageOneUsers = List.of();
    private List<Map<String, Object>> pageTwoUsers = List.of();
    private boolean usersEndpointFails = false;

    @BeforeEach
    public void setup() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/realms/" + ADMIN_REALM + "/protocol/openid-connect/token", this::handleToken);
        server.createContext("/admin/realms/" + USER_REALM + "/users", this::handleListUsers);
        server.start();

        String serverUrl = "http://localhost:" + server.getAddress().getPort();
        keycloakClient = new KeycloakClient(roleRepository, serverUrl);
        ReflectionTestUtils.setField(keycloakClient, "adminRealm", ADMIN_REALM);
        ReflectionTestUtils.setField(keycloakClient, "clientId", "admin-cli");
        ReflectionTestUtils.setField(keycloakClient, "username", "admin");
        ReflectionTestUtils.setField(keycloakClient, "password", "admin");
        ReflectionTestUtils.setField(keycloakClient, "userRealm", USER_REALM);
    }

    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    public void getUsersEnabledStatusTest() throws SecurityException {
        pageOneUsers = List.of(
                userRepresentation("enabled-id", Boolean.TRUE),
                userRepresentation("disabled-id", Boolean.FALSE),
                userRepresentation("undefined-id", null));
        pageTwoUsers = List.of();

        final Map<String, Boolean> enabledByKeycloakId = keycloakClient.getUsersEnabledStatus();

        assertEquals(3, enabledByKeycloakId.size());
        assertEquals(Boolean.TRUE, enabledByKeycloakId.get("enabled-id"));
        assertEquals(Boolean.FALSE, enabledByKeycloakId.get("disabled-id"));
        assertEquals(Boolean.FALSE, enabledByKeycloakId.get("undefined-id"));
    }

    @Test
    public void getUsersEnabledStatusPaginationTest() throws SecurityException {
        final List<Map<String, Object>> firstPage = new ArrayList<>();
        for (int i = 0; i < PAGE_SIZE; i++) {
            firstPage.add(userRepresentation("first-page-id-" + i, Boolean.TRUE));
        }
        pageOneUsers = firstPage;
        pageTwoUsers = List.of(userRepresentation("second-page-id", Boolean.FALSE));

        final Map<String, Boolean> enabledByKeycloakId = keycloakClient.getUsersEnabledStatus();

        assertEquals(PAGE_SIZE + 1, enabledByKeycloakId.size());
        assertEquals(Boolean.TRUE, enabledByKeycloakId.get("first-page-id-0"));
        assertEquals(Boolean.FALSE, enabledByKeycloakId.get("second-page-id"));
    }

    @Test
    public void getUsersEnabledStatusKeycloakErrorTest() {
        usersEndpointFails = true;
        assertThrows(SecurityException.class, () -> keycloakClient.getUsersEnabledStatus());
    }

    private Map<String, Object> userRepresentation(final String keycloakId, final Boolean enabled) {
        final Map<String, Object> userRepresentation = new HashMap<>();
        userRepresentation.put("id", keycloakId);
        userRepresentation.put("enabled", enabled);
        return userRepresentation;
    }

    private void handleToken(HttpExchange exchange) throws IOException {
        writeJson(exchange, 200, Map.of("access_token", "test-token"));
    }

    private void handleListUsers(HttpExchange exchange) throws IOException {
        if (usersEndpointFails) {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
            return;
        }
        int first = extractIntQueryParam(exchange.getRequestURI().getQuery(), "first");
        List<Map<String, Object>> page = (first == 0) ? pageOneUsers : pageTwoUsers;
        writeJson(exchange, 200, page);
    }

    private int extractIntQueryParam(String query, String name) {
        if (query == null) {
            return -1;
        }
        for (String part : query.split("&")) {
            String[] kv = part.split("=");
            if (kv.length == 2 && kv[0].equals(name)) {
                return Integer.parseInt(kv[1]);
            }
        }
        return -1;
    }

    private void writeJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = objectMapper.writeValueAsBytes(body);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

}
