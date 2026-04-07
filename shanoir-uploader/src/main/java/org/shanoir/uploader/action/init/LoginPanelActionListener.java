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

    private static final Logger LOG = LoggerFactory.getLogger(LoginPanelActionListener.class);

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
        if (e.getSource().equals(loginPanel.connect)) {
            String username = this.loginPanel.loginText.getText();
            String password = String.valueOf(this.loginPanel.passwordText.getPassword());
            login(username, password);
        } else if (e.getSource().equals(loginPanel.connectLater)) {
            LOG.info("Connect later, no username.");
            ShUpConfig.username = "anonymous";
            sSC.setState(pacsConfigurationState);
            sSC.nextState();
        }
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
                LOG.info("Login successful with username: " + username);
                ShUpConfig.username = username;
                sSC.setState(pacsConfigurationState);
            } else {
                sSC.getShUpStartupDialog().updateStartupText(
                        "\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
                sSC.setState(authenticationManualConfigurationState);
                LOG.info("Login error with username: " + username);
                ShUpConfig.username = null;
            }
        } catch (Exception e1) {
            LOG.error(e1.getMessage(), e1);
            sSC.getShUpStartupDialog().updateStartupText(
                    "\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
            sSC.setState(authenticationManualConfigurationState);
        }
        sSC.nextState();
    }

}
