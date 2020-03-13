package org.shanoir.uploader.action.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

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
	
	public void load(StartupStateContext context) throws Exception {
		initShanoirUploaderFolder();
		initLogging();
		logger.info("Start running of ShanoirUploader...");
		logger.info("Version: " + ShUpConfig.SHANOIR_UPLOADER_VERSION);	
		logger.info(System.getProperty("java.vendor"));
		logger.info(System.getProperty("java.vendor.url"));
		logger.info(System.getProperty("java.version"));
		 // Disable http request to check for quartz upload
		System.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
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
			log4jProperties.put("log4j.appender.file.File",
					ShUpConfig.shanoirUploaderFolder.getAbsolutePath() + File.separator + "su.log");
			PropertyConfigurator.configure(log4jProperties);
			logger.info("Logging successfully initialized.");
		} catch (IOException e) {
			// System.out here, as error in logging init, only exception
			System.out.println("Init logging error: " + e.getMessage());
		}
	}
	
	private void initPropertiesFiles() throws FileNotFoundException, IOException {
		// load properties into ShUpConfig properties (and copy into .su if necessary)
		initProperties(ShUpConfig.GENERAL_PROPERTIES, ShUpConfig.generalProperties);
		logger.info("general.properties successfully initialized.");
		
		String randomSeed = generateRandomSeed();
		ShUpConfig.encryption = new Encryption(randomSeed);
		logger.info("random.seed successfully initialized.");
		
		initProperties(ShUpConfig.DICOM_SERVER_PROPERTIES,
				ShUpConfig.dicomServerProperties);
		logger.info("dicom_server.properties successfully initialized.");
		
		initProperties(ShUpConfig.SHANOIR_SERVER_PROPERTIES,
				ShUpConfig.shanoirServerProperties);
		ShUpConfig.encryption.decryptIfEncryptedString(ShUpConfig.shanoirUploaderFolder,
				ShUpConfig.shanoirServerProperties, "shanoir.server.user.password",
				ShUpConfig.SHANOIR_SERVER_PROPERTIES);
		logger.info("shanoir_server.properties successfully initialized.");
		
		initProperties(ShUpConfig.SHANOIR_NG_SERVER_PROPERTIES,
				ShUpConfig.shanoirNGServerProperties);
		ShUpConfig.keycloakJson = initFile(ShUpConfig.KEYCLOAK_JSON);
		logger.info("shanoir_ng_server.properties and keycloak.json successfully initialized.");

		initProperties(ShUpConfig.PROXY_PROPERTIES, ShUpConfig.proxyProperties);
		if (ShUpConfig.proxyProperties.getProperty("proxy.password") != null
				&& !ShUpConfig.proxyProperties.getProperty("proxy.password").equals("")) {
			ShUpConfig.encryption.decryptIfEncryptedString(ShUpConfig.shanoirUploaderFolder,
					ShUpConfig.proxyProperties, "proxy.password",
					ShUpConfig.PROXY_PROPERTIES);
		}
		logger.info("proxy.properties successfully initialized.");
		
		initProperties(ShUpConfig.LANGUAGE_PROPERTIES, ShUpConfig.languageProperties);
		logger.info("language.properties successfully initialized.");
		
		// check if pseudonymus has been copied in case of true
		if (Boolean.parseBoolean(ShUpConfig.generalProperties.getProperty(ShUpConfig.MODE_PSEUDONYMUS))) {
			// check at first for the executables
			File pseudonymusFolder = new File(ShUpConfig.shanoirUploaderFolder + File.separator + Pseudonymizer.PSEUDONYMUS_FOLDER);
			if (!pseudonymusFolder.exists()) {
				throw new FileNotFoundException(pseudonymusFolder.getAbsolutePath() + " folder missing for mode pseudonymus! Please copy manually.");
			}
			// than check for the key in the .jar file
			Properties keyProperties = new Properties();
			InputStream in = getClass().getResourceAsStream(ShUpConfig.MODE_PSEUDONYMUS_KEY_FILE);
			keyProperties.load(in);
			in.close();
			ShUpConfig.generalProperties.put("key", keyProperties.get("key"));
		}
		
		// put settings into ShUpOnloadConfig
		ShUpOnloadConfig.setShanoirNg(
				Boolean.parseBoolean(ShUpConfig.shanoirNGServerProperties.getProperty("is.ng.up")));
	}

	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private String generateRandomSeed() throws FileNotFoundException, IOException {
		String randomSeed = ShUpConfig.generalProperties.getProperty("random.seed");
		if (randomSeed != null && !randomSeed.isEmpty() && !randomSeed.equals("0")) {
			return randomSeed;
		} else {
			Random r = new Random();
			int num = r.nextInt(10000);
			String knum = String.valueOf(num);
			ShUpConfig.generalProperties.setProperty("random.seed", knum);
			final File generalProps = new File(ShUpConfig.shanoirUploaderFolder + File.separator + ShUpConfig.GENERAL_PROPERTIES);
			OutputStream out = new FileOutputStream(generalProps);
			ShUpConfig.generalProperties.store(out, "general.properties");
			out.close();
			return knum;
		}
	}
	
	/**
	 * Initialize personal properties folder of ShanoirUploader.
	 */
	private void initShanoirUploaderFolder() {
		final String userHomeFolderPath = System.getProperty(ShUpConfig.USER_HOME);
		final String shanoirUploaderFolderPath = userHomeFolderPath
				+ File.separator + ShUpConfig.SU + "_" + ShUpConfig.SHANOIR_UPLOADER_VERSION;
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
	private void initProperties(final String fileName, final Properties properties) {
		final File propertiesFile = new File(ShUpConfig.shanoirUploaderFolder + File.separator + fileName);
		if (propertiesFile.exists()) {
			// do nothing
		} else {
			Util.copyPropertiesFile(fileName, propertiesFile);
		}
		loadPropertiesFromFile(properties, propertiesFile);
	}
	
	/**
	 * Read file from .su folder into memory, or copy file from .jar into .su folder
	 * @param fileName
	 * @return
	 */
	private File initFile(final String fileName) {
		final File file = new File(ShUpConfig.shanoirUploaderFolder + File.separator + fileName);
		if (file.exists()) {
			// do nothing
		} else {
			Util.copyPropertiesFile(fileName, file);
		}
		return file;
	}

	/**
	 * @param properties
	 * @param propertiesFile
	 */
	private void loadPropertiesFromFile(final Properties properties, final File propertiesFile) {
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
	
	private void initLanguage() {
		String language = ShUpConfig.languageProperties.getProperty("shanoir.uploader.language");
		if (language != null && language.equals(ShUpConfig.FRENCH_LANGUAGE)) {
			ShUpConfig.resourceBundle = ResourceBundle.getBundle("messages", Locale.FRENCH);
		} else {
			ShUpConfig.resourceBundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
		}
	}

}
