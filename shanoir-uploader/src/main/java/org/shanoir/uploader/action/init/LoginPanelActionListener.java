package org.shanoir.uploader.action.init;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.gui.LoginConfigurationPanel;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClientNG;
import org.shanoir.uploader.service.soap.ServiceConfiguration;

public class LoginPanelActionListener implements ActionListener {

	private LoginConfigurationPanel loginPanel;

	private StartupStateContext sSC;

	public LoginPanelActionListener(LoginConfigurationPanel loginPanel, StartupStateContext sSC) {
		this.loginPanel = loginPanel;
		this.sSC = sSC;
	}

	public void actionPerformed(ActionEvent e) {
		String username = this.loginPanel.loginText.getText();
		String password = String.valueOf(this.loginPanel.passwordText.getPassword());
		if (ShUpOnloadConfig.isShanoirNg()) {
			ShanoirUploaderServiceClientNG shanoirUploaderServiceClientNG = ShUpOnloadConfig.getShanoirUploaderServiceClientNG();
			String token = shanoirUploaderServiceClientNG.loginWithKeycloakForToken(username, password);
			if (token != null) {
				ShUpOnloadConfig.setTokenString(token);
				sSC.getShUpStartupDialog().updateStartupText(
				"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.success"));
				sSC.setState(new PacsConfigurationState());
			} else {
				sSC.getShUpStartupDialog().updateStartupText(
						"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
				sSC.setState(new AuthenticationManualConfigurationState());
			}
		} else {
			ServiceConfiguration serviceConfiguration = ServiceConfiguration.getInstance();
			serviceConfiguration.setUsername(username);
			serviceConfiguration.setPassword(password);
			ShUpConfig.profileProperties.setProperty("shanoir.server.user.name",
					serviceConfiguration.getUsername());
			ShUpConfig.profileProperties.setProperty("shanoir.server.user.password",
					serviceConfiguration.getPassword());
			final File propertiesFile = new File(ShUpConfig.profileDirectory, ShUpConfig.PROFILE_PROPERTIES);
			ShUpConfig.encryption.decryptIfEncryptedString(propertiesFile,
					ShUpConfig.profileProperties, "shanoir.server.user.password");			
		}
		sSC.nextState();
	}

}
