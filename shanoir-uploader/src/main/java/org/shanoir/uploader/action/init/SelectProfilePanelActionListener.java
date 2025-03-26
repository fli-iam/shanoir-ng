package org.shanoir.uploader.action.init;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.gui.SelectProfileConfigurationPanel;
import org.shanoir.uploader.utils.PropertiesUtil;

@Component
public class SelectProfilePanelActionListener implements ActionListener {
	
	private static final Logger logger = LoggerFactory.getLogger(SelectProfilePanelActionListener.class);

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
			logger.info("Saving Profile selected in basic.properties file.");
			String fileName = ShUpConfig.shanoirUploaderFolder + File.separator + ShUpConfig.BASIC_PROPERTIES;
			final File propertiesFile = new File(fileName);
			boolean propertiesExists = propertiesFile.exists();
			if (propertiesExists) {
				try (OutputStream out = new FileOutputStream(propertiesFile);){
					Properties props = ShUpConfig.basicProperties;
					props.setProperty(ShUpConfig.PROFILE, selectedProfile);
					// Store the new Profile configuration in the basic.properties file
					props.store(out, "Profile Configuration");
				} catch (Exception exception) {
					logger.error("Failed to save selected Profile : " + exception.getMessage());
				}
			}
		}
		sSC.nextState();
	}

	public void configureSelectedProfile(String selectedProfile) {
		String filePath = File.separator + ShUpConfig.PROFILE_DIR + selectedProfile;
		ShUpConfig.profileDirectory = new File(ShUpConfig.shanoirUploaderFolder, filePath);
		logger.info("Profile directory set to: " + ShUpConfig.profileDirectory.getAbsolutePath());
		File profilePropertiesFile = new File(ShUpConfig.profileDirectory, ShUpConfig.PROFILE_PROPERTIES);
		PropertiesUtil.loadPropertiesFromFile(ShUpConfig.profileProperties, profilePropertiesFile);
		logger.info("Profile " + selectedProfile + " successfully initialized.");
		
		File keycloakJson = new File(ShUpConfig.profileDirectory, ShUpConfig.KEYCLOAK_JSON);
		if (keycloakJson.exists()) {
			ShUpConfig.keycloakJson = keycloakJson;
			logger.info("keycloak.json successfully initialized.");
		} else {
			logger.error("Error: missing keycloak.json! Connection with sh-ng will not work.");
			return;
		}

		// check if pseudonymus has been copied in case of true
		if (Boolean.parseBoolean(ShUpConfig.profileProperties.getProperty(ShUpConfig.MODE_PSEUDONYMUS))) {
			// check at first for the executables
			File pseudonymusFolder = new File(ShUpConfig.shanoirUploaderFolder + File.separator + Pseudonymizer.PSEUDONYMUS_FOLDER);
			if (!pseudonymusFolder.exists()) {
				logger.error(pseudonymusFolder.getAbsolutePath() + " folder missing for mode pseudonymus! Please copy manually.");
			}
			// then check for the key in the .jar file
			Properties keyProperties = new Properties();
			InputStream in = getClass().getResourceAsStream("/profile." + selectedProfile + "/" + ShUpConfig.MODE_PSEUDONYMUS_KEY_FILE);
			if (in != null) {
				try {
					keyProperties.load(in);
					in.close();
				} catch (IOException ex) {
					logger.error(ex.getMessage(), ex);
				}
				ShUpConfig.basicProperties.put(ShUpConfig.MODE_PSEUDONYMUS_KEY_FILE, keyProperties.get(ShUpConfig.MODE_PSEUDONYMUS_KEY_FILE));				
			} else {
				logger.error("Missing pseudonymus key file.");
				return;
			}
		}
	}

}
