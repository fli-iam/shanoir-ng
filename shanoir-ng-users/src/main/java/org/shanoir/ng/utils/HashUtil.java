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
