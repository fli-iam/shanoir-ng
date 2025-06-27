package org.shanoir.uploader.action.init;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.shanoir.ng.utils.Utils;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.gui.ShUpStartupDialog;
import org.shanoir.uploader.utils.Encryption;
import org.shanoir.uploader.utils.PropertiesUtil;
import org.shanoir.uploader.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
public class InitialStartupState implements State {

	private static final Logger logger = LoggerFactory.getLogger(InitialStartupState.class);

	//private static final Pattern VERSION_PATTERN = Pattern.compile(".su_v(\\d+)\\.(\\d+)\\.(\\d+)");

	private static final String USER_HOME_FOLDER = System.getProperty(ShUpConfig.USER_HOME);
	
	private static final String SU_V6_0_3 = ".su_v6.0.3";

	private static final String SU_V6_0_4 = ".su_v6.0.4";

	private static final String SU_V7_0_1 = ".su_v7.0.1";

	private static final String SU_V8_0_0 = ".su_v8.0.0";

	@Autowired
	private SelectProfileConfigurationState selectProfileConfigurationState;

	@Autowired
	private ShUpStartupDialog shUpStartupDialog;

	public void load(StartupStateContext context) throws Exception {
		logger.info("Start running of ShanoirUploader...");
		logger.info("Version: " + ShUpConfig.SHANOIR_UPLOADER_VERSION);
		logger.info("Release Date: " + ShUpConfig.RELEASE_DATE);
		logger.info("Java Vendor: " + System.getProperty("java.vendor"));
		logger.info("Java Vendor URL: " + System.getProperty("java.vendor.url"));
		logger.info("Java Version: " + System.getProperty("java.version"));
        InetAddress inetAddress = InetAddress.getLocalHost();
        logger.info("IP Address: " + inetAddress.getHostAddress());
        logger.info("Host Name: " + inetAddress.getHostName());
		logger.info("TimeZone: " + System.getProperty("user.timezone") + ", " + TimeZone.getDefault() + ", " + ZoneId.systemDefault());
		 // Disable http request to check for quartz upload
		System.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");
		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
		doMigration();
		initPropertiesFiles();
		initLanguage();
		copyPseudonymus();
		initProfiles();
		initProfile();
		initCredentials();
		initStartupDialog(context);
		context.setState(selectProfileConfigurationState);
		context.nextState();
	}

	private void doMigration() throws IOException {
		// We run through all previous versions folders in user home folder and delete them after migrating properties.
		File userHomeFolderPath = new File(USER_HOME_FOLDER);
		File[] obsoleteFolders = Arrays.stream(userHomeFolderPath.listFiles())
    		.filter(file -> file.getName().startsWith(ShUpConfig.SU))
    		.filter(file -> !file.getName().contains(ShUpConfig.SHANOIR_UPLOADER_VERSION))
    		.filter(File::isDirectory)
    		.toArray(File[]::new);

		if (obsoleteFolders == null || obsoleteFolders.length == 0) return;

		Arrays.sort(obsoleteFolders, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
		for (File folder : obsoleteFolders) {
    		migrateFromVersion(folder);
		}
	}

	private void migrateFromVersion(File shupFolder) throws IOException {
		String previousVersion;
		// If previous shup folder contains version in its name, we extract it.
		if (shupFolder.getName().contains("_v")) {
			previousVersion = shupFolder.getName().replaceFirst("^\\.ba_v", "");
		// otherwise it is a folder from version 5.2, which does not contain version in its name.
		} else {
			previousVersion = "5.2";
		}
		logger.info("Start migrating properties from version " + previousVersion + " of ShUp.");
		copyPropertiesFile(shupFolder, ShUpConfig.shanoirUploaderFolder, ShUpConfig.LANGUAGE_PROPERTIES);
		copyPropertiesFile(shupFolder, ShUpConfig.shanoirUploaderFolder, ShUpConfig.PROXY_PROPERTIES);
		copyPropertiesFile(shupFolder, ShUpConfig.shanoirUploaderFolder, ShUpConfig.DICOM_SERVER_PROPERTIES);
		logger.info("Finished migrating properties from version " + previousVersion + " of ShUp: language, proxy, dicom_server.");
		Utils.deleteFolder(shupFolder);
		logger.info("Folder from version " + previousVersion + " of ShanoirUploader was successfully deleted.");
	}

	private void copyPropertiesFile(final File srcDir, final File destDir, final String fileName) throws IOException {
		final File propertiesSrc = new File(srcDir, fileName);
		final File propertiesDest = new File(destDir, fileName);
		if (propertiesDest.exists()) {
			// do nothing in case of already existing property file
			logger.info("Start migrating properties: property not copied, because of existing already in destination folder: " + propertiesDest.getAbsolutePath());
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
		shUpStartupDialog.configure(context);
		shUpStartupDialog.setVisible(true);
		context.setShUpStartupDialog(shUpStartupDialog);
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
		
		initProperties(ShUpConfig.ENDPOINT_PROPERTIES,
				ShUpConfig.endpointProperties);
		logger.info("endpoint.properties successfully initialized.");
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
	 * Reads properties from .su folder into memory, or copies property file if not existing.
	 */
	private void initProperties(final String fileName, final Properties properties) {
		final File propertiesFile = new File(ShUpConfig.shanoirUploaderFolder + File.separator + fileName);
		// in case of profiles.properties and endpoint.properties we copy each time from the .jar,
		// as we consider the new code has always right and these files are never edited manually
		// by the installing user: advantage new added endpoints are always considered
		if (propertiesFile.exists()
			&& !fileName.equals(ShUpConfig.PROFILES_PROPERTIES)
			&& !fileName.equals(ShUpConfig.ENDPOINT_PROPERTIES)) {
			// do nothing
		} else {
			Util.copyFileFromJar(fileName, propertiesFile);
		}
		PropertiesUtil.loadPropertiesFromFile(properties, propertiesFile);
	}
	
	private void initLanguage() {
		String language = ShUpConfig.languageProperties.getProperty("shanoir.uploader.language");
		if (language != null && language.equals(ShUpConfig.FRENCH_LANGUAGE)) {
			ShUpConfig.resourceBundle = ResourceBundle.getBundle("messages", Locale.FRENCH);
		} else {
			ShUpConfig.resourceBundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
		}
	}

	private void initProfile() throws FileNotFoundException, IOException {
		// If profile property is not null or empty it means that the "remember profile" box was ticked in a previous execution. 
		String profile = ShUpConfig.basicProperties.getProperty(ShUpConfig.PROFILE);
		if (profile != null && !profile.isEmpty()) {
			ShUpConfig.profileSelected = profile;
		}
	}
	
	private void initCredentials() throws FileNotFoundException, IOException {
		String username = ShUpConfig.basicProperties.getProperty(ShUpConfig.USERNAME);
		String password = ShUpConfig.basicProperties.getProperty(ShUpConfig.PASSWORD);
		if (username != null && !username.isBlank() && password != null && !password.isBlank()) {
			ShUpConfig.username = username;
			ShUpConfig.password = password;
			logger.info("Pre-configured credentials found.");
		}
	}

}
