package org.shanoir.ng.shared.migration;

import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpStatus;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.json.JSONObject;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * This class is used to connect to a distant Shanoir using an URL, a username and a password, and to keep open the connection
 * @author fli
 *
 */
@Service
public class DistantKeycloakConfigurationService {


	private static final Logger LOG = LoggerFactory.getLogger(DistantKeycloakConfigurationService.class);

	private static final String SHANOIR_QUALIF = "shanoir-qualif";
	private static final String SHANOIR_DEV = "shanoir-ng-nginx";
	
	@Autowired
	RestTemplate restTemplate;
	
	RestTemplate weakRestTemplate;

	RestTemplate usedTemplate;
	
	private String refreshToken;
	
	private String accessToken;

	private String server;

	private ScheduledExecutorService executor;
	
	
	public DistantKeycloakConfigurationService() {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext;
		try {
			sslContext = org.apache.http.ssl.SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy)
					.build();

			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

			CloseableHttpClient httpClient = HttpClients.custom()
					.setSSLSocketFactory(csf)
					.build();

			HttpComponentsClientHttpRequestFactory requestFactory =
					new HttpComponentsClientHttpRequestFactory();

			requestFactory.setHttpClient(httpClient);
			weakRestTemplate = new RestTemplate(requestFactory);
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			LOG.error("Could not get a valid rest template.");
		}
	}
	
	/**
	 * Connects to a distant keycloak and keeps the connection alive
	 * @param shanoirUrl
	 * @param username
	 * @param userPassword
	 */
	public void connectToDistantKeycloak(String shanoirUrl, String username, String userPassword) throws ShanoirException {
		// Connect
		this.setServer(shanoirUrl);
		if (shanoirUrl.contains(SHANOIR_QUALIF) || shanoirUrl.contains(SHANOIR_DEV)) {
			usedTemplate = weakRestTemplate;
		} else {
			usedTemplate = restTemplate;
		}
		String keycloakURL = shanoirUrl + "/auth/realms/shanoir-ng/protocol/openid-connect/token";
		try {
			final StringBuilder postBody = new StringBuilder();
			postBody.append("client_id=shanoir-uploader");
			postBody.append("&grant_type=password");
			postBody.append("&username=").append(URLEncoder.encode(username, "UTF-8"));
			postBody.append("&password=").append(URLEncoder.encode(userPassword, "UTF-8"));
			postBody.append("&scope=offline_access");
			
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.set("Content-type", "application/x-www-form-urlencoded");

			ResponseEntity<String> response = usedTemplate.exchange(keycloakURL, HttpMethod.POST, new HttpEntity<>(postBody.toString(), headers), String.class);
			// Keep connection alive
			final int statusCode = response.getStatusCodeValue();
			if (HttpStatus.SC_OK == statusCode) {
				JSONObject responseEntityJson = new JSONObject(response.getBody());
				// Access token
				String newAccessToken = responseEntityJson.getString("access_token");
				if (newAccessToken != null) {
					accessToken = newAccessToken;
				} else {
					LOG.error("ERROR: with access token refresh.");
				}
				// Refresh token
				refreshToken = responseEntityJson.getString("refresh_token");
				this.refreshToken(keycloakURL);
			} else {
				throw new ShanoirException("ERROR: Access token could NOT be getted: HttpStatus-" + statusCode + response.toString());
			}
		} catch (Exception e) {
			throw new ShanoirException("ERROR: Could not connect to distant keycloak", e);
		}
	}

	/**
	 * Start job, that refreshes the access token every 240 seconds.
	 * The default access token lifetime of Keycloak is 5 min (300 secs),
	 * we update after 4 min (240 secs) to use the time frame, but not to
	 * be to close to the end.
	 */
	public void refreshToken(String keycloakURL) {
		if (keycloakURL.contains(SHANOIR_QUALIF) || keycloakURL.contains(SHANOIR_DEV)) {
			usedTemplate = weakRestTemplate;
		} else {
			usedTemplate = restTemplate;
		}
		final StringBuilder postBody = new StringBuilder();
		postBody.append("client_id=shanoir-uploader");
		postBody.append("&grant_type=refresh_token");
		postBody.append("&refresh_token=").append(refreshToken);
		executor = Executors.newScheduledThreadPool(1);
		Runnable task = () -> {
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
				headers.set("Content-type", "application/x-www-form-urlencoded");

				ResponseEntity<String> response = usedTemplate.exchange(keycloakURL, HttpMethod.POST, new HttpEntity<>(postBody.toString(), headers), String.class);

				// Keep connection alive
				final int statusCode = response.getStatusCodeValue();
				if (HttpStatus.SC_OK == statusCode) {
					JSONObject responseEntityJson = new JSONObject(response.getBody());
					String newAccessToken = responseEntityJson.getString("access_token");
					if (newAccessToken != null) {
						accessToken = newAccessToken;
					} else {
						LOG.error("ERROR: with access token refresh.");
					}
					LOG.error("Access token has been refreshed.");
				} else {
					LOG.error("ERROR: Access token could NOT be refreshed: HttpStatus-" + statusCode);
				}
			} catch (Exception e) {
				LOG.error("Could not refresh distant keycloak token", e);
			}
		};
		executor.scheduleAtFixedRate(task, 0, 240, TimeUnit.SECONDS);
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}
	
	/**
	 * @return the refreshToken
	 */
	public String getRefreshToken() {
		return refreshToken;
	}
	
	/**
	 * @param refreshToken the refreshToken to set
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	/**
	 * @return the accessToken
	 */
	public String getAccessToken() {
		return accessToken;
	}


	/**
	 * Stop the distant keycloak connection
	 */
	public void stop() {
		if (this.executor != null) {
			this.executor.shutdown();
		}
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public RestTemplate getRestTemplate() {
		return usedTemplate;
	}
}
