package org.shanoir.uploader.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * A utility class introduced to perform
 * read/write operations over properties files.
 * @author lvallet
 *
 */
public class PropertiesUtil {

    private static Logger logger = Logger.getLogger(PropertiesUtil.class);
    
    public static void loadPropertiesFromFile(final Properties properties, final File propertiesFile) {
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
