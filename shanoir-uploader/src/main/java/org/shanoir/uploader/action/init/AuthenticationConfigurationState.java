package org.shanoir.uploader.action.init;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
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
			// https://github.com/fli-iam/shanoir-ng/issues/615, KeycloakInstalled removed here as not working in CHUs
			context.setState(new AuthenticationManualConfigurationState());
			context.nextState();
			return;
		} else {
			String serviceURI = ShUpConfig.profileProperties.getProperty("shanoir.server.uploader.service.qname.namespace.uri");
			String serviceLocalPart = ShUpConfig.profileProperties.getProperty("shanoir.server.uploader.service.qname.local.part");
			URL serviceURL = null;
			try {
				serviceURL = new URL(ShUpConfig.profileProperties.getProperty("shanoir.server.uploader.service.url"));
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
