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

import org.shanoir.uploader.gui.OtpInputPanel;
import org.shanoir.uploader.gui.ShUpStartupDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Startup state reached when Keycloak indicates that the user has 2FA enabled
 * and must supply a TOTP one-time code. Displays the {@link OtpInputPanel} and
 * waits for the user to submit the code.
 */
@Component
public class OtpInputState implements State {

    private static final Logger LOG = LoggerFactory.getLogger(OtpInputState.class);

    @Autowired
    private OtpInputPanel otpInputPanel;

    @Override
    public void load(StartupStateContext context) {
        LOG.info("OtpInputState: showing OTP input form.");
        ShUpStartupDialog dialog = context.getShUpStartupDialog();
        otpInputPanel.configure(context);
        dialog.showOtpInputForm();
    }
}
