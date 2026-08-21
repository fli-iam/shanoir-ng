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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.gui.LoginConfigurationPanel;
import org.shanoir.uploader.gui.ShUpStartupDialog;

@Component
public class AuthenticationManualConfigurationState implements State {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationManualConfigurationState.class);

    @Autowired
    private AuthenticationConfigurationState authenticationConfigurationState;

    @Autowired
    private LoginPanelActionListener loginPanelActionListener;

    @Autowired
    public LoginConfigurationPanel loginPanel;

    public void load(StartupStateContext context) {
        if (ShUpConfig.username == null) {
            ShUpStartupDialog shUpStartupDialog = context.getShUpStartupDialog();
            shUpStartupDialog.showLoginForm();
            context.setState(authenticationConfigurationState);
        } else {
            LOG.info("Credentials found in basic.properties. Username: " + ShUpConfig.username);
            context.getShUpStartupDialog().updateStartupText("\nUsername: " + ShUpConfig.username);
            loginPanelActionListener.configure(loginPanel, context);
            loginPanelActionListener.login(ShUpConfig.username, ShUpConfig.password);
        }
    }

}
