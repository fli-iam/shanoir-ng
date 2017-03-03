package org.shanoir.ng.keycloak;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.shanoir.ng.model.User;
import org.shanoir.ng.utils.KeycloakUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	@Value("${keycloak.realm}")
	private String keycloakRealm;

	@Value("${kc.requests.admin.login}")
	private String keycloakRequestsAdminLogin;

	@Value("${kc.requests.admin.password}")
	private String keycloakRequestsAdminPassword;

	@Value("${kc.requests.auth-server-url}")
	private String keycloakRequestsAuthServerUrl;
	
	@Value("${kc.requests.client.id}")
	private String keycloakRequestsClientId;

	@Value("${kc.requests.realm}")
	private String keycloakRequestsRealm;

	private Keycloak keycloak;

	protected Keycloak getKeycloak() {
		if (keycloak == null) {
			keycloak = Keycloak.getInstance(keycloakRequestsAuthServerUrl, keycloakRequestsRealm, keycloakRequestsAdminLogin,
					keycloakRequestsAdminPassword, keycloakRequestsClientId);
		}
		return keycloak;
	}

	/**
	 * Create a user.
	 * 
	 * @param user
	 *            user to create.
	 * @return keycloak user id.
	 */
	public String createUser(final User user) {
		try {
			final String keycloakId = KeycloakUtils
					.getCreatedUserId(getKeycloak().realm(keycloakRealm).users().create(getUserRepresentation(user)));
			
			final UserResource userResource = getKeycloak().realm(keycloakRealm).users().get(keycloakId);
			// Reset user password
			// TODO: manage it
			final CredentialRepresentation credential = new CredentialRepresentation();
			credential.setType(CredentialRepresentation.PASSWORD);
			credential.setValue(user.getPassword());
//			credential.setTemporary(???);
//			userResource.resetPassword(credential);

			// Add realm role
			userResource.roles().realmLevel().add(Arrays.asList(
					getKeycloak().realm(keycloakRealm).roles().get(user.getRole().getName()).toRepresentation()));
			return keycloakId;
		} catch (Exception e) {
			LOG.error("Error while creating user with id " + user.getId() + " on Keycloak server", e);
			return null;
		}
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

			// New user password
			// TODO: manage it
			final CredentialRepresentation credential = new CredentialRepresentation();
			credential.setType(CredentialRepresentation.PASSWORD);
			credential.setValue(user.getPassword());
//			credential.setTemporary(???);
//			userResource.resetPassword(credential);

			// Add realm role
//			userResource.roles().realmLevel().add(Arrays.asList(
//					getKeycloak().realm(keycloakRealm).roles().get(user.getRole().getName()).toRepresentation()));
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
		final Map<String, List<String>> attributes = new HashMap<String, List<String>>();
		attributes.put("userId", Arrays.asList(user.getId().toString()));
		if (user.getExpirationDate() != null) {
			attributes.put("expirationDate", Arrays.asList("" + user.getExpirationDate().getTime()));
		}

		final UserRepresentation userRepresentation = new UserRepresentation();
		userRepresentation.setAttributes(attributes);
		userRepresentation.setEmail(user.getEmail());
		userRepresentation.setEnabled(user.isEnabled() && !user.isAccountRequestDemand());
		userRepresentation.setFirstName(user.getFirstName());
		userRepresentation.setLastName(user.getLastName());
		userRepresentation.setUsername(user.getUsername());

		return userRepresentation;
	}

}
