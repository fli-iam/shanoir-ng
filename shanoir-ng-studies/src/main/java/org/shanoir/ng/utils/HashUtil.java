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

package org.shanoir.ng.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for password hash
 * 
 * @author msimon
 *
 */
public class HashUtil {

	/** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

	/** The algorithm. */
	private static final String ALGORITHM = "SHA";

	/** Password Hash length. */
	private static final int PASSWORD_HASH_LENGTH = 14;

	/**
	 * Get the hash for the given password.
	 * 
	 * @param password
	 *            password to hash.
	 * @param hashLength
	 *            the hash length.
	 * 
	 * @return the hash for the given password.
	 */
	public static String getHash(final String password) {
		try {
			int hashInt = -1;
			final MessageDigest msgDigest = MessageDigest.getInstance(ALGORITHM);
			msgDigest.update(password.getBytes());
			final byte[] hash = msgDigest.digest();

			final StringBuilder hex = new StringBuilder();
			for (int i = 0; i < hash.length; i++) {
				hashInt = hash[i] & 0xFF;

				if (hashInt < 16) {
					hex.append("0");
				}

				hex.append(Integer.toString(hashInt, 16).toUpperCase());
				hex.append(Byte.toString(hash[i]));
			}

			return hex.substring(0, PASSWORD_HASH_LENGTH);
		}
		catch (final NoSuchAlgorithmException exc) {
			LOG.error("Cannot continue to hash: ", exc);
			return null;
		}
	}

}
