package org.shanoir.uploader.action.init;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.service.RestWebService;
import org.shanoir.uploader.service.IWebService;
import org.shanoir.uploader.service.SoapWebService;

/**
 * This concrete state class defines the state when the ShanoirUploader tests the User Authentication to remote shanoir server
 * 		- loads the property file containing user/(crypted)password for connecting to the remote shanoir server
 * 		- loads the testCredential WSDL webservice
 * 		- test if the user can connect to the remote shanoir server
 *
 * As a result, the context will change either to :
 * 		- a manual user authentication in case of failure
 * 		- step into the next state in case of success.
 *
 * @author atouboul
 * @author mkain
 * 
 */
public class AuthenticationConfigurationState implements State {

	public void load(StartupStateContext context) {
		IWebService webService = null;
		if (ShUpOnloadConfig.isShanoirNg()) {
			webService = RestWebService.getInstance();
		} else {
			webService = SoapWebService.getInstance();
		}
		int initSuccessful = webService.init();
		switch (initSuccessful) {
		case 0:
			break;
		case -3:
			context.getShUpStartupDialog().updateStartupText("\n"+ ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
			context.setState(new ServerUnreachableState());
			context.nextState();
			return;
		}
		int isAccountValid = webService.testConnection();
		switch (isAccountValid) {
		case 0:
			context.getShUpStartupDialog().updateStartupText("\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.success"));
			context.setState(new PacsConfigurationState());
			context.nextState();
			break;
		case -1:
			context.getShUpStartupDialog().updateStartupText("\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
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
