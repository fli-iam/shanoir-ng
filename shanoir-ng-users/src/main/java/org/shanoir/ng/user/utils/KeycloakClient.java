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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

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

    /**
     * Keycloak required action that forces a user to register a TOTP authenticator at next login.
     */
    private static final String CONFIGURE_TOTP_ACTION = "CONFIGURE_TOTP";

    /**
     * Keycloak credential type stored once a user has registered an authenticator.
     */
    private static final String OTP_CREDENTIAL_TYPE = "otp";

    /**
     * Page size used when listing all realm users.
     */
    private static final int USERS_PAGE_SIZE = 100;

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

    /**
     * Enable Keycloak two-factor (TOTP) authentication for a user by adding the
     * {@code CONFIGURE_TOTP} required action. The user is then forced to register an
     * authenticator at next login.
     *
     * @param keycloakId
     *            the keycloak id of the user.
     * @throws SecurityException
     */
    public void enableTotp(final String keycloakId) throws SecurityException {
        try {
            final String token = getAdminToken();
            final Map<String, Object> userRepresentation = getUserRepresentation(keycloakId, token);
            final List<String> requiredActions = getRequiredActions(userRepresentation);
            if (!requiredActions.contains(CONFIGURE_TOTP_ACTION)) {
                requiredActions.add(CONFIGURE_TOTP_ACTION);
                userRepresentation.put("requiredActions", requiredActions);
                updateUserRepresentation(keycloakId, userRepresentation, token);
            }
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("Could not enable two-factor authentication for user with keycloak id " + keycloakId, e);
        }
    }


    /**
     * Disable Keycloak two-factor (TOTP) authentication for a user. Removes the
     * {@code CONFIGURE_TOTP} required action (if pending) and deletes any existing OTP
     * credential so the user is no longer prompted for a second factor.
     *
     * @param keycloakId
     *            the keycloak id of the user.
     * @throws SecurityException
     */
    public void disableTotp(final String keycloakId) throws SecurityException {
        try {
            final String token = getAdminToken();
            final Map<String, Object> userRepresentation = getUserRepresentation(keycloakId, token);
            final List<String> requiredActions = getRequiredActions(userRepresentation);
            if (requiredActions.contains(CONFIGURE_TOTP_ACTION)) {
                requiredActions.remove(CONFIGURE_TOTP_ACTION);
                userRepresentation.put("requiredActions", requiredActions);
                updateUserRepresentation(keycloakId, userRepresentation, token);
            }
            for (Map<String, Object> credential : getCredentials(keycloakId, token)) {
                if (OTP_CREDENTIAL_TYPE.equals(credential.get("type"))) {
                    deleteCredential(keycloakId, (String) credential.get("id"), token);
                }
            }
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("Could not disable two-factor authentication for user with keycloak id " + keycloakId, e);
        }
    }

    /**
     * Tell whether Keycloak two-factor (TOTP) authentication is enabled for a user.
     *
     * @param keycloakId
     *            the keycloak id of the user.
     * @return {@code true} if an OTP credential exists or the {@code CONFIGURE_TOTP} required
     *         action is set, {@code false} otherwise.
     * @throws SecurityException
     */
    public boolean isTotpEnabled(final String keycloakId) throws SecurityException {
        try {
            final String token = getAdminToken();
            for (Map<String, Object> credential : getCredentials(keycloakId, token)) {
                if (OTP_CREDENTIAL_TYPE.equals(credential.get("type"))) {
                    return true;
                }
            }
            final Map<String, Object> userRepresentation = getUserRepresentation(keycloakId, token);
            return getRequiredActions(userRepresentation).contains(CONFIGURE_TOTP_ACTION);
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("Could not read two-factor authentication status for user with keycloak id " + keycloakId, e);
        }
    }

    /**
     * Enable or disable a user in Keycloak (the account {@code enabled} flag).
     *
     * @param keycloakId
     *            the keycloak id of the user.
     * @param enabled
     *            {@code true} to activate the user, {@code false} to deactivate it.
     * @throws SecurityException
     */
    public void setUserEnabled(final String keycloakId, final boolean enabled) throws SecurityException {
        try {
            final String token = getAdminToken();
            final Map<String, Object> userRepresentation = getUserRepresentation(keycloakId, token);
            userRepresentation.put("enabled", enabled);
            updateUserRepresentation(keycloakId, userRepresentation, token);
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("Could not update the enabled status for user with keycloak id " + keycloakId, e);
        }
    }


    /**
     * Read the enabled (activated) flag of every user of the realm in a single
     * paginated listing, instead of one Keycloak request per user.
     *
     * @return a map of keycloak id to enabled flag.
     * @throws SecurityException
     */
    public Map<String, Boolean> getUsersEnabledStatus() throws SecurityException {
        try {
            final String token = getAdminToken();
            final Map<String, Boolean> enabledByKeycloakId = new HashMap<>();
            List<Map<String, Object>> page;
            int first = 0;
            do {
                page = listUsers(first, USERS_PAGE_SIZE, token);
                for (Map<String, Object> userRepresentation : page) {
                    enabledByKeycloakId.put((String) userRepresentation.get("id"),
                            Boolean.TRUE.equals(userRepresentation.get("enabled")));
                }
                first += page.size();
            } while (page.size() == USERS_PAGE_SIZE);
            return enabledByKeycloakId;
        } catch (Exception e) {
            throw new SecurityException("Could not list the users enabled status from Keycloak.", e);
        }
    }

    /**
     * Tell whether a user is enabled (activated) in Keycloak.
     *
     * @param keycloakId
     *            the keycloak id of the user.
     * @return {@code true} if the user is enabled in Keycloak, {@code false} otherwise.
     * @throws SecurityException
     */
    public boolean isUserEnabled(final String keycloakId) throws SecurityException {
        try {
            final String token = getAdminToken();
            final Map<String, Object> userRepresentation = getUserRepresentation(keycloakId, token);
            return Boolean.TRUE.equals(userRepresentation.get("enabled"));
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("Could not read the enabled status for user with keycloak id " + keycloakId, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getUserRepresentation(String keycloakId, String token) {
        Map<?, ?> response = webClient.get()
                .uri("/admin/realms/" + userRealm + "/users/" + keycloakId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));
        if (response == null) {
            throw new NoSuchElementException("User not found in Keycloak: " + keycloakId);
        }
        return (Map<String, Object>) response;
    }

    private void updateUserRepresentation(String keycloakId, Map<String, Object> userRepresentation, String token) {
        webClient.put()
                .uri("/admin/realms/" + userRealm + "/users/" + keycloakId)
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRepresentation)
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(10));
    }

    @SuppressWarnings("unchecked")
    private List<String> getRequiredActions(Map<String, Object> userRepresentation) {
        List<String> requiredActions = (List<String>) userRepresentation.get("requiredActions");
        return requiredActions == null ? new ArrayList<>() : new ArrayList<>(requiredActions);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getCredentials(String keycloakId, String token) {
        List<?> response = webClient.get()
                .uri("/admin/realms/" + userRealm + "/users/" + keycloakId + "/credentials")
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(List.class)
                .block(Duration.ofSeconds(10));
        return response == null ? List.of() : (List<Map<String, Object>>) response;
    }

    private void deleteCredential(String keycloakId, String credentialId, String token) {
        webClient.delete()
                .uri("/admin/realms/" + userRealm + "/users/" + keycloakId + "/credentials/" + credentialId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(10));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listUsers(int first, int max, String token) {
        List<?> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/realms/" + userRealm + "/users")
                        .queryParam("first", first)
                        .queryParam("max", max)
                        .build())
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(List.class)
                .block(Duration.ofSeconds(10));
        return response == null ? List.of() : (List<Map<String, Object>>) response;
    }

    /**
     * Delete a user.
     *
     * @param username
     *            user name.
     */
    public void deleteUser(final String keycloakId) {
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
