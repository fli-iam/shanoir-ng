package org.shanoir.uploader.action.init;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.gui.LoginConfigurationPanel;
import org.shanoir.uploader.service.wsdl.ServiceConfiguration;

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
		
		ShUpConfig.shanoirServerProperties.setProperty("shanoir.server.user.name",
				serviceConfiguration.getUsername());
		ShUpConfig.shanoirServerProperties.setProperty("shanoir.server.user.password",
				serviceConfiguration.getPassword());
		ShUpConfig.encryption.decryptIfEncryptedString(ShUpConfig.shanoirUploaderFolder,
				ShUpConfig.shanoirServerProperties, "shanoir.server.user.password",
				ShUpConfig.SHANOIR_SERVER_PROPERTIES);

		ShUpConfig.shanoirNGServerProperties.setProperty("shanoir.server.user.name",
				serviceConfiguration.getUsername());
		ShUpConfig.shanoirNGServerProperties.setProperty("shanoir.server.user.password",
				serviceConfiguration.getPassword());
		ShUpConfig.encryption.decryptIfEncryptedString(ShUpConfig.shanoirUploaderFolder,
				ShUpConfig.shanoirNGServerProperties, "shanoir.server.user.password",
				ShUpConfig.SHANOIR_NG_SERVER_PROPERTIES);
		
		// attention: values are never stored in config files
		sSC.nextState();
	}

}
