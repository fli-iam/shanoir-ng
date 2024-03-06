package org.shanoir.uploader.action.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.gui.ShUpStartupDialog;

public class AuthenticationManualConfigurationState implements State {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationManualConfigurationState.class);

	public void load(StartupStateContext context) {
		if(ShUpConfig.username == null) {
			ShUpStartupDialog shUpStartupDialog = context.getShUpStartupDialog();
			shUpStartupDialog.showLoginForm();
			context.setState(new AuthenticationConfigurationState());
		} else {
			logger.info("Credentials found in basic.properties. Username: " + ShUpConfig.username);
			context.getShUpStartupDialog().updateStartupText("\nUsername: " + ShUpConfig.username);
			LoginPanelActionListener loginPanelAL = new LoginPanelActionListener(null, context);
			loginPanelAL.login(ShUpConfig.username, ShUpConfig.password);
		}
	}
	
}
