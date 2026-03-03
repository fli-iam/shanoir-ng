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

import java.time.Duration;
import java.util.*;

import org.shanoir.ng.role.model.Role;
import org.shanoir.ng.role.repository.RoleRepository;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.utils.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class KeycloakClient {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakClient.class);

    @Value("${kc.admin.client.server.url}")
    private String serverUrl;

    @Value("${kc.admin.client.realm}")
    private String adminRealm;

    @Value("${kc.admin.client.client.id}")
    private String clientId;

    @Value("${SHANOIR_KEYCLOAK_USER}")
    private String username;

    @Value("${SHANOIR_KEYCLOAK_PASSWORD}")
    private String password;

    @Value("${kc.admin.client.realm.users}")
    private String userRealm;

    private final RoleRepository roleRepository;

    private final WebClient webClient;

    public KeycloakClient(RoleRepository roleRepository,
            @Value("${kc.admin.client.server.url}") String serverUrl) {
        this.roleRepository = roleRepository;
        this.webClient = WebClient.builder()
                .baseUrl(serverUrl)
                .build();
    }

    private String getAdminToken() throws SecurityException {
        Map<String, String> form = new HashMap<>();
        form.put("grant_type", "password");
        form.put("client_id", clientId);
        form.put("username", username);
        form.put("password", password);
        Map<?, ?> response = webClient.post()
                .uri("/realms/" + adminRealm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(
                        "grant_type", "password")
                        .with("client_id", clientId)
                        .with("username", username)
                        .with("password", password))
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));
        if (response == null || !response.containsKey("access_token")) {
            throw new SecurityException("Unable to retrieve admin token from Keycloak.");
        }
        return response.get("access_token").toString();
    }

    public String createUserWithPassword(User user, String rawPassword) throws SecurityException {
        try {
            String token = getAdminToken();
            Map<String, Object> body = buildUserPayload(user, rawPassword);
            var response = webClient.post()
                    .uri("/admin/realms/" + userRealm + "/users")
                    .headers(h -> h.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .exchangeToMono(clientResponse -> {
                        if (!clientResponse.statusCode().is2xxSuccessful()) {
                            return Mono.error(new SecurityException("Failed to create user in Keycloak."));
                        }
                        return Mono.just(clientResponse.headers().asHttpHeaders());
                    })
                    .block(Duration.ofSeconds(10));
            if (response == null) {
                throw new SecurityException("Failed to create user in Keycloak.");
            }
            String location = Objects.requireNonNull(response.getLocation()).getPath();
            String keycloakId = location.replaceAll(".*/([^/]+)$", "$1");
            assignRealmRole(keycloakId, user.getRole().getId(), token);
            return keycloakId;
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("Could not register the new user into Keycloak.", e);
        }
    }

    public String createUserWithPassword(User user) throws SecurityException {
        return createUserWithPassword(user, user.getPassword());
    }

    public void deleteUser(String keycloakId) {
        try {
            String token = getAdminToken();
            webClient.delete()
                    .uri("/admin/realms/" + userRealm + "/users/" + keycloakId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(10));
        } catch (Exception e) {
            LOG.error("Error deleting user {}", keycloakId, e);
        }
    }

    public void updateUser(User user) {
        try {
            String token = getAdminToken();
            webClient.put()
                    .uri("/admin/realms/" + userRealm + "/users/" + user.getKeycloakId())
                    .headers(h -> h.setBearerAuth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(buildUserPayload(user, null))
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(10));
            removeAllManagedRealmRoles(user.getKeycloakId(), token);
            assignRealmRole(user.getKeycloakId(), user.getRole().getId(), token);
        } catch (Exception e) {
            LOG.error("Error updating user {}", user.getKeycloakId(), e);
        }
    }

    public String resetPassword(String keycloakId) throws SecurityException {
        String newPassword = PasswordUtils.generatePassword();
        String token = getAdminToken();
        Map<String, Object> body = Map.of(
                "type", "password",
                "temporary", true,
                "value", newPassword);
        webClient.put()
                .uri("/admin/realms/" + userRealm + "/users/" + keycloakId + "/reset-password")
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(10));
        return newPassword;
    }

    private void assignRealmRole(String userId, Long roleId, String token) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NoSuchElementException("Role not found"));
        Map<?, ?> roleRepresentation = webClient.get()
                .uri("/admin/realms/" + userRealm + "/roles/" + role.getName())
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));
        webClient.post()
                .uri("/admin/realms/" + userRealm + "/users/" + userId + "/role-mappings/realm")
                .headers(h -> h.setBearerAuth(token))
                .bodyValue(List.of(roleRepresentation))
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(10));
    }

    private void removeAllManagedRealmRoles(String userId, String token) {
        List<String> managedRoleNames = roleRepository.getAllNames();
        List<Map<String, Object>> roles = webClient.get()
                .uri("/admin/realms/" + userRealm + "/users/" + userId + "/role-mappings/realm")
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(List.class)
                .block(Duration.ofSeconds(10));
        if (roles == null)
            return;
        List<Map<String, Object>> toRemove = new ArrayList<>();
        for (Map<String, Object> role : roles) {
            if (managedRoleNames.contains(role.get("name"))) {
                toRemove.add(role);
            }
        }
        if (!toRemove.isEmpty()) {
            webClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri("/admin/realms/" + userRealm + "/users/" + userId + "/role-mappings/realm")
                    .headers(h -> h.setBearerAuth(token))
                    .bodyValue(toRemove)
                    .retrieve()
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(10));
        }
    }

    private Map<String, Object> buildUserPayload(User user, String rawPassword) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", user.getUsername());
        payload.put("email", user.getEmail());
        payload.put("firstName", user.getFirstName());
        payload.put("lastName", user.getLastName());
        payload.put("enabled", user.isEnabled());
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("userId", List.of(user.getId().toString()));
        attributes.put("canImportFromPACS", Arrays.asList("" + user.isCanAccessToDicomAssociation()));
        if (user.getExpirationDate() != null) {
            attributes.put("expirationDate", Arrays.asList("" + user.getExpirationDate()));
        }
        payload.put("attributes", attributes);
        if (rawPassword != null) {
            payload.put("credentials", List.of(Map.of(
                    "type", "password",
                    "temporary", true,
                    "value", rawPassword)));
        }
        return payload;
    }

}
