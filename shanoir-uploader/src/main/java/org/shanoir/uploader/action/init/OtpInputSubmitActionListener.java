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
import org.shanoir.uploader.gui.OtpInputPanel;
import org.shanoir.uploader.service.rest.KeycloakAuthCodeLoginService.AuthResult;
import org.shanoir.uploader.service.rest.KeycloakAuthCodeLoginService.AuthStep;
import org.shanoir.uploader.service.rest.KeycloakAuthCodeLoginService.LoginSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OtpInputSubmitActionListener implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(OtpInputSubmitActionListener.class);

    private OtpInputPanel otpInputPanel;
    private StartupStateContext sSC;

    @Autowired
    private PacsConfigurationState pacsConfigurationState;

    @Autowired
    private AuthenticationManualConfigurationState authenticationManualConfigurationState;

    public void configure(OtpInputPanel panel, StartupStateContext sSC) {
        this.otpInputPanel = panel;
        this.sSC = sSC;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String otpCode = otpInputPanel.otpText.getText().trim();
        LoginSession session = sSC.getLoginSession();
        if (session == null) {
            LOG.error("No active login session found for OTP submission.");
            sSC.getShUpStartupDialog().updateStartupText(
                "\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
            sSC.setState(authenticationManualConfigurationState);
            sSC.nextState();
            return;
        }

        try {
            AuthResult result = session.submitOtp(otpCode);
            handleResult(result);
        } catch (Exception ex) {
            LOG.error("OTP submission error: {}", ex.getMessage(), ex);
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
            LOG.info("OTP verification succeeded.");
            sSC.setState(pacsConfigurationState);
            sSC.nextState();
        } else {
            sSC.getShUpStartupDialog().updateStartupText(
                "\n" + ShUpConfig.resourceBundle.getString("shanoir.uploader.startup.test.connection.fail"));
            LOG.warn("OTP verification failed (step={})", result.step);
            sSC.setState(authenticationManualConfigurationState);
            sSC.nextState();
        }
    }
}
