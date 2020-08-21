package org.shanoir.uploader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.ResourceBundle;

import org.shanoir.uploader.utils.Encryption;

/**
 * This class contains all static data needed by ShanoirUploader upon startup.
 * 
 * @author mkain
 * 
 */
public class ShUpConfig {

	static {
        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
    }
	
	/**
	 * Constants
	 */
	public static final String SHANOIR_UPLOADER_VERSION = "v7.0.0";
	
	public static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
	
	public static final String PROFILES_PROPERTIES = "profiles.properties";
	
	public static final String PROFILES_PROPERTY = "profiles";
	
	public static final String PROFILE_DIR = "profile.";
	
	public static final String PROFILE_PROPERTIES = "profile.properties";

	public static final String MODE_PSEUDONYMUS = "mode.pseudonymus";
	
	public static final String MODE_PSEUDONYMUS_KEY_FILE = "key";
	
	public static final String MODE_SUBJECT_COMMON_NAME = "mode.subject.common.name";
	
	public static final String MODE_SUBJECT_COMMON_NAME_AUTO_INCREMENT = "auto-increment";
	
	public static final String MODE_SUBJECT_COMMON_NAME_MANUAL = "manual";
	
	public static final String MODE_SUBJECT_STUDY_IDENTIFIER = "mode.subject.study.identifier";
	
	public static final String BASIC_PROPERTIES = "basic.properties";
	
	public static final String LANGUAGE_PROPERTIES = "language.properties";

	public static final String PROXY_PROPERTIES = "proxy.properties";

	public static final String DICOM_SERVER_PROPERTIES = "dicom_server.properties";
	
	public static final String KEYCLOAK_JSON = "keycloak.json";
	
	public static final String SU = ".su";
	
	public static final String USER_HOME = "user.home";

	public static final String FRENCH_LANGUAGE = "FRENCH";
	
	public static final String WORK_FOLDER = "workFolder";
	
	public static final String UPLOAD_SERVICE_JOB = "uploadServiceJob";
	
	public static final int UPLOAD_SERVICE_INTERVAL = 5;

	public static final String RANDOM_SEED = "random.seed";
	
	/**
	 * Static variables
	 */
	public static Properties basicProperties = new Properties();
	
	public static Properties languageProperties = new Properties();

	public static Properties proxyProperties = new Properties();

	public static Properties dicomServerProperties = new Properties();

	public static Properties profilesProperties = new Properties();
	
	public static String[] profiles;
	
	public static String profileSelected;
	
	public static File profileDirectory;
	
	public static Properties profileProperties = new Properties();
	
	public static File keycloakJson;
	
	public static ResourceBundle resourceBundle;

	public static File shanoirUploaderFolder;

	public static Integer studyCardComplianceLevel;
	
	public static Encryption encryption;
	
	public static boolean isModePseudonymus() {
		return Boolean.parseBoolean(profileProperties.getProperty(MODE_PSEUDONYMUS));
	}
	
	public static boolean isModeSubjectCommonNameAutoIncrement() {
		return MODE_SUBJECT_COMMON_NAME_AUTO_INCREMENT.equals(profileProperties.getProperty(MODE_SUBJECT_COMMON_NAME));
	}

	public static boolean isModeSubjectCommonNameManual() {
		return MODE_SUBJECT_COMMON_NAME_MANUAL.equals(profileProperties.getProperty(MODE_SUBJECT_COMMON_NAME));
	}

	public static boolean isModeSubjectStudyIdentifier() {
		return Boolean.parseBoolean(profileProperties.getProperty(MODE_SUBJECT_STUDY_IDENTIFIER));
	}

}
