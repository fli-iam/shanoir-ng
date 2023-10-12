package org.shanoir.uploader.action.init;


import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpConfig;

public class SelectProfileConfigurationState implements State {

	private static Logger logger = Logger.getLogger(SelectProfileConfigurationState.class);
	
	public void load(StartupStateContext context) {
		if(ShUpConfig.profileSelected == null) {
			context.setState(new SelectProfileManualConfigurationState());
			context.nextState();
		} else {
			logger.info("Profile found in basic.properties. Used as default: " + ShUpConfig.profileSelected);
			context.getShUpStartupDialog().updateStartupText("\nProfile: " + ShUpConfig.profileSelected);
			context.setState(new AuthenticationConfigurationState());
			context.nextState();
		}
	}

}
