package org.shanoir.uploader.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class introduced to perform
 * read/write operations over properties files.
 * 
 * @author lvallet
 *
 */
public class PropertiesUtil {

	private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

	public static void loadPropertiesFromFile(final Properties properties, final File file) {
		try (FileInputStream fIS = new FileInputStream(file)) {
			properties.load(fIS);
			fIS.close();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void initPropertiesFromResourcePath(final Properties properties, final String path) {
		try (InputStream is = Util.class.getResourceAsStream("/" + path)) {
			if (is == null) {
				logger.warn("Resource not found: {}", path);
				return;
			}
			properties.load(is);
		} catch (IOException e) {
			logger.error("Failed to load properties from resource: {}", path, e);
		}
	}

}
