package org.shanoir.uploader.action.init;


import org.shanoir.uploader.ShUpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SelectProfileConfigurationState implements State {

	private static final Logger logger = LoggerFactory.getLogger(SelectProfileConfigurationState.class);

	@Autowired
	private ProxyConfigurationState proxyConfigurationState;

	@Autowired
	private SelectProfileManualConfigurationState selectProfileManualConfigurationState;

	@Autowired
	private SelectProfilePanelActionListener selectProfilePanelActionListener;

	public void load(StartupStateContext context) {
		if (ShUpConfig.profileSelected == null) {
			context.setState(selectProfileManualConfigurationState);
			context.nextState();
		} else {
			logger.info("Profile found in basic.properties. Used as default: " + ShUpConfig.profileSelected);
			selectProfilePanelActionListener.configure(null, null);
			selectProfilePanelActionListener.configureSelectedProfile(ShUpConfig.profileSelected);
			context.getShUpStartupDialog().updateStartupText("\nProfile: " + ShUpConfig.profileSelected);
			context.setState(proxyConfigurationState);
			context.nextState();
		}
	}

}
