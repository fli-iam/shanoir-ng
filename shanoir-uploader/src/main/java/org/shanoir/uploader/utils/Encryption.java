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
import java.security.GeneralSecurityException;
import java.util.Properties;

import org.shanoir.uploader.cryptography.AesGcmAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encrypts/decrypts the user and proxy passwords stored in the properties files
 * using AES-256-GCM (see {@link AesGcmAlgorithm}).
 *
 * Values in any other format (e.g. legacy Blowfish or plain text) are not
 * supported anymore: they are rejected with an error and must be reset.
 *
 * @author ifakhfak
 */
public class Encryption {

    private static final Logger LOG = LoggerFactory.getLogger(Encryption.class);

    private final AesGcmAlgorithm aes;

    public Encryption(String key) {
        this.aes = new AesGcmAlgorithm(key);
    }

    /**
     * Replaces the property value in memory by its clear text.
     * If the value is not in the current encrypted format, or cannot be
     * decrypted, an error is logged and the property is removed from memory.
     *
     * @param propertiesFile unused, kept for API compatibility
     */
    public void decryptIfEncryptedString(File propertiesFile, Properties propertyObject, String propertyString) {
        String stored = propertyObject.getProperty(propertyString);
        if (stored == null || stored.isEmpty()) {
            return;
        }

        if (!AesGcmAlgorithm.isEncrypted(stored)) {
            LOG.error("Property '" + propertyString + "' is not in the supported encrypted format "
                    + "(legacy Blowfish or plain text is no longer supported). "
                    + "The property has to be reset: please enter the value again.");
            propertyObject.remove(propertyString);
            return;
        }

        try {
            propertyObject.setProperty(propertyString, aes.decrypt(stored));
            LOG.debug("Decrypted " + propertyString);
        } catch (GeneralSecurityException | RuntimeException e) {
            LOG.error("Property '" + propertyString + "' could not be decrypted (wrong key or corrupted value). "
                    + "The property has to be reset: please enter the value again. Cause: " + e.getMessage());
            propertyObject.remove(propertyString);
        }
    }

    /** Encrypts a string with the current algorithm. Returns "" on failure. */
    public String cryptEncryptedString(String stringToEncrypt) {
        try {
            return aes.encrypt(stringToEncrypt);
        } catch (Exception e) {
            LOG.error("Issue during encryption", e);
            return "";
        }
    }

}
