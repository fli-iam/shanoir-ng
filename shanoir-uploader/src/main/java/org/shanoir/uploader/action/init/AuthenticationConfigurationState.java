package org.shanoir.uploader.action.init;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.keycloak.OAuthErrorException;
import org.keycloak.adapters.ServerRequest.HttpFailure;
import org.keycloak.adapters.installed.KeycloakInstalled;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.service.wsdl.ShanoirUploaderServiceClient;

/**
 * This concrete state class defines the state when the ShanoirUploader tests
 * the User Authentication to remote shanoir server - loads the property file
 * containing user/(crypted)password for connecting to the remote shanoir server
 * - loads the testCredential WSDL webservice - test if the user can connect to
 * the remote shanoir server
 *
 * As a result, the context will change either to : - a manual user
 * authentication in case of failure - step into the next state in case of
 * success.
 *
 * @author atouboul
 * @author mkain
 * 
 */
public class AuthenticationConfigurationState implements State {

	private static Logger logger = Logger.getLogger(AuthenticationConfigurationState.class);

	public void load(StartupStateContext context) {
		if (ShUpOnloadConfig.isShanoirNg()) {
			try {
				FileInputStream fIS = new FileInputStream(ShUpConfig.keycloakJson);
				KeycloakInstalled keycloak = new KeycloakInstalled(fIS);
				keycloak.setLocale(Locale.ENGLISH);
				keycloak.loginDesktop();
				AccessToken token = keycloak.getToken();
//				Executors.newSingleThreadExecutor().submit(() -> {
//					logger.info("Logged in...");
//					logger.info("Token: " + token.getSubject());
//					logger.info("Username: " + token.getPreferredUsername());
//					try {
//						logger.info("AccessToken: " + keycloak.getTokenString());
//					} catch (Exception ex) {
//						logger.error(ex.getMessage(), ex);
//					}
////					int timeoutSeconds = 20;
////					System.out.printf("Logging out in...%d Seconds%n", timeoutSeconds);
////					try {
////						TimeUnit.SECONDS.sleep(timeoutSeconds);
////					} catch (Exception e) {
////						logger.error(e.getMessage(), e);
////					}
////					try {
////						keycloak.logout();
////					} catch (Exception e) {
////						logger.error(e.getMessage(), e);
////					}
////					logger.info("Exiting...");
////					System.exit(0);
//				});
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} catch (VerificationException e) {
				logger.error(e.getMessage(), e);
			} catch (OAuthErrorException e) {
				logger.error(e.getMessage(), e);
			} catch (URISyntaxException e) {
				logger.error(e.getMessage(), e);
			} catch (HttpFailure e) {
				logger.error(e.getMessage(), e);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
			context.setState(new PacsConfigurationState());
			context.nextState();
		} else {
			String serviceURI = ShUpConfig.shanoirServerProperties.getProperty("shanoir.server.uploader.service.qname.namespace.uri");
			String serviceLocalPart = ShUpConfig.shanoirServerProperties.getProperty("shanoir.server.uploader.service.qname.local.part");
			URL serviceURL = null;
			try {
				serviceURL = new URL(ShUpConfig.shanoirServerProperties.getProperty("shanoir.server.uploader.service.url"));
			} catch (MalformedURLException e) {
				logger.error("Property defined in shanoir.server.uploader.service.url (File shanoir_server.properties in .su folder) is not properly configured", e);
				context.getShUpStartupDialog().updateStartupText(
						"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
				context.setState(new ServerUnreachableState());
				context.nextState();
				return;
			}
			try {
				ShanoirUploaderServiceClient shanoirUploaderServiceClient = new ShanoirUploaderServiceClient(serviceURI, serviceLocalPart, serviceURL);
				ShUpOnloadConfig.setShanoirUploaderServiceClient(shanoirUploaderServiceClient);
				boolean isAccountValid = shanoirUploaderServiceClient.login();
				if (isAccountValid) {
					context.getShUpStartupDialog().updateStartupText(
							"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.success"));
					context.setState(new PacsConfigurationState());
					context.nextState();
				} else {
					context.getShUpStartupDialog().updateStartupText(
							"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
					context.setState(new AuthenticationManualConfigurationState());
					context.nextState();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				context.getShUpStartupDialog().updateStartupText(
						"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
				context.setState(new ServerUnreachableState());
				context.nextState();
				return;
			}
		}
	}

}
