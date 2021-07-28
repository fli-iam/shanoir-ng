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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.shanoir.ng.role.repository.RoleRepository;
import org.shanoir.ng.shared.exception.SecurityException;
import org.shanoir.ng.user.model.User;
import org.shanoir.ng.utils.KeycloakShanoirUtil;
import org.shanoir.ng.utils.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Keycloak client. Used to execute requests to Keycloak server.
 * 
 * @author msimon
 *
 */
@Component
public class KeycloakClient {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(KeycloakClient.class);

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
	private RoleRepository roleRepository;

	protected Keycloak getKeycloak() {
		if (keycloak == null) {
			keycloak = Keycloak.getInstance(kcAdminClientServerUrl, kcAdminClientRealm,
					kcAdminClientUsername, kcAdminClientPassword, kcAdminClientClientId);
		}
		return keycloak;
	}

	/**
	 * Create a user with a password.
	 * 
	 * @param user
	 *            user to create.
	 * @param password
	 *            user password.
	 * @return keycloak user id.
	 * @throws SecurityException
	 */
	public String createUserWithPassword(final User user, final String password) throws SecurityException {
		try {
			final String keycloakId = KeycloakShanoirUtil
					.getCreatedUserId(getKeycloak().realm(keycloakRealm).users().create(getUserRepresentation(user)));

			final UserResource userResource = getKeycloak().realm(keycloakRealm).users().get(keycloakId);
			// Reset user password
			final CredentialRepresentation credential = new CredentialRepresentation();
			credential.setType(CredentialRepresentation.PASSWORD);
			credential.setValue(password);
			credential.setTemporary(Boolean.TRUE);
			userResource.resetPassword(credential);

			// Get user role
			final String userRoleName = roleRepository.findById(user.getRole().getId()).orElse(null).getName();
			// Add realm role
			userResource.roles().realmLevel().add(Arrays
					.asList(getKeycloak().realm(keycloakRealm).roles().get(userRoleName).toRepresentation()));
			return keycloakId;
		} catch (Exception e) {
			throw new SecurityException("Could not register the new user into Keycloak.", e);
		}
	}

	/**
	 * This methods resets the password of th given user and sets a temporary password.
	 * @param keycloakId the keycloak ID of the user to update.
	 * @return the password newly created
	 */
	public String resetPassword(String keycloakId) {
		// Create new temporary password
		String newPassword = PasswordUtils.generatePassword();

		final CredentialRepresentation credential = new CredentialRepresentation();
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setTemporary(Boolean.TRUE);
		credential.setValue(newPassword);

		final UserResource userResource = keycloak.realm(keycloakRealm).users().get(keycloakId);
		userResource.resetPassword(credential);
		
		return newPassword;
	}
	
	/**
	 * Create a user with a password.
	 * 
	 * @param user
	 *            user to create.
	 * @return keycloak user id.
	 * @throws SecurityException
	 */
	public String createUserWithPassword(final User user) throws SecurityException {
		return createUserWithPassword(user, user.getPassword());
	}
	

	/**
	 * Delete a user.
	 * 
	 * @param username
	 *            user name.
	 */
	public void deleteUser(final String keycloakId) {
		try {
			getKeycloak().realm(keycloakRealm).users().delete(keycloakId);
		} catch (Exception e) {
			LOG.error("Error while deleting user with keycloak id " + keycloakId + " on Keycloak server", e);
		}
	}

	/**
	 * Update a user.
	 * 
	 * @param user
	 *            user to update.
	 */
	public void updateUser(final User user) {
		try {
			final UserResource userResource = getKeycloak().realm(keycloakRealm).users().get(user.getKeycloakId());
			userResource.update(getUserRepresentation(user));

			// Remove old realm role
			final List<String> roleNames = roleRepository.getAllNames();
			final List<RoleRepresentation> roleRepresentations = new ArrayList<>(
					userResource.roles().realmLevel().listAll());
			for (RoleRepresentation roleRepresentation : roleRepresentations) {
				if (roleNames.contains(roleRepresentation.getName())) {
					userResource.roles().realmLevel().remove(Arrays.asList(roleRepresentation));
				}
			}
			// Get user role
			final String userRoleName = roleRepository.findById(user.getRole().getId()).orElse(null).getName();
			// Add realm role
			userResource.roles().realmLevel().add(Arrays
					.asList(getKeycloak().realm(keycloakRealm).roles().get(userRoleName).toRepresentation()));
		} catch (Exception e) {
			LOG.error("Error while updating user with keycloak id " + user.getKeycloakId() + " on Keycloak server", e);
		}
	}

	/*
	 * Parse user to keycloak user representation.
	 * 
	 * @param user user.
	 * 
	 * @return keycloak user representation.
	 */
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
		userRepresentation.setEnabled(user.isEnabled() && (user.isAccountRequestDemand() == null || !user.isAccountRequestDemand()));
		userRepresentation.setFirstName(user.getFirstName());
		userRepresentation.setLastName(user.getLastName());
		userRepresentation.setUsername(user.getUsername());

		return userRepresentation;
	}

}
