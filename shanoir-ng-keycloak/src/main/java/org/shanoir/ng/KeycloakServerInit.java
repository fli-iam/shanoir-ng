package org.shanoir.ng;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.shanoir.ng.model.Role;
import org.shanoir.ng.model.User;
import org.shanoir.ng.repository.RoleRepository;
import org.shanoir.ng.repository.UserRepository;
import org.shanoir.ng.utils.KeycloakUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Hello world!
 *
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class KeycloakServerInit extends SpringBootServletInitializer {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(KeycloakServerInit.class);

	@Value("${kc.requests.admin.login}")
	private String keycloakRequestsAdminLogin;

	@Value("${keycloak.auth-server-url}")
	private String keycloakAuthServerUrl;

	@Value("${keycloak.realm}")
	private String keycloakRealm;

	@Value("${kc.requests.admin.password}")
	private String keycloakRequestsAdminPassword;

	@Value("${kc.requests.client.id}")
	private String keycloakRequestsClientId;

	@Value("${kc.requests.realm}")
	private String keycloakRequestsRealm;

	@Value("${kc.requests.temporary.password}")
	private boolean keycloakTemporaryPassword;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	private Keycloak keycloak;

	protected Keycloak getKeycloak() {
		if (keycloak == null) {
			keycloak = Keycloak.getInstance(keycloakAuthServerUrl, keycloakRequestsRealm, keycloakRequestsAdminLogin,
					keycloakRequestsAdminPassword, keycloakRequestsClientId);
		}
		return keycloak;
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext context = null;
		try {
			context = SpringApplication.run(KeycloakServerInit.class, args);

			context.getBean(KeycloakServerInit.class).start();
		} catch (Exception e) {
			LOG.error("Error while initializing Keycloak server", e);
		} finally {
			if (context != null) {
				context.close();
			}
		}
	}

	private void start() {
		createRoles();
		createUsers();
	}

	private void createRoles() {
		LOG.info("Create roles");

		final Iterable<Role> roles = roleRepository.findAll();
		for (final Role role : roles) {
			getKeycloak().realm(keycloakRealm).roles().create(getRoleRepresentation(role));
		}
	}

	private void createUsers() {
		LOG.info("Create users");

		final Iterable<User> users = userRepository.findAll();
		for (final User user : users) {
			final String keycloakId = KeycloakUtils
					.getCreatedUserId(getKeycloak().realm(keycloakRealm).users().create(getUserRepresentation(user)));
			user.setKeycloakId(keycloakId);
			userRepository.save(user);

			final UserResource userResource = getKeycloak().realm(keycloakRealm).users().get(keycloakId);
			// Reset user password
			final CredentialRepresentation credential = new CredentialRepresentation();
			credential.setType(CredentialRepresentation.PASSWORD);
			// TODO: generate password
			credential.setValue(user.getUsername());
			credential.setTemporary(keycloakTemporaryPassword);
			userResource.resetPassword(credential);

			// Add realm role
			userResource.roles().realmLevel().add(Arrays.asList(
					getKeycloak().realm(keycloakRealm).roles().get(user.getRole().getName()).toRepresentation()));
		}
	}

	/*
	 * Parse user to keycloak user representation.
	 * 
	 * @param user user.
	 * 
	 * @return keycloak user representation.
	 */
	private RoleRepresentation getRoleRepresentation(final Role role) {
		final RoleRepresentation roleRepresentation = new RoleRepresentation();
		roleRepresentation.setClientRole(true);
		roleRepresentation.setName(role.getName());

		return roleRepresentation;
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
		userRepresentation.setEmailVerified(Boolean.TRUE);
		userRepresentation.setEnabled(user.isEnabled());
		userRepresentation.setFirstName(user.getFirstName());
		userRepresentation.setLastName(user.getLastName());
		userRepresentation.setUsername(user.getUsername());

		return userRepresentation;
	}

}
