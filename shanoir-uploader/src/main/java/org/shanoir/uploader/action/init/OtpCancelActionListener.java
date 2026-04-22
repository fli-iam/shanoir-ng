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
import org.shanoir.uploader.action.init.StartupStateContext;
import org.shanoir.uploader.action.init.AuthenticationManualConfigurationState;
import org.shanoir.uploader.gui.ShUpStartupDialog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OtpCancelActionListener implements ActionListener {

    private StartupStateContext sSC;

    @Autowired
    private ShUpStartupDialog shUpStartupDialog;

    @Autowired
    private AuthenticationManualConfigurationState authenticationManualConfigurationState;

    public void configure(StartupStateContext sSC) {
        this.sSC = sSC;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Go back to login screen
        shUpStartupDialog.showLoginForm();
    }
}