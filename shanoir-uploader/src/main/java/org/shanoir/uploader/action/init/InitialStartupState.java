package org.shanoir.uploader.action.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.gui.ShUpStartupDialog;
import org.shanoir.uploader.utils.Encryption;
import org.shanoir.uploader.utils.Util;

/**
 * This concrete state class is the initial state (entry point) of the state machine.
 * It initializes the ShanoirUploader application by 
 * - initializing the logger
 * - initializing the .su folder
 * - loading all required property files
 * - setting the language
 * - creating and showing the startup dialog (dependency to language and properties files);
 * the dialog is then set into the context, to be used and influenced by other states after
 * 
 *  When done the state is changed to ProxyConfigurationState.
 *  
 *  @author atouboul
 *  @author mkain
 * 
 */
public class InitialStartupState implements State {

	private static Logger logger = Logger.getLogger(InitialStartupState.class);
	
	private static final String LOG4J_PROPERTIES = "/log4j.properties";
	
	public void load(StartupStateContext context) {
		initLogging();
		logger.info("Start running of ShanoirUploader...");
		logger.info("Version: " + ShUpConfig.SHANOIR_UPLOADER_VERSION);	
		logger.info(System.getProperty("java.vendor"));
		logger.info(System.getProperty("java.vendor.url"));
		logger.info(System.getProperty("java.version"));
		 // Disable http request to check for quartz upload
		System.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
		initShanoirUploaderFolder();
		initPropertiesFiles();
		initLanguage();
		initStartupDialog(context);
		context.setState(new ProxyConfigurationState());
		context.nextState();
	}

	/**
	 * @param context
	 */
	private void initStartupDialog(StartupStateContext context) {
		ShUpStartupDialog shUpStartupDialog = new ShUpStartupDialog(context);
		shUpStartupDialog.setVisible(true);
		context.setShUpStartupDialog(shUpStartupDialog);
	}
	
	/**
	 * Initialize the logging.
	 */
	private void initLogging() {
		try {
			Properties log4jProperties = new Properties();
			InputStream propsFile = InitialStartupState.class.getResourceAsStream(LOG4J_PROPERTIES);
			log4jProperties.load(propsFile);
			PropertyConfigurator.configure(log4jProperties);
			logger.info("Logging successfully initialized.");
		} catch (IOException e) {
			// System.out here, as error in logging init, only exception
			System.out.println("Init logging error: " + e.getMessage());
		}
	}
	
	private void initPropertiesFiles() {
		// load properties into ShUpConfig properties
		initProperties(ShUpConfig.GENERAL_PROPERTIES, ShUpConfig.generalProperties);
		logger.info("General properties successfully initialized.");
		
		initProperties(ShUpConfig.DICOM_SERVER_PROPERTIES,
				ShUpConfig.dicomServerProperties);
		logger.info("DicomServer properties successfully initialized.");
		
		initProperties(ShUpConfig.SHANOIR_SERVER_PROPERTIES,
				ShUpConfig.shanoirServerProperties);
		new Encryption().decryptIfEncryptedString(ShUpConfig.shanoirUploaderFolder,
				ShUpConfig.shanoirServerProperties, "shanoir.server.user.password",
				ShUpConfig.SHANOIR_SERVER_PROPERTIES);
		logger.info("ShanoirServer properties successfully initialized.");
		
		initProperties(ShUpConfig.SHANOIR_NG_SERVER_PROPERTIES,
				ShUpConfig.shanoirNGServerProperties);
		new Encryption().decryptIfEncryptedString(ShUpConfig.shanoirUploaderFolder,
				ShUpConfig.shanoirNGServerProperties, "shanoir.server.user.password",
				ShUpConfig.SHANOIR_NG_SERVER_PROPERTIES);
		logger.info("ShanoirNGServer properties successfully initialized.");

		initProperties(ShUpConfig.PROXY_PROPERTIES, ShUpConfig.proxyProperties);
		if (ShUpConfig.proxyProperties.getProperty("proxy.password") != null
				&& !ShUpConfig.proxyProperties.getProperty("proxy.password").equals("")) {
			new Encryption().decryptIfEncryptedString(ShUpConfig.shanoirUploaderFolder,
					ShUpConfig.proxyProperties, "proxy.password",
					ShUpConfig.PROXY_PROPERTIES);
		}
		logger.info("Proxy properties successfully initialized.");
		
		initProperties(ShUpConfig.LANGUAGE_PROPERTIES, ShUpConfig.languageProperties);
		logger.info("Language properties successfully initialized.");
		
		// put settings into ShUpOnloadConfig
		ShUpOnloadConfig.setOfsep(Boolean.parseBoolean(ShUpConfig.generalProperties.getProperty("is.ofsep")));
		ShUpOnloadConfig.setShanoirNg(
				Boolean.parseBoolean(ShUpConfig.shanoirNGServerProperties.getProperty("is.ng.up")));
		if (ShUpOnloadConfig.isOfsep()) {
			Util.copyPseudonymusFolder("/" + Pseudonymizer.PSEUDONYMUS_FOLDER);
		}
		ShUpOnloadConfig.setAutoImportEnabled(
				Boolean.parseBoolean(ShUpConfig.generalProperties.getProperty("autoimport.enable")));
	}
	
	/**
	 * Initialize personal properties folder of ShanoirUploader.
	 */
	private void initShanoirUploaderFolder() {
		final String userHomeFolderPath = System.getProperty(ShUpConfig.USER_HOME);
		final String shanoirUploaderFolderPath = userHomeFolderPath
				+ File.separator + ShUpConfig.SU;
		final File shanoirUploaderFolder = new File(shanoirUploaderFolderPath);
		boolean shanoirUploaderFolderExists = shanoirUploaderFolder.exists();
		if (shanoirUploaderFolderExists) {
			// do nothing
		} else {
			shanoirUploaderFolder.mkdirs();
		}
		ShUpConfig.shanoirUploaderFolder = shanoirUploaderFolder;
	}
	
	/**
	 * Reads properties from .su folder into memory, or copies property file if not existing.
	 */
	private void initProperties(final String fileName,
			final Properties properties) {
		final File propertiesFile = new File(ShUpConfig.shanoirUploaderFolder + File.separator + fileName);
		boolean propertiesExists = propertiesFile.exists();
		if (propertiesExists) {
			// do nothing
		} else {
			Util.copyPropertiesFile(fileName, propertiesFile);
		}
		try {
			final FileInputStream fIS = new FileInputStream(propertiesFile);
			properties.load(fIS);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private void initLanguage() {
		String language = ShUpConfig.languageProperties.getProperty("shanoir.uploader.language");
		if (language != null && language.equals(ShUpConfig.FRENCH_LANGUAGE)) {
			ShUpConfig.resourceBundle = ResourceBundle.getBundle("messages", Locale.FRENCH);
		} else {
			ShUpConfig.resourceBundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
		}
	}

}
