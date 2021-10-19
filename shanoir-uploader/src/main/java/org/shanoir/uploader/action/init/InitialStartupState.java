package org.shanoir.uploader.action.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.shanoir.uploader.ShUpConfig;
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

	private static final String SU_V6_0_3 = ".su_v6.0.3";

	private static final String SU_V6_0_4 = ".su_v6.0.4";
	
	public void load(StartupStateContext context) throws Exception {
		initShanoirUploaderFolder();
		initLogging();
		logger.info("Start running of ShanoirUploader...");
		logger.info("Version: " + ShUpConfig.SHANOIR_UPLOADER_VERSION);	
		logger.info(System.getProperty("java.vendor"));
		logger.info(System.getProperty("java.vendor.url"));
		logger.info(System.getProperty("java.version"));
        InetAddress inetAddress = InetAddress.getLocalHost();
        logger.info("IP Address:- " + inetAddress.getHostAddress());
        logger.info("Host Name:- " + inetAddress.getHostName());
		 // Disable http request to check for quartz upload
		System.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
		doMigration();
		initPropertiesFiles();
		initLanguage();
		copyPseudonymus();
		initProfiles();
		initStartupDialog(context);
		context.setState(new ProxyConfigurationState());
		context.nextState();
	}

	private void doMigration() throws IOException {
		// as properties, that exist already are not replaced/changed, start with the last version before,
		// as considered as more important
		// overwrite with properties from ShanoirUploader v6.0.4 or v6.0.3, if existing
		migrateFromVersion(SU_V6_0_4);
		migrateFromVersion(SU_V6_0_3);
		// migrate properties from ShanoirUploader v5.2
		migrateFromVersion(ShUpConfig.SU);
	}

	private void migrateFromVersion(String version) throws IOException {
		final String userHomeFolderPath = System.getProperty(ShUpConfig.USER_HOME);
		final String shanoirUploaderFolderPathForVersion = userHomeFolderPath + File.separator + version;
		final File shanoirUploaderFolderForVersion = new File(shanoirUploaderFolderPathForVersion);
		boolean shanoirUploaderFolderExistsForVersion = shanoirUploaderFolderForVersion.exists();
		if (shanoirUploaderFolderExistsForVersion) {
			logger.info("Start migrating properties from version " + version + " (.su == v5.2) of ShUp.");
			copyPropertiesFile(shanoirUploaderFolderForVersion, ShUpConfig.shanoirUploaderFolder, ShUpConfig.LANGUAGE_PROPERTIES);
			copyPropertiesFile(shanoirUploaderFolderForVersion, ShUpConfig.shanoirUploaderFolder, ShUpConfig.PROXY_PROPERTIES);
			copyPropertiesFile(shanoirUploaderFolderForVersion, ShUpConfig.shanoirUploaderFolder, ShUpConfig.DICOM_SERVER_PROPERTIES);
			logger.info("Finished migrating properties from version " + version + " (.su == v5.2) of ShUp: language, proxy, dicom_server.");
		}
	}

	private void copyPropertiesFile(final File srcDir, final File destDir, final String fileName) throws IOException {
		final File propertiesSrc = new File(srcDir, fileName);
		final File propertiesDest = new File(destDir, fileName);
		if (propertiesDest.exists()) {
			// do nothing in case of existing
			logger.info("Start migrating properties: property not copied, because of existing already.");
		} else {
			Util.copyFileUsingStream(propertiesSrc, propertiesDest);			
		}
	}

	private void copyPseudonymus() {
		Util.copyPseudonymusFolder(Pseudonymizer.PSEUDONYMUS_FOLDER);
		logger.info("Pseudonymus successfully copied.");
	}

	private void initProfiles() throws IOException {
		initProperties(ShUpConfig.PROFILES_PROPERTIES, ShUpConfig.profilesProperties);
		logger.info("profiles.properties successfully initialized.");
		// iterate over list of profiles, create folder and copy 3 types of files if existing
		String profilesStr = ShUpConfig.profilesProperties.getProperty(ShUpConfig.PROFILES_PROPERTY);
		String[] profiles = profilesStr.split(",");
		// check if the old profiles setup exists, named with -NG
		boolean migrateProfiles = false;
		for (int i = 0; i < profiles.length; i++) {
			if (profiles[i].contains("-NG")) {
				migrateProfiles = true;
			}
		}
		if (migrateProfiles) {
			logger.info("Profiles migration starts...");
			logger.info("Deletion of all old profiles folders.");
			for (int i = 0; i < profiles.length; i++) {
				File profileDir = new File(ShUpConfig.shanoirUploaderFolder, ShUpConfig.PROFILE_DIR + profiles[i]);
				if (profileDir.exists()) {
					logger.info("Profile migration: deletion of old profile: " + profileDir.getAbsolutePath());
					Files.walk(profileDir.toPath())
				      .sorted(Comparator.reverseOrder())
				      .map(Path::toFile)
				      .forEach(File::delete);
				}
			}
			logger.info("Deletion of old profiles.properties.");
			File profilesPropertiesOld = new File(ShUpConfig.shanoirUploaderFolder, ShUpConfig.PROFILES_PROPERTIES);
			if (profilesPropertiesOld.exists()) {
				profilesPropertiesOld.delete();
			}
			initProperties(ShUpConfig.PROFILES_PROPERTIES, ShUpConfig.profilesProperties);
			logger.info("New profiles.properties successfully initialized with new profiles.");
			profilesStr = ShUpConfig.profilesProperties.getProperty(ShUpConfig.PROFILES_PROPERTY);
			profiles = profilesStr.split(",");
			logger.info("Profiles migration finished...");
		}
		
		for (int i = 0; i < profiles.length; i++) {
			logger.info("Checking profile folder: " + profiles[i]);
			File profileDir = new File(ShUpConfig.shanoirUploaderFolder, ShUpConfig.PROFILE_DIR + profiles[i]);
			if (profileDir.exists()) {
				logger.info("Keep existing profile folder: " + profiles[i]);
				// do nothing and keep local config
			} else {
				profileDir.mkdirs();
				logger.info("Profile folder created: " + profiles[i]);
				// copy for each profile "profile.properties"
				File profilePropertiesFile = new File(profileDir, ShUpConfig.PROFILE_PROPERTIES);
				Util.copyFileFromJar(ShUpConfig.PROFILE_DIR + profiles[i] + "/" + ShUpConfig.PROFILE_PROPERTIES, profilePropertiesFile);
				// copy pseudonymus key, if existing
				File keyFile = new File(profileDir, ShUpConfig.MODE_PSEUDONYMUS_KEY_FILE);
				Util.copyFileFromJar(ShUpConfig.PROFILE_DIR + profiles[i] + "/" + ShUpConfig.MODE_PSEUDONYMUS_KEY_FILE, keyFile);
				// copy keycloak.json, if existing
				File keycloakFile = new File(profileDir, ShUpConfig.KEYCLOAK_JSON);
				Util.copyFileFromJar(ShUpConfig.PROFILE_DIR + profiles[i] + "/" + ShUpConfig.KEYCLOAK_JSON, keycloakFile);			
			}
		}
		ShUpConfig.profiles = profiles;
	}

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
		initProperties(ShUpConfig.BASIC_PROPERTIES, ShUpConfig.basicProperties);
		logger.info("basic.properties successfully initialized.");

		initProperties(ShUpConfig.LANGUAGE_PROPERTIES, ShUpConfig.languageProperties);
		logger.info("language.properties successfully initialized.");

		String randomSeed = generateRandomSeed();
		ShUpConfig.encryption = new Encryption(randomSeed);
		logger.info("random.seed successfully initialized.");

		initProperties(ShUpConfig.PROXY_PROPERTIES, ShUpConfig.proxyProperties);
		if (ShUpConfig.proxyProperties.getProperty("proxy.password") != null
				&& !ShUpConfig.proxyProperties.getProperty("proxy.password").equals("")) {
			File proxyProperties = new File(ShUpConfig.shanoirUploaderFolder, ShUpConfig.PROXY_PROPERTIES);
			ShUpConfig.encryption.decryptIfEncryptedString(proxyProperties,
					ShUpConfig.proxyProperties, "proxy.password");
		}
		logger.info("proxy.properties successfully initialized.");

		initProperties(ShUpConfig.DICOM_SERVER_PROPERTIES,
				ShUpConfig.dicomServerProperties);
		logger.info("dicom_server.properties successfully initialized.");
	}

	private String generateRandomSeed() throws FileNotFoundException, IOException {
		String randomSeed = ShUpConfig.basicProperties.getProperty(ShUpConfig.RANDOM_SEED);
		if (randomSeed != null && !randomSeed.isEmpty() && !randomSeed.equals("0")) {
			return randomSeed;
		} else {
			Random r = new Random();
			int num = r.nextInt(10000);
			String knum = String.valueOf(num);
			ShUpConfig.basicProperties.setProperty(ShUpConfig.RANDOM_SEED, knum);
			final File basicProps = new File(ShUpConfig.shanoirUploaderFolder + File.separator + ShUpConfig.BASIC_PROPERTIES);
			OutputStream out = new FileOutputStream(basicProps);
			ShUpConfig.basicProperties.store(out, "basic.properties");
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
			Util.copyFileFromJar(fileName, propertiesFile);
		}
		loadPropertiesFromFile(properties, propertiesFile);
	}

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
