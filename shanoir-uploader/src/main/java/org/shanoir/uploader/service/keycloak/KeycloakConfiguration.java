package org.shanoir.uploader.service.keycloak;

import org.shanoir.uploader.service.wsdl.ServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keycloak config. Used to generate keycloak configuration using resource file.
 *
 * @author atouboulic
 *
 */

public class KeycloakConfiguration {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(KeycloakConfiguration.class);

	private String keycloakRealm;

	private String keycloakRequestsUserLogin;

	private String keycloakRequestsUserPassword;

	private String keycloakRequestsAuthServerUrl;

	private String keycloakRequestsClientId;

	private String keycloakRequestsRealm;

	private String keycloakRequestsRealmKey;

	private String keycloakRequestsSecret;
	
	/** Constructeur privé */
	private KeycloakConfiguration()
	{}
 
	/** Instance unique pré-initialisée */
	private static KeycloakConfiguration INSTANCE = new KeycloakConfiguration();
 
	/** Point d'accès pour l'instance unique du singleton */
	public static KeycloakConfiguration getInstance()
	{	return INSTANCE;
	}

	public String getKeycloakRequestsSecret() {
		return keycloakRequestsSecret;
	}

	public void setKeycloakRequestsSecret(String keycloakRequestsSecret) {
		this.keycloakRequestsSecret = keycloakRequestsSecret;
	}

	public String getKeycloakRealm() {
		return keycloakRealm;
	}

	public void setKeycloakRealm(String keycloakRealm) {
		this.keycloakRealm = keycloakRealm;
	}

	public String getKeycloakRequestsUserLogin() {
		return keycloakRequestsUserLogin;
	}

	public void setKeycloakRequestsUserLogin(String keycloakRequestsUserLogin) {
		this.keycloakRequestsUserLogin = keycloakRequestsUserLogin;
	}

	public String getKeycloakRequestsUserPassword() {
		return keycloakRequestsUserPassword;
	}

	public void setKeycloakRequestsUserPassword(String keycloakRequestsUserPassword) {
		this.keycloakRequestsUserPassword = keycloakRequestsUserPassword;
	}

	public String getKeycloakRequestsAuthServerUrl() {
		return keycloakRequestsAuthServerUrl;
	}

	public void setKeycloakRequestsAuthServerUrl(String keycloakRequestsAuthServerUrl) {
		this.keycloakRequestsAuthServerUrl = keycloakRequestsAuthServerUrl;
	}

	public String getKeycloakRequestsClientId() {
		return keycloakRequestsClientId;
	}

	public void setKeycloakRequestsClientId(String keycloakRequestsClientId) {
		this.keycloakRequestsClientId = keycloakRequestsClientId;
	}

	public String getKeycloakRequestsRealm() {
		return keycloakRequestsRealm;
	}

	public void setKeycloakRequestsRealm(String keycloakRequestsRealm) {
		this.keycloakRequestsRealm = keycloakRequestsRealm;
	}

	public String getKeycloakRequestsRealmKey() {
		return keycloakRequestsRealmKey;
	}

	public void setKeycloakRequestsRealmKey(String keycloakRequestsRealmKey) {
		this.keycloakRequestsRealmKey = keycloakRequestsRealmKey;
	}
}