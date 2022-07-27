package org.shanoir.uploader.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.utils.Util;

/**
 * This class is the base class for all ShUp test classes, that do
 * integration tests on shanoir servers. It provides the login and
 * the creation of the ShanoirUploaderServiceClient.
 * 
 * @author mkain
 *
 */
public abstract class AbstractTest {

	private static Logger logger = Logger.getLogger(AbstractTest.class);

	private static final String TEST_PROPERTIES = "test.properties";
	
	public static Properties testProperties = new Properties();

	private static final String PROFILE = "profile";

	private static final String USER_NAME = "user.name";

	private static final String USER_PASSWORD = "user.password";

	protected static ShanoirUploaderServiceClient shUpClient;
	
	@BeforeAll
	public static void setup() {
		initProperties(TEST_PROPERTIES, testProperties);
		initProperties(ShUpConfig.PROFILE_DIR + testProperties.getProperty(PROFILE) + "/" + ShUpConfig.PROFILE_PROPERTIES,
				ShUpConfig.profileProperties);
		shUpClient = new ShanoirUploaderServiceClient();
		String user = testProperties.getProperty(USER_NAME);
		String password = testProperties.getProperty(USER_PASSWORD);
		String token = shUpClient.loginWithKeycloakForToken(user, password);
		if (token != null) {
			ShUpOnloadConfig.setTokenString(token);
		} else {
			logger.error("ERROR: login not successful.");
		}
	}

	private static void initProperties(final String fileName, final Properties properties) {
		try {
			InputStream iS = Util.class.getResourceAsStream("/" + fileName);
			if (iS != null) {
				properties.load(iS);
				iS.close();
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
