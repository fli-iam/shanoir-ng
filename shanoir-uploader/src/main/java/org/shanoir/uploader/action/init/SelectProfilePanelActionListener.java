package org.shanoir.uploader.action.init;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.gui.SelectProfileConfigurationPanel;

public class SelectProfilePanelActionListener implements ActionListener {
	
	private static Logger logger = Logger.getLogger(SelectProfilePanelActionListener.class);

	private SelectProfileConfigurationPanel selectProfilePanel;

	private StartupStateContext sSC;

	public SelectProfilePanelActionListener(SelectProfileConfigurationPanel selectProfilePanel, StartupStateContext sSC) {
		this.selectProfilePanel = selectProfilePanel;
		this.sSC = sSC;
	}

	public void actionPerformed(ActionEvent e) {
		String selectedProfile = (String) selectProfilePanel.selectProfileCB.getSelectedItem();
		ShUpConfig.profileSelected = selectedProfile;
		String filePath = File.separator + ShUpConfig.PROFILE_DIR + selectedProfile;
		ShUpConfig.profileDirectory = new File(ShUpConfig.shanoirUploaderFolder, filePath);
		logger.info("Profile directory set to: " + ShUpConfig.profileDirectory.getAbsolutePath());
		File profilePropertiesFile = new File(ShUpConfig.profileDirectory, ShUpConfig.PROFILE_PROPERTIES);
		loadPropertiesFromFile(profilePropertiesFile, ShUpConfig.profileProperties);
		
		ShUpConfig.encryption.decryptIfEncryptedString(profilePropertiesFile,
				ShUpConfig.profileProperties, "shanoir.server.user.password");
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
			// than check for the key in the .jar file
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
		sSC.nextState();
	}

	private void loadPropertiesFromFile(final File propertiesFile, final Properties properties) {
		try {
			final FileInputStream fIS = new FileInputStream(propertiesFile);
			properties.load(fIS);
			fIS.close();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
