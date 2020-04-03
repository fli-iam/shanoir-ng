package org.shanoir.uploader.action.init;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.gui.LoginConfigurationPanel;
import org.shanoir.uploader.service.soap.ServiceConfiguration;

public class LoginPanelActionListener implements ActionListener {

	private LoginConfigurationPanel loginPanel;

	private StartupStateContext sSC;

	public LoginPanelActionListener(LoginConfigurationPanel loginPanel, StartupStateContext sSC) {
		this.loginPanel = loginPanel;
		this.sSC = sSC;
	}

	public void actionPerformed(ActionEvent e) {
		ServiceConfiguration serviceConfiguration = ServiceConfiguration.getInstance();
		serviceConfiguration.setUsername(this.loginPanel.loginText.getText());
		serviceConfiguration.setPassword(String.valueOf(this.loginPanel.passwordText.getPassword()));
		ShUpConfig.profileProperties.setProperty("shanoir.server.user.name",
				serviceConfiguration.getUsername());
		ShUpConfig.profileProperties.setProperty("shanoir.server.user.password",
				serviceConfiguration.getPassword());
		final File propertiesFile = new File(ShUpConfig.profileDirectory, ShUpConfig.PROFILE_PROPERTIES);
		ShUpConfig.encryption.decryptIfEncryptedString(propertiesFile,
				ShUpConfig.profileProperties, "shanoir.server.user.password");
		sSC.nextState();
	}

}
