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
import java.security.SecureRandom;

import org.shanoir.ng.shared.exception.ShanoirUsersException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for password hash
 * 
 * @author msimon
 *
 */
public final class PasswordUtils {

	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(PasswordUtils.class);

	/** List of letters for passwords */
	private static final String LOWERCASE_ALPHA = "abcdefghijklmnopqrstuvwxyz";

	/** List of letters for passwords */
	private static final String UPPERCASE_ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/** List of numbers for passwords */
	private static final String NUMERIC = "0123456789";

	/** List of allowed special characters for passwords */
	private static final String SPECIAL_CHARS = "%$#@";

	/** List of allowed characters for passwords */
	private static final String ALL_CHARS = LOWERCASE_ALPHA + UPPERCASE_ALPHA + NUMERIC + SPECIAL_CHARS;

	/** Password minimum length */
	private static final int PASSWORD_MIN_LENGTH = 8;

	/** The algorithm. */
	private static final String ALGORITHM = "SHA";

	/** Password Hash length. */
	private static final int PASSWORD_HASH_LENGTH = 14;

	/**
	 * Private constructor
	 */
	private PasswordUtils() {
	}
	
	/**
	 * Check policy of a password.
	 * 
	 * @param password password to check.
	 * @param username user name.
	 * @throws ShanoirUsersException exception thrown if password doesn't match policy
	 */
	public static boolean checkPasswordPolicy(final String password) {
		// Shanoir NG password check
		if (password != null && password.length() >= PASSWORD_MIN_LENGTH) {
			boolean hasLowerCaseAlpha = false;
			boolean hasUpperCaseAlpha = false;
			boolean hasNumeric = false;
			boolean hasSpecialChar = false;
			for (int i = 0 ; i < password.length() ; i++) {
				final String c = String.valueOf(password.charAt(i));
				if (LOWERCASE_ALPHA.contains(c)) {
					hasLowerCaseAlpha = true;
				} else if (UPPERCASE_ALPHA.contains(c)) {
					hasUpperCaseAlpha = true;
				} else if (NUMERIC.contains(c)) {
					hasNumeric = true;
				} else if (SPECIAL_CHARS.contains(c)) {
					hasSpecialChar = true;
				}
				if (hasLowerCaseAlpha && hasUpperCaseAlpha && hasNumeric && hasSpecialChar) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Generates a random password. Password must contain at least 1 letter, 1
	 * number and 1 special character (%,$,#,@) and its minimum size is 8.
	 * 
	 * @return password
	 */
	public static String generatePassword() {
		final SecureRandom rnd = new SecureRandom();
		final char[] pwd = new char[PASSWORD_MIN_LENGTH];
		
		// 1 lower case letter
		int index = getNextIndex(rnd, pwd);
		pwd[index] = LOWERCASE_ALPHA.charAt(rnd.nextInt(LOWERCASE_ALPHA.length()));

		// 1 upper case letter
		index = getNextIndex(rnd, pwd);
		pwd[index] = UPPERCASE_ALPHA.charAt(rnd.nextInt(UPPERCASE_ALPHA.length()));

		// 1 number
		index = getNextIndex(rnd, pwd);
		pwd[index] = NUMERIC.charAt(rnd.nextInt(NUMERIC.length()));

		// 1 special char
		index = getNextIndex(rnd, pwd);
		pwd[index] = SPECIAL_CHARS.charAt(rnd.nextInt(SPECIAL_CHARS.length()));

		for (int i = 0; i < PASSWORD_MIN_LENGTH; i++) {
			if (pwd[i] == 0) {
				pwd[i] = ALL_CHARS.charAt(rnd.nextInt(ALL_CHARS.length()));
			}
		}
		return new String(pwd);
	}

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
			int hashInt;
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
		} catch (final NoSuchAlgorithmException exc) {
			LOG.error("Cannot continue to hash: ", exc);
			return null;
		}
	}

	/*
	 * Get next password index to fill.
	 * 
	 * @param rnd random object.
	 * 
	 * @param pwd password.
	 * 
	 * @return password index.
	 */
	private static int getNextIndex(final SecureRandom rnd, final char[] pwd) {
		int index = rnd.nextInt(PASSWORD_MIN_LENGTH);
		while (pwd[index = rnd.nextInt(PASSWORD_MIN_LENGTH)] != 0) {
		}
		return index;
	}

}
