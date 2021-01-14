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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.shanoir.ng.email.EmailService;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.user.repository.UserRepository;
import org.shanoir.ng.utils.KeycloakShanoirUtil;
import org.shanoir.ng.utils.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import groovyjarjarcommonscli.MissingArgumentException;

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
//@EnableRetry
public class ShanoirUsersManagement implements ApplicationRunner {

	private static final String SYNC_ALL_USERS_TO_KEYCLOAK = "syncAllUsersToKeycloak";

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(ShanoirUsersManagement.class);

	/**
	 * Five values are necessary to init the Keycloak client: url, realm, client-id, login, pw
	 * Login and pw are given from the command line to avoid admin pw storage on the disk and
	 * in application.yml. URL, realm and client-id come from application.yml or env configuration.
	 * 
	 */
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

	@Value("${keycloak.realm}")
	private String keycloakRealm;
	
	private Keycloak keycloak;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private EmailService emailService;

	
	@Override
	public void run(final ApplicationArguments args) throws Exception {
		if (args.getOptionNames().isEmpty()) {
			LOG.info("ShanoirUsersManagement called without option. Starting up MS Users without additional operation.");
		} else {
			initKeycloakAdminClient();
			if(args.containsOption("createInitialAdminShanoir")) {
				/**
				 * @ToDo: implement initial admin Shanoir creation here, in database users and in keycloak, check if dbs are really empty.
				 */
			} else if (args.containsOption(SYNC_ALL_USERS_TO_KEYCLOAK)
					&& args.getOptionValues(SYNC_ALL_USERS_TO_KEYCLOAK).get(0) != null
					&& args.getOptionValues(SYNC_ALL_USERS_TO_KEYCLOAK).get(0).equals("true")) {
				
				int tries = 0;
				boolean success = false;
				while (!success && tries < 50) {
					try {
						createUsersIfNotExisting();
						success = true;
					} catch (Exception e) {
						tries++;
						String msg = "Try " + tries + " failed for updating keycloak users on startup (" + e.getMessage() + ")";
						LOG.error(msg); // users logs
						System.out.println(msg); // docker compose console
						TimeUnit.SECONDS.sleep(5);
					}
				}
				if (!success) {
					throw new IllegalStateException("Could not export users to Keycloak.");
				}
			}
		}
	}

	private void initKeycloakAdminClient() throws MissingArgumentException {
			keycloak = Keycloak.getInstance(
				kcAdminClientServerUrl,
				kcAdminClientRealm,
				kcAdminClientUsername,
				kcAdminClientPassword,
				kcAdminClientClientId);
	}

	//@Retryable(value = { ProcessingException.class }, maxAttempts = 50, backoff = @Backoff(delay = 5000))
	private void createUsersIfNotExisting() {
		LOG.info(SYNC_ALL_USERS_TO_KEYCLOAK);
		final Iterable<User> users = userRepository.findAll();
		for (final User user : users) {
			final List<UserRepresentation> userRepresentationList = keycloak.realm(keycloakRealm).users().search(user.getUsername());
			if (userRepresentationList != null && !userRepresentationList.isEmpty()) {
				LOG.debug("User already existing in Keycloak. Do nothing.");
			} else {
				// Add user to Keycloak and save its keycloakId in the users database after
				final UserRepresentation userRepresentation = getUserRepresentation(user);
				final Response response = keycloak.realm(keycloakRealm).users().create(userRepresentation);
				final String keycloakId = KeycloakShanoirUtil.getCreatedUserId(response);
				user.setKeycloakId(keycloakId);
				userRepository.save(user);
				// Reset user password, which is stored in Keycloak only
				final CredentialRepresentation credential = new CredentialRepresentation();
				credential.setType(CredentialRepresentation.PASSWORD);
				String newPassword = PasswordUtils.generatePassword();
				credential.setValue(newPassword);
				credential.setTemporary(true);
				final UserResource userResource = keycloak.realm(keycloakRealm).users().get(keycloakId);
				userResource.resetPassword(credential);
				final RoleResource roleResource = keycloak.realm(keycloakRealm).roles().get(user.getRole().getName());
				userResource.roles().realmLevel().add(Arrays.asList(roleResource.toRepresentation()));
				// Notify user
				emailService.notifyUserResetPassword(user, newPassword);
			}
		}
	}

	private UserRepresentation getUserRepresentation(final User user) {
		final Map<String, List<String>> attributes = new HashMap<>();
		attributes.put("userId", Arrays.asList(user.getId().toString()));
		attributes.put("canImportFromPACS", Arrays.asList("" + user.isCanAccessToDicomAssociation()));
		if (user.getExpirationDate() != null) {
			attributes.put("expirationDate", Arrays.asList("" + user.getExpirationDate()));
		}
		final UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setAttributes(attributes);
		userRepresentation.setEmail(user.getEmail());
		userRepresentation.setEmailVerified(Boolean.TRUE);
		userRepresentation.setEnabled(user.isEnabled());
		userRepresentation.setFirstName(user.getFirstName());
		userRepresentation.setLastName(user.getLastName());
		userRepresentation.setUsername(user.getUsername());
		return userRepresentation;
	}

}
