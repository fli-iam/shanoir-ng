package org.shanoir.uploader.action.init;

import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.keycloak.adapters.installed.KeycloakInstalled;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.uploader.service.soap.ShanoirUploaderServiceClient;

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
			ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG = new ShanoirUploaderServiceClientNG();
			ShUpOnloadConfig.setShanoirUploaderServiceClientNG(shanoirUploaderServiceClientNG);
			try {
				FileInputStream fIS = new FileInputStream(ShUpConfig.keycloakJson);
				KeycloakInstalled keycloakInstalled = new KeycloakInstalled(fIS);
				keycloakInstalled.setLocale(Locale.ENGLISH);
				keycloakInstalled.loginDesktop();
				ShUpOnloadConfig.setKeycloakInstalled(keycloakInstalled);
				/**
				 * Start job, that refreshes token every 20 seconds
				 */
				ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
				Runnable task = () -> {
					try {
						keycloakInstalled.refreshToken();
						logger.info("KeycloakInstalled: token has been refreshed.");
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						context.getShUpStartupDialog().updateStartupText(
								"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
						context.setState(new ServerUnreachableState());
						context.nextState();	
						return;						
					}
				};
				executor.scheduleAtFixedRate(task, 0, 20, TimeUnit.SECONDS);
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				context.getShUpStartupDialog().updateStartupText(
						"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
				context.setState(new ServerUnreachableState());
				context.nextState();	
				return;
			}
			context.getShUpStartupDialog().updateStartupText(
					"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.success"));
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
			}
		}
	}

}
