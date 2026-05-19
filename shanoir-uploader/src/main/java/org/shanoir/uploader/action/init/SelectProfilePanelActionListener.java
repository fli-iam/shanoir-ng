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
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.gui.SelectProfileConfigurationPanel;
import org.shanoir.uploader.utils.PropertiesUtil;

@Component
public class SelectProfilePanelActionListener implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(SelectProfilePanelActionListener.class);

    private SelectProfileConfigurationPanel selectProfilePanel;

    private StartupStateContext sSC;

    public void configure(SelectProfileConfigurationPanel selectProfilePanel, StartupStateContext sSC) {
        this.selectProfilePanel = selectProfilePanel;
        this.sSC = sSC;
    }

    public void actionPerformed(ActionEvent e) {
        String selectedProfile = (String) selectProfilePanel.selectProfileCB.getSelectedItem();
        ShUpConfig.profileSelected = selectedProfile;
        configureSelectedProfile(selectedProfile);

        // If the "Remember profile" box is ticked, we store the selected profile in basic.properties
        // to avoid displaying the Profile selection the next time the application starts
        if (selectProfilePanel.rbRememberProfile.isSelected()) {
            LOG.info("Saving Profile selected in basic.properties file.");
            String fileName = ShUpConfig.shanoirUploaderFolder + File.separator + ShUpConfig.BASIC_PROPERTIES;
            PropertiesUtil.storePropertyToFile(fileName, ShUpConfig.basicProperties, ShUpConfig.PROFILE, selectedProfile);
        }
        sSC.nextState();
    }

    public void configureSelectedProfile(String selectedProfile) {
        String filePath = File.separator + ShUpConfig.PROFILE_DIR + selectedProfile;
        ShUpConfig.profileDirectory = new File(ShUpConfig.shanoirUploaderFolder, filePath);
        LOG.info("Profile directory set to: " + ShUpConfig.profileDirectory.getAbsolutePath());
        File profilePropertiesFile = new File(ShUpConfig.profileDirectory, ShUpConfig.PROFILE_PROPERTIES);
        PropertiesUtil.loadPropertiesFromFile(ShUpConfig.profileProperties, profilePropertiesFile);
        LOG.info("Profile " + selectedProfile + " successfully initialized.");

        File keycloakJson = new File(ShUpConfig.profileDirectory, ShUpConfig.KEYCLOAK_JSON);
        if (keycloakJson.exists()) {
            ShUpConfig.keycloakJson = keycloakJson;
            LOG.info("keycloak.json successfully initialized.");
        } else {
            LOG.error("Error: missing keycloak.json! Connection with sh-ng will not work.");
            return;
        }

        // check if pseudonymus has been copied in case of true
        if (Boolean.parseBoolean(ShUpConfig.profileProperties.getProperty(ShUpConfig.MODE_PSEUDONYMUS))) {
            // check at first for the executables
            File pseudonymusFolder = new File(ShUpConfig.shanoirUploaderFolder + File.separator + Pseudonymizer.PSEUDONYMUS_FOLDER);
            if (!pseudonymusFolder.exists()) {
                LOG.error(pseudonymusFolder.getAbsolutePath() + " folder missing for mode pseudonymus! Please copy manually.");
            }
        }
    }

}
