package org.shanoir.ng.keycloak;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.keycloak.admin.client.Keycloak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class used for keycloak server configuration.
 *
 */
public abstract class AbstractKeycloakInit {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(AbstractKeycloakInit.class);

	private static final String KEYCLOAK_USER_ENV = "KEYCLOAK_USER";
	private static final String KEYCLOAK_PASSWORD_ENV = "KEYCLOAK_PASSWORD";
	
	private static String keycloakAuthServerUrl;

	private static String keycloakRealm;

	private static String keycloakRequestsAdminLogin;
	
	private static String keycloakRequestsAdminPassword;

	private static String keycloakRequestsClientId;

	private static String keycloakRequestsRealm;

	private static Keycloak keycloak;

	/**
	 * @return the keycloakRealm
	 */
	protected static String getKeycloakRealm() {
		return keycloakRealm;
	}

	protected static Keycloak getKeycloak() {
		if (keycloak == null) {
			keycloak = Keycloak.getInstance(keycloakAuthServerUrl, keycloakRequestsRealm, keycloakRequestsAdminLogin,
					keycloakRequestsAdminPassword, keycloakRequestsClientId);
		}
		return keycloak;
	}
	
	protected static void loadParams() {
		LOG.info("Load parameters");
		
		keycloakRequestsAdminLogin = System.getenv(KEYCLOAK_USER_ENV);
		keycloakRequestsAdminPassword = System.getenv(KEYCLOAK_PASSWORD_ENV);
		
		Properties prop = new Properties();
		InputStream input = null;

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			input = classLoader.getResourceAsStream("application.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			keycloakAuthServerUrl = prop.getProperty("keycloak.auth-server-url");
			keycloakRealm = prop.getProperty("keycloak.realm");
			keycloakRequestsClientId = prop.getProperty("kc.requests.client.id");
			keycloakRequestsRealm = prop.getProperty("kc.requests.client.realm");

		} catch (IOException e) {
			LOG.error("Error while getting properties", e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					LOG.error("Error while closing properties file", e);
				}
			}
		}
	}

}
