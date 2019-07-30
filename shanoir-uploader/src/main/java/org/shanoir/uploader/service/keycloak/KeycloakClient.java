package org.shanoir.uploader.service.keycloak;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotAuthorizedException;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.rotation.AdapterRSATokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.shanoir.uploader.model.dto.StudyDTO;
import org.shanoir.uploader.service.WebServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keycloak client. Used to get token for accessing shanoir-ng microservices
 *
 * @author atouboulic
 *
 */

public class KeycloakClient {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(KeycloakClient.class);
	
	private static Keycloak keycloak;
	
	private static AccessTokenResponse atr;

	protected static Keycloak getKeycloak(KeycloakConfiguration kcConfig) {
		//if (keycloak == null) {
			keycloak = Keycloak.getInstance(kcConfig.getKeycloakRequestsAuthServerUrl(),
					kcConfig.getKeycloakRequestsRealm(), kcConfig.getKeycloakRequestsUserLogin(),
					kcConfig.getKeycloakRequestsUserPassword(), kcConfig.getKeycloakRequestsClientId(),
					kcConfig.getKeycloakRequestsSecret());
		//}
		return keycloak;
	}

	public static WebServiceResponse<AccessTokenResponse> getAccessToken(KeycloakConfiguration kcConfig, boolean forceNewToken) {
		WebServiceResponse<AccessTokenResponse> response = new WebServiceResponse<AccessTokenResponse>();
		if (atr == null || forceNewToken) {
			try {
			atr = getKeycloak(kcConfig).tokenManager().grantToken();
			} catch (NotAuthorizedException nae) {
				LOG.error("ERROR on getAccessToken method: ", nae);
				response.setStatusCode(-1);
				response.setObj(null);
				return response;
			} catch (Exception e) {
				LOG.error("ERROR on getAccessToken method: ", e);
				response.setStatusCode(-3);
				response.setObj(null);
				return response;
			}
		}
		else {
			try {
			atr = getKeycloak(kcConfig).tokenManager().refreshToken();
			} catch (NotAuthorizedException nae) {
				LOG.error("ERROR on getAccessToken method: ", nae);
				response.setStatusCode(-1);
				response.setObj(null);
				return response;
			}
			catch (Exception e) {
				LOG.error("ERROR on getAccessToken method: ", e);
				response.setStatusCode(-3);
				response.setObj(null);
				return response;
			}
		}
		response.setObj(atr);		
		return response;
	}

	public static boolean verifyToken(KeycloakConfiguration kcConfig,AccessTokenResponse atr){
		AdapterConfig config = new AdapterConfig();
        config.setAuthServerUrl(kcConfig.getKeycloakRequestsAuthServerUrl());
        config.setRealm(kcConfig.getKeycloakRequestsRealm());
        config.setRealmKey(kcConfig.getKeycloakRequestsRealmKey());
		config.setResource(kcConfig.getKeycloakRequestsClientId());

		KeycloakDeployment deployment = KeycloakDeploymentBuilder.build(config);
        try {
            AccessToken token = AdapterRSATokenVerifier.verifyToken(atr.getToken(), deployment);
            if (token != null) {
            	return true;
            } else {
            	return false;
            }
        } catch (VerificationException e) {
			LOG.error("ERROR on verifyToken method: ", e);
        } 
        return false;
	}
	
	public static String getUserId(KeycloakConfiguration kcConfig,AccessTokenResponse atr){
		AdapterConfig config = new AdapterConfig();
        config.setAuthServerUrl(kcConfig.getKeycloakRequestsAuthServerUrl());
        config.setRealm(kcConfig.getKeycloakRequestsRealm());
        config.setRealmKey(kcConfig.getKeycloakRequestsRealmKey());
		config.setResource(kcConfig.getKeycloakRequestsClientId());
		String userId = null;
		KeycloakDeployment deployment = KeycloakDeploymentBuilder.build(config);
        try {
            AccessToken token = AdapterRSATokenVerifier.verifyToken(atr.getToken(), deployment);
            if (token != null) {
                Map<String, Object> otherClaims = token.getOtherClaims();
                if (otherClaims.containsKey("userId")) {
                    return String.valueOf(otherClaims.get("userId"));
                }
            }
        } catch (VerificationException e) {
			LOG.error("ERROR on getUserId method: ", e);
        }
        return userId;
	}
	
}