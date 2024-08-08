package org.shanoir.uploader.action.init;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.gui.LoginConfigurationPanel;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;

@Component
public class LoginPanelActionListener implements ActionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(LoginPanelActionListener.class);

	private LoginConfigurationPanel loginPanel;

	private StartupStateContext sSC;

	@Autowired
	private PacsConfigurationState pacsConfigurationState;

	@Autowired
	private AuthenticationManualConfigurationState authenticationManualConfigurationState;

	public void configure(LoginConfigurationPanel loginPanel, StartupStateContext sSC) {
		this.loginPanel = loginPanel;
		this.sSC = sSC;
	}

	public void actionPerformed(ActionEvent e) {
		String username = this.loginPanel.loginText.getText();
		String password = String.valueOf(this.loginPanel.passwordText.getPassword());
		login(username, password);
	}

	public void login(String username, String password) {
		ShanoirUploaderServiceClient shanoirUploaderServiceClient = ShUpOnloadConfig.getShanoirUploaderServiceClient();
		String token;
		try {
			token = shanoirUploaderServiceClient.loginWithKeycloakForToken(username, password);
			if (token != null) {
				ShUpOnloadConfig.setTokenString(token);
				sSC.getShUpStartupDialog().updateStartupText(
				"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.success"));
				logger.info("Login successful with username: " + username);
				ShUpConfig.username = username;
				sSC.setState(pacsConfigurationState);
			} else {
				sSC.getShUpStartupDialog().updateStartupText(
						"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
				sSC.setState(authenticationManualConfigurationState);
				logger.info("Login error with username: " + username);
				ShUpConfig.username = null;
			}
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			sSC.getShUpStartupDialog().updateStartupText(
					"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
			sSC.setState(authenticationManualConfigurationState);
		}
		sSC.nextState();
	}

}
