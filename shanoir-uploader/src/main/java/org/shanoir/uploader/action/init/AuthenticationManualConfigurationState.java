package org.shanoir.uploader.action.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.gui.LoginConfigurationPanel;
import org.shanoir.uploader.gui.ShUpStartupDialog;

@Component
public class AuthenticationManualConfigurationState implements State {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationManualConfigurationState.class);

	@Autowired
	private AuthenticationConfigurationState authenticationConfigurationState;

	@Autowired
	private LoginPanelActionListener loginPanelActionListener;

	@Autowired
	public LoginConfigurationPanel loginPanel;

	public void load(StartupStateContext context) {
		if (ShUpConfig.username == null) {
			ShUpStartupDialog shUpStartupDialog = context.getShUpStartupDialog();
			shUpStartupDialog.showLoginForm();
			context.setState(authenticationConfigurationState);
		} else {
			logger.info("Credentials found in basic.properties. Username: " + ShUpConfig.username);
			context.getShUpStartupDialog().updateStartupText("\nUsername: " + ShUpConfig.username);
			loginPanelActionListener.configure(loginPanel, context);
			loginPanelActionListener.login(ShUpConfig.username, ShUpConfig.password);
		}
	}
	
}
