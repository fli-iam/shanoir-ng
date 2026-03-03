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

package org.shanoir.ng;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.study.rights.StudyRightsService;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * This class does the user(s) management for Shanoir-NG in relation with
 * Keycloak and its dedicated microservice. This class reads command line
 * arguments (options) at the end of the start-up of the microservice users
 * (ShanoirUsersApplication).
 *
 * For security reasons, we do not provide these features using a REST
 * interface. The administrator/deploy script, that runs the ms users decides
 * with additional options on the command line, what user(s) management
 * operations should be performed:
 *
 * For code duplication reasons, we have not put this code, that interacts with:
 * - MS keycloak - EmailService - Database users into a separate .jar file (what
 * could be done), but put it directly into ms users, as ms users has already
 * access to all these resources. I decided to run up ms users differently
 * depending what the current admin wants as result.
 *
 * This component implements the ApplicationRunner interface, what means Spring
 * Boot will call it at the end of its start-up and pre-format the command line
 * arguments as ApplicationArguments, what is better to handle than a string.
 *
 * 1) First user creation, e.g. the first initial admin user of the system, when
 * we start with a completely empty Shanoir-NG users database.
 *
 * 2) After a migration, when all existing users have been migrated into the ms
 * users relational database, all users not yet existing in Keycloak can be
 * copied into ms keycloak, that the accounts work. Same applies for development
 * purpose: create all users from import.sql in Keycloak.
 *
 */
@Component
public class ShanoirUsersManagement implements ApplicationRunner {

    private static final String SYNC_ALL_USERS_TO_KEYCLOAK = "syncAllUsersToKeycloak";

    private static final Logger LOG = LoggerFactory.getLogger(ShanoirUsersManagement.class);

    @Value("${kc.admin.client.server.url}")
    private String kcAdminClientServerUrl;

    @Value("${kc.admin.client.realm}")
    private String kcAdminClientRealm;

    @Value("${kc.admin.client.client.id}")
    private String kcAdminClientClientId;

    @Value("${SHANOIR_KEYCLOAK_USER}")
    private String kcAdminClientUsername;

    @Value("${SHANOIR_KEYCLOAK_PASSWORD}")
    private String kcAdminClientPassword;

    @Value("${kc.admin.client.realm.users}")
    private String keycloakRealm;

    @Value("${service-account.user.name}")
    private String vipSrvUsername;

    @Value("${service-account.user.email}")
    private String vipSrvEmail;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private StudyRightsService commService;

    private WebClient webClient;

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        if (args.getOptionNames().isEmpty()) {
            LOG.info(
                    "ShanoirUsersManagement called without option. Starting up MS Users without additional operation.");
            return;
        }

