package org.shanoir.ng.utils;

import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.ng.utils.tests.TestTrustManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Abstract class used to manage Keycloak token.
 * 
 * @author msimon
 *
 */
public abstract class KeycloakControllerTestIT {

	@Value("${keycloak.auth-server-url}")
	private String keycloakAuthServerUrl;

	@Value("${keycloak.realm}")
	private String keycloakRealm;

	@Value("${keycloak.resource}")
	private String keycloakResource;

	@Value("${keycloak.credentials.secret}")
	private String keycloakCredentialsSecret;

	private String login;
	private String password;
	
	/*
	 * Obtain headers for Keycloack authentication (administrator or guest).
	 * 
	 * @param admin administrator.
	 * 
	 * @return headers.
	 */
	protected HttpHeaders getHeadersWithToken(final boolean admin) throws GeneralSecurityException {
		if (admin) {
			login = "admin";
		} else {
			login = "guest";
		}
		password = "11&&AAaa";
		final AccessTokenResponse tokenResponse = getToken();
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Bearer " + tokenResponse.getToken());
		return headers;
	}

	/*
	 * Obtain a token on behalf of shanoir-ng-users. Send credentials through
	 * direct access api. Make sure the realm has the Direct Grant API switch ON
	 * (it can be found on Settings/Login page!)
	 * 
	 * @return response with Keycloak token.q
	 */
	private AccessTokenResponse getToken() throws GeneralSecurityException { 
		// Install the all-trusting trust manager
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, new TrustManager[] { new TestTrustManager() }, new java.security.SecureRandom());
		
		Keycloak keycloak = Keycloak.getInstance (keycloakAuthServerUrl, keycloakRealm, login, password, keycloakResource, keycloakCredentialsSecret, sslContext);
		return keycloak.tokenManager().getAccessToken();
	}

}
