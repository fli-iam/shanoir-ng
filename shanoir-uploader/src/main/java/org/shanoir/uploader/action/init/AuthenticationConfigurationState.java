package org.shanoir.uploader.action.init;

import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;

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

	public void load(StartupStateContext context) {
		ShanoirUploaderServiceClient shanoirUploaderServiceClient = new ShanoirUploaderServiceClient();
		ShUpOnloadConfig.setShanoirUploaderServiceClient(shanoirUploaderServiceClient);
		// https://github.com/fli-iam/shanoir-ng/issues/615, KeycloakInstalled removed here as not working in CHUs
		context.setState(new AuthenticationManualConfigurationState());
		context.nextState();
		return;
	}

}
