package org.shanoir.ng.keycloak.authentication;

import java.io.IOException;
import java.util.Properties;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserModel;

/**
 * Action provider used to send request to MS users to update login date after
 * validated login.
 * 
 * @author msimon
 *
 */
public class RecordLoginDateRequiredActionProvider implements RequiredActionProvider {

	private String urlRequest;

	/**
	 * @return the urlRequest
	 * @throws IOException 
	 */
	protected String getUrlRequest() throws IOException {
		if (urlRequest == null) {
			final Properties properties = new Properties();
			try {
				properties.load(this.getClass().getResourceAsStream("/application.properties"));
				urlRequest = properties.getProperty("ms.users.server.address");
			} catch (IOException e) {
				System.out.println("Error while reading properties file: " + e.getMessage());
				throw e;
			}
		}
		return urlRequest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.keycloak.provider.Provider#close()
	 */
	public void close() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.keycloak.authentication.RequiredActionProvider#evaluateTriggers(org.
	 * keycloak.authentication.RequiredActionContext)
	 */
	public void evaluateTriggers(final RequiredActionContext context) {
		final UserModel user = context.getUser();

		final HttpClient client = HttpClientBuilder.create().build();
		try {
			final HttpPost request = new HttpPost(getUrlRequest());

			// Add header
			request.addHeader("Content-type", "application/json");

			// Add content
			request.setEntity(new StringEntity(user.getUsername()));

			client.execute(request);
		} catch (final Exception e) {
			System.out.println("Error while sending request to MS users to update login date: " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.keycloak.authentication.RequiredActionProvider#processAction(org.
	 * keycloak.authentication.RequiredActionContext)
	 */
	public void processAction(final RequiredActionContext context) {
		context.success();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.keycloak.authentication.RequiredActionProvider#
	 * requiredActionChallenge(org.keycloak.authentication.
	 * RequiredActionContext)
	 */
	public void requiredActionChallenge(final RequiredActionContext context) {
	}

}
