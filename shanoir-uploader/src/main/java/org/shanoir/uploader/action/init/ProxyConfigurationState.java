package org.shanoir.uploader.action.init;


import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.utils.ProxyUtil;

/**
 * This concrete state class defines the state when the ShanoirUploader :
 * 		- Initialize the proxy environment variable if enabled
 * 		- tests the connection to remote shanoir server on using the proxy
 *
 * As a result, the context will change either to :
 * 		- a manual proxy configuration
 * 		- step into the next state in case of success.
 * 
 * @author atouboul
 * @author mkain
 * 
 */
public class ProxyConfigurationState implements State {

	private static Logger logger = Logger.getLogger(ProxyConfigurationState.class);
	
	public void load(StartupStateContext context) {
		ProxyUtil.initializeSystemProxy();
		int httpResponseCode = ProxyUtil.testProxy();
		logger.info("Proxy test returned following code: " + httpResponseCode);
		switch (httpResponseCode){
			case 200 :
				context.getShUpStartupDialog().updateStartupText("\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.proxy.success"));
				context.setState(new AuthenticationConfigurationState());
				context.nextState();
				break;
			default:
				context.getShUpStartupDialog().updateStartupText("\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.proxy.fail"));
				context.setState(new ProxyManualConfigurationState());
				context.nextState();
				break;
		}
	}

}
