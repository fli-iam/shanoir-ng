package org.shanoir.ng.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.HttpClientBuilder;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.util.JsonSerialization;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Utility class used to manage Keycloak tokens for tests.
 * 
 * @author msimon
 *
 */
public final class AccessTokenUtil {

	/**
	 * Obtain headers for Keycloack authentication.
	 * 
	 * @return headers.
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static HttpHeaders getHeadersWithToken() throws ClientProtocolException, IOException {
		final AccessTokenResponse tokenResponse = getToken();
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Bearer " + tokenResponse.getToken());
		return headers;
	}

	/*
	 * Obtain a token on behalf of ngular2-product. Send credentials through
	 * direct access api:
	 * http://docs.jboss.org/keycloak/docs/1.2.0.CR1/userguide/html/direct-
	 * access-grants.html Make sure the realm has the Direct Grant API switch ON
	 * (it can be found on Settings/Login page!)
	 * 
	 * @return response with Keycloak token.
	 * 
	 * @throws ClientProtocolException
	 * 
	 * @throws IOException
	 */
	private static AccessTokenResponse getToken() throws ClientProtocolException, IOException {
		final CloseableHttpClient client = (CloseableHttpClient) new HttpClientBuilder().disableTrustManager().build();
		try {
			final HttpPost post = new HttpPost(KeycloakUriBuilder.fromUri("http://localhost:8080/auth")
					.path(ServiceUrlConstants.TOKEN_PATH).build("demo"));
			final List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			formparams.add(new BasicNameValuePair(OAuth2Constants.GRANT_TYPE, "password"));
			formparams.add(new BasicNameValuePair("username", "admin"));
			formparams.add(new BasicNameValuePair("password", "admin"));

			// will obtain a token on behalf of angular2-product
			formparams.add(new BasicNameValuePair(OAuth2Constants.CLIENT_ID, "angular2-product"));

			final UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
			post.setEntity(form);
			final HttpResponse response = client.execute(post);
			final int status = response.getStatusLine().getStatusCode();
			final org.apache.http.HttpEntity entity = response.getEntity();
			if (status != 200) {
				throw new IOException("Bad status: " + status);
			}
			if (entity == null) {
				throw new IOException("No Entity");
			}
			final InputStream is = entity.getContent();
			try {
				return JsonSerialization.readValue(is, AccessTokenResponse.class);
			} finally {
				try {
					is.close();
				} catch (IOException ignored) {
				}
			}
		} finally {
			client.close();
		}
	}

}
