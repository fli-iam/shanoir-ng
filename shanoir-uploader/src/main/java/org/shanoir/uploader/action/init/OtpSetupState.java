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

import org.shanoir.uploader.gui.OtpSetupPanel;
import org.shanoir.uploader.gui.ShUpStartupDialog;
import org.shanoir.uploader.service.rest.KeycloakAuthCodeLoginService.LoginSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Startup state reached when Keycloak requires the user to enrol a TOTP
 * authenticator for the first time. Displays the {@link OtpSetupPanel} with
 * the QR code and manual key extracted from the Keycloak HTML response.
 */
@Component
public class OtpSetupState implements State {

    private static final Logger LOG = LoggerFactory.getLogger(OtpSetupState.class);

    @Autowired
    private OtpSetupPanel otpSetupPanel;

    @Override
    public void load(StartupStateContext context) {
        LOG.info("OtpSetupState: showing OTP setup form.");
        LoginSession session = context.getLoginSession();
        ShUpStartupDialog dialog = context.getShUpStartupDialog();

        byte[] qrCodeBytes = context.getPendingQrCodeBytes();
        String totpManualKey = context.getPendingTotpManualKey();

        otpSetupPanel.configure(context);
        otpSetupPanel.populate(qrCodeBytes, totpManualKey);
        dialog.showOtpSetupForm();
    }
}
