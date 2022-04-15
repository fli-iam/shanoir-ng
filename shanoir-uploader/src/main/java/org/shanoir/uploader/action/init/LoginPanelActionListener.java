/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.uploader.action.init;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.gui.LoginConfigurationPanel;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;

public class LoginPanelActionListener implements ActionListener {
	
	private static Logger logger = Logger.getLogger(LoginPanelActionListener.class);

	private LoginConfigurationPanel loginPanel;

	private StartupStateContext sSC;

	public LoginPanelActionListener(LoginConfigurationPanel loginPanel, StartupStateContext sSC) {
		this.loginPanel = loginPanel;
		this.sSC = sSC;
	}

	public void actionPerformed(ActionEvent e) {
		String username = this.loginPanel.loginText.getText();
		String password = String.valueOf(this.loginPanel.passwordText.getPassword());
		ShanoirUploaderServiceClient shanoirUploaderServiceClient = ShUpOnloadConfig.getShanoirUploaderServiceClient();
		String token;
		try {
			token = shanoirUploaderServiceClient.loginWithKeycloakForToken(username, password);
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
		} catch (JSONException e1) {
			logger.error(e1.getMessage(), e1);
			sSC.getShUpStartupDialog().updateStartupText(
					"\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
			sSC.setState(new AuthenticationManualConfigurationState());
		}
		sSC.nextState();
	}

}
