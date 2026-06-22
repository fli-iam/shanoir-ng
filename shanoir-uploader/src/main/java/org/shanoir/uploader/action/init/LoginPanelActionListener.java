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

import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.gui.LoginConfigurationPanel;
import org.shanoir.uploader.service.rest.KeycloakAuthCodeLoginService;
import org.shanoir.uploader.service.rest.KeycloakAuthCodeLoginService.AuthResult;
import org.shanoir.uploader.service.rest.KeycloakAuthCodeLoginService.AuthStep;
import org.shanoir.uploader.service.rest.KeycloakAuthCodeLoginService.LoginSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginPanelActionListener implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(LoginPanelActionListener.class);

    private LoginConfigurationPanel loginPanel;
    private StartupStateContext sSC;

    @Autowired
    private KeycloakAuthCodeLoginService keycloakBrowserLoginService;

    @Autowired
    private PacsConfigurationState pacsConfigurationState;

    @Autowired
    private AuthenticationManualConfigurationState authenticationManualConfigurationState;

    @Autowired
    private OtpInputState otpInputState;

    @Autowired
    private OtpSetupState otpSetupState;

    public void configure(LoginConfigurationPanel loginPanel, StartupStateContext sSC) {
        this.loginPanel = loginPanel;
        this.sSC = sSC;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(loginPanel.connect)) {
            String username = loginPanel.loginText.getText();
            String password = String.valueOf(loginPanel.passwordText.getPassword());
            login(username, password);
        } else if (e.getSource().equals(loginPanel.connectLater)) {
            LOG.info("Connect later: offline mode selected.");
            ShUpConfig.username = "anonymous";
            sSC.setState(pacsConfigurationState);
            sSC.nextState();
        }
    }

    public void login(String username, String password) {
        String serverUrl = ShUpConfig.profileProperties.getProperty("shanoir.server.url");
        try {
            LoginSession session = keycloakBrowserLoginService.createSession(serverUrl);
            sSC.setLoginSession(session);

            AuthResult result = session.submitCredentials(username, password);
            handleResult(result, username);
        } catch (Exception e) {
            LOG.error("Login error: {}", e.getMessage(), e);
            sSC.getShUpStartupDialog().updateStartupText(
                "\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
            ShUpConfig.username = null;
            sSC.setState(authenticationManualConfigurationState);
            sSC.nextState();
        }
    }

    private void handleResult(AuthResult result, String username) {
        if (result.step == AuthStep.SUCCESS) {
            ShUpOnloadConfig.setTokenString(result.accessToken);
            sSC.getShUpStartupDialog().updateStartupText(
                "\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.success"));
            LOG.info("Login successful for username: {}", username);
            ShUpConfig.username = username;
            sSC.setState(pacsConfigurationState);
            sSC.nextState();

        } else if (result.step == AuthStep.OTP_REQUIRED) {
            LOG.info("OTP required for username: {}", username);
            ShUpConfig.username = username;
            sSC.setState(otpInputState);
            sSC.nextState();

        } else if (result.step == AuthStep.OTP_SETUP_REQUIRED) {
            LOG.info("OTP setup required for username: {}", username);
            ShUpConfig.username = username;
            sSC.setPendingQrCodeBytes(result.qrCodeBytes);
            sSC.setPendingTotpManualKey(result.totpManualKey);
            sSC.setState(otpSetupState);
            sSC.nextState();

        } else {
            sSC.getShUpStartupDialog().updateStartupText(
                "\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
            LOG.info("Login failed for username: {}", username);
            ShUpConfig.username = null;
            sSC.setState(authenticationManualConfigurationState);
            sSC.nextState();
        }
    }
}
