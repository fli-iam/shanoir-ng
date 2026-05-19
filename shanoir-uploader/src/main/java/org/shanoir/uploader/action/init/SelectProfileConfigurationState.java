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


import org.shanoir.uploader.ShUpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SelectProfileConfigurationState implements State {

    private static final Logger LOG = LoggerFactory.getLogger(SelectProfileConfigurationState.class);

    @Autowired
    private ProxyConfigurationState proxyConfigurationState;

    @Autowired
    private SelectProfileManualConfigurationState selectProfileManualConfigurationState;

    @Autowired
    private SelectProfilePanelActionListener selectProfilePanelActionListener;

    public void load(StartupStateContext context) {
        if (ShUpConfig.profileSelected == null) {
            context.setState(selectProfileManualConfigurationState);
            context.nextState();
        } else {
            LOG.info("Profile found in basic.properties. Used as default: " + ShUpConfig.profileSelected);
            selectProfilePanelActionListener.configure(null, null);
            selectProfilePanelActionListener.configureSelectedProfile(ShUpConfig.profileSelected);
            context.getShUpStartupDialog().updateStartupText("\nProfile: " + ShUpConfig.profileSelected);
            context.setState(proxyConfigurationState);
            context.nextState();
        }
    }

}
