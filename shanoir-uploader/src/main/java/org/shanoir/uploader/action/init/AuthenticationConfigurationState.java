package org.shanoir.uploader.action.init;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.keycloak.OAuthErrorException;
import org.keycloak.adapters.ServerRequest.HttpFailure;
import org.keycloak.adapters.installed.KeycloakInstalled;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.service.IWebService;
import org.shanoir.uploader.service.SoapWebService;

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
		IWebService webService = null;
		if (ShUpOnloadConfig.isShanoirNg()) {
			try {
				FileInputStream fIS = new FileInputStream(ShUpConfig.keycloakJson);
				KeycloakInstalled keycloak = new KeycloakInstalled(fIS);
				keycloak.setLocale(Locale.ENGLISH);
				keycloak.loginDesktop();
				AccessToken token = keycloak.getToken();
				Executors.newSingleThreadExecutor().submit(() -> {
					logger.info("Logged in...");
					logger.info("Token: " + token.getSubject());
					logger.info("Username: " + token.getPreferredUsername());
					try {
						logger.info("AccessToken: " + keycloak.getTokenString());
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
					}
//					int timeoutSeconds = 20;
//					System.out.printf("Logging out in...%d Seconds%n", timeoutSeconds);
//					try {
//						TimeUnit.SECONDS.sleep(timeoutSeconds);
//					} catch (Exception e) {
//						logger.error(e.getMessage(), e);
//					}
//					try {
//						keycloak.logout();
//					} catch (Exception e) {
//						logger.error(e.getMessage(), e);
//					}
//					logger.info("Exiting...");
//					System.exit(0);
				});
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
			webService = SoapWebService.getInstance();
			int initSuccessful = webService.init();
			switch (initSuccessful) {
			case 0:
				break;
			case -3:
				context.getShUpStartupDialog().updateStartupText(
						"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
				context.setState(new ServerUnreachableState());
				context.nextState();
				return;
			}
			int isAccountValid = webService.testConnection();
			switch (isAccountValid) {
			case 0:
				context.getShUpStartupDialog().updateStartupText(
						"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.success"));
				context.setState(new PacsConfigurationState());
				context.nextState();
				break;
			case -1:
				context.getShUpStartupDialog().updateStartupText(
						"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
				context.setState(new AuthenticationManualConfigurationState());
				context.nextState();
				break;
			case -2:
				context.setState(new ProxyManualConfigurationState());
				context.nextState();
				break;
			case -3:
				context.setState(new ServerUnreachableState());
				context.nextState();
				break;
			}
		}
	}

}
