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
import org.shanoir.uploader.gui.OtpSetupPanel;
import org.shanoir.uploader.service.rest.KeycloakAuthCodeLoginService.AuthResult;
import org.shanoir.uploader.service.rest.KeycloakAuthCodeLoginService.AuthStep;
import org.shanoir.uploader.service.rest.KeycloakAuthCodeLoginService.LoginSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OtpSetupSubmitActionListener implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(OtpSetupSubmitActionListener.class);

    private OtpSetupPanel otpSetupPanel;
    private StartupStateContext sSC;

    @Autowired
    private PacsConfigurationState pacsConfigurationState;

    @Autowired
    private OtpInputState otpInputState;

    @Autowired
    private AuthenticationManualConfigurationState authenticationManualConfigurationState;

    public void configure(OtpSetupPanel panel, StartupStateContext sSC) {
        this.otpSetupPanel = panel;
        this.sSC = sSC;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String otpCode = otpSetupPanel.otpText.getText().trim();
        String deviceLabel = otpSetupPanel.deviceLabelText.getText().trim();
        LoginSession session = sSC.getLoginSession();
        if (session == null) {
            LOG.error("No active login session found for OTP setup submission.");
            sSC.getShUpStartupDialog().updateStartupText(
                "\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
            sSC.setState(authenticationManualConfigurationState);
            sSC.nextState();
            return;
        }

        try {
            AuthResult result = session.submitOtpSetup(otpCode, deviceLabel);
            handleResult(result);
        } catch (Exception ex) {
            LOG.error("OTP setup submission error: {}", ex.getMessage(), ex);
            sSC.getShUpStartupDialog().updateStartupText(
                "\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
            sSC.setState(authenticationManualConfigurationState);
            sSC.nextState();
        }
    }

    private void handleResult(AuthResult result) {
        if (result.step == AuthStep.SUCCESS) {
            ShUpOnloadConfig.setTokenString(result.accessToken);
            sSC.getShUpStartupDialog().updateStartupText(
                "\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.success"));
            LOG.info("OTP setup and verification succeeded.");
            sSC.setState(pacsConfigurationState);
            sSC.nextState();
        } else if (result.step == AuthStep.OTP_REQUIRED) {
            // Keycloak confirmed enrollment and now asks for a fresh OTP code
            sSC.setState(otpInputState);
            sSC.nextState();
        } else {
            sSC.getShUpStartupDialog().updateStartupText(
                "\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
            LOG.warn("OTP setup failed (step={})", result.step);
            sSC.setState(authenticationManualConfigurationState);
            sSC.nextState();
        }
    }
}
