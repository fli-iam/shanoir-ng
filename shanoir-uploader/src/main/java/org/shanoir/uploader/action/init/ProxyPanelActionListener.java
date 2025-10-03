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
import org.shanoir.uploader.gui.ProxyConfigurationPanel;
import org.shanoir.uploader.utils.Util;
import org.springframework.stereotype.Component;

@Component
public class ProxyPanelActionListener implements ActionListener {

    private ProxyConfigurationPanel proxyPanel;

    private StartupStateContext sSC;

    public void configure(ProxyConfigurationPanel proxyPanel, StartupStateContext sSC) {
        this.proxyPanel = proxyPanel;
        this.sSC = sSC;
    }

    public void actionPerformed(ActionEvent e) {
        ShUpConfig.proxyProperties.setProperty("proxy.enabled",
                Boolean.toString(this.proxyPanel.enableProxyCB.isSelected()));
        ShUpConfig.proxyProperties.setProperty("proxy.secure",
                Boolean.toString(this.proxyPanel.secureSslProxyCB.isSelected()));
        ShUpConfig.proxyProperties.setProperty("proxy.host", this.proxyPanel.httpHostText.getText());
        ShUpConfig.proxyProperties.setProperty("proxy.port", this.proxyPanel.httpPortText.getText());
        ShUpConfig.proxyProperties.setProperty("proxy.user", this.proxyPanel.httpLoginText.getText());
        ShUpConfig.proxyProperties.setProperty("proxy.password",
                String.valueOf(this.proxyPanel.httpPasswordText.getPassword()));
        Util.encryptPasswordAndCopyPropertiesFile(ShUpConfig.shanoirUploaderFolder, ShUpConfig.proxyProperties,
                ShUpConfig.PROXY_PROPERTIES, "proxy.password");
        sSC.nextState();
    }

}