        if (args.containsOption(SYNC_ALL_USERS_TO_KEYCLOAK)
                && "true".equals(args.getOptionValues(SYNC_ALL_USERS_TO_KEYCLOAK).get(0))) {

            this.webClient = WebClient.builder()
                    .baseUrl(kcAdminClientServerUrl)
                    .build();

            if (!StringUtils.isBlank(vipSrvEmail)) {
                this.setVIPServiceAccountEmail();
            }

            int tries = 0;
            boolean success = false;
            while (!success && tries < 50) {
                try {
                    createUsersIfNotExisting();
                    success = true;
                } catch (Exception e) {
                    tries++;
                    String msg = "Try [" + tries + "] failed for updating keycloak users on startup (" + e.getMessage()
                            + ")";
                    LOG.error(msg, e);
                    System.out.println(msg);
                    TimeUnit.SECONDS.sleep(5);
                }
            }
            if (!success) {
                throw new IllegalStateException("Could not export users to Keycloak.");
            }
        }
    }

    private String getAdminToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", kcAdminClientClientId);
        form.add("username", kcAdminClientUsername);
        form.add("password", kcAdminClientPassword);
        Map<?, ?> response = webClient.post()
                .uri("/realms/" + kcAdminClientRealm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));
        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("Failed to obtain admin token from Keycloak.");
        }
        return (String) response.get("access_token");
    }

    private void createUsersIfNotExisting() {
        LOG.info(SYNC_ALL_USERS_TO_KEYCLOAK);
        final String token = getAdminToken();
        final Iterable<User> users = userRepository.findAll();
        for (final User user : users) {
            List<?> existing = searchUserByUsername(user.getUsername(), token);
            if (existing != null && !existing.isEmpty()) {
                LOG.debug("User already existing in Keycloak. Do nothing.");
            } else {
                String keycloakId = createUser(user, token);
                user.setKeycloakId(keycloakId);
                userRepository.save(user);
                String newPassword = PasswordUtils.generatePassword();
                resetPassword(keycloakId, newPassword, token);
                assignRealmRole(keycloakId, user.getRole().getName(), token);
                emailService.notifyUserResetPassword(user, newPassword);
            }
        }
    }

    private List<?> searchUserByUsername(String username, String token) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/realms/" + keycloakRealm + "/users")
                        .queryParam("username", username)
                        .queryParam("exact", true)
                        .build())
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(List.class)
                .block(Duration.ofSeconds(10));
    }

    private String createUser(User user, String token) {
        Map<String, Object> body = buildUserRepresentation(user);
        HttpHeaders headers = webClient.post()
                .uri("/admin/realms/" + keycloakRealm + "/users")
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchangeToMono(response -> {
                    if (!response.statusCode().is2xxSuccessful()) {
                        return Mono.error(new IllegalStateException(
                                "Failed to create user in Keycloak. Status: " + response.statusCode()));
                    }
                    return Mono.just(response.headers().asHttpHeaders());
                })
                .block(Duration.ofSeconds(10));
        String location = Objects.requireNonNull(
                Objects.requireNonNull(headers).getLocation(),
                "No Location header returned after user creation").getPath();
        return location.replaceAll(".*/([^/]+)$", "$1");
    }

    private void resetPassword(String keycloakId, String newPassword, String token) {
        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", newPassword);
        credential.put("temporary", true);
        webClient.put()
                .uri("/admin/realms/" + keycloakRealm + "/users/" + keycloakId + "/reset-password")
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(credential)
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(10));
    }

    private void assignRealmRole(String keycloakId, String roleName, String token) {
        // 1. Fetch the role representation
        Map<?, ?> role = webClient.get()
                .uri("/admin/realms/" + keycloakRealm + "/roles/" + roleName)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));
        if (role == null) {
            throw new IllegalStateException("Role not found in Keycloak: " + roleName);
        }
        // 2. Assign the role to the user
        webClient.post()
                .uri("/admin/realms/" + keycloakRealm + "/users/" + keycloakId + "/role-mappings/realm")
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(List.of(role))
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(10));
    }

    /**
     * Set up the email ${service-account.user.email}
     * of the keycloak user ${service-account.user.name}
     * ('service-account-service-account')
     * associated with the keycloak client 'service-account'
     *
     * See service-account.user.* application properties
     */
    private void setVIPServiceAccountEmail() {
        String token = getAdminToken();

        List<?> users = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/realms/" + keycloakRealm + "/users")
                        .queryParam("username", vipSrvUsername)
                        .queryParam("exact", true)
                        .build())
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(List.class)
                .block(Duration.ofSeconds(10));

        if (users == null || users.isEmpty()) {
            LOG.debug("User [{}] does not exist in Keycloak. Do nothing.", vipSrvUsername);
            return;
        }
        if (users.size() > 1) {
            LOG.error("Multiple users [{}] found in Keycloak.", vipSrvUsername);
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> userRep = (Map<String, Object>) users.get(0);
        String userId = (String) userRep.get("id");
        userRep.put("email", vipSrvEmail);

        webClient.put()
                .uri("/admin/realms/" + keycloakRealm + "/users/" + userId)
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRep)
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(10));
    }

    private Map<String, Object> buildUserRepresentation(User user) {
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("userId", List.of(user.getId().toString()));
        attributes.put("canImportFromPACS", List.of("" + user.isCanAccessToDicomAssociation()));
        if (user.getExpirationDate() != null) {
            attributes.put("expirationDate", List.of("" + user.getExpirationDate()));
        }
        Map<String, Object> body = new HashMap<>();
        body.put("attributes", attributes);
        body.put("email", user.getEmail());
        body.put("emailVerified", true);
        body.put("enabled", user.isEnabled());
        body.put("firstName", user.getFirstName());
        body.put("lastName", user.getLastName());
        body.put("username", user.getUsername());
        return body;
    }

    /**
     * Check that the connected user has the given right for the given study.
     *
     * @param studyId the study id
     * @param rightStr the right
     * @return true or false
     */
    public boolean hasRightOnStudy(Long studyId, String rightStr) {
        if (KeycloakUtil.isAdmin()) {
            return true;
        }
        if (studyId == null) {
            return false;
        }
        return commService.hasRightOnStudy(studyId, rightStr);
    }

}
