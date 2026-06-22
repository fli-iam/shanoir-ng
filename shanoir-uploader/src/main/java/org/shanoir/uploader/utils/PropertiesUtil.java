/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.uploader.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesUtil.class);

    public static void loadPropertiesFromFile(final Properties properties, final File file) {
        try (FileInputStream fIS = new FileInputStream(file)) {
            properties.load(fIS);
            fIS.close();
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static void storePropertyToFile(String filePath, Properties properties, String key, String value) {
        final File propertiesFile = new File(filePath);
        boolean propertiesExists = propertiesFile.exists();
        if (propertiesExists) {
            try (OutputStream out = new FileOutputStream(propertiesFile);) {
                properties.setProperty(key, value);
                properties.store(out, "");
            } catch (Exception e) {
                LOG.error("Failed to store property: " + e.getMessage(), e);
            }
        }
    }

    public static void initPropertiesFromResourcePath(final Properties properties, final String path) {
        try (InputStream is = Util.class.getResourceAsStream("/" + path)) {
            if (is == null) {
                LOG.warn("Resource not found: {}", path);
                return;
            }
            properties.load(is);
        } catch (IOException e) {
            LOG.error("Failed to load properties from resource: {}", path, e);
        }
    }

}
