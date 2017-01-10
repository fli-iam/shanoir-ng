package org.shanoir.ng.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.shanoir.ng.exception.ShanoirUsersException;
import org.shanoir.ng.exception.error.ErrorModelCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for password hash
 * 
 * @author msimon
 *
 */
public class PasswordUtils {

	/** Logger */
	private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

	/** List of letters for passwords */
	private static final String ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/** List of numbers for passwords */
	private static final String NUMERIC = "0123456789";

	/** List of allowed special characters for passwords */
	private static final String SPECIAL_CHARS = "%$#@";

	/** List of allowed characters for passwords */
	private static final String ALL_CHARS = ALPHA + NUMERIC + SPECIAL_CHARS;

	/** Password minimum length */
	private static final int PASSWORD_MIN_LENGTH = 8;

	/** The algorithm. */
	private static final String ALGORITHM = "SHA";

	/** Password Hash length. */
	private static final int PASSWORD_HASH_LENGTH = 14;

	/**
	 * Check policy of a password.
	 * 
	 * @param password password to check.
	 * @param username user name.
	 * @throws ShanoirUsersException exception thrown if password doesn't match policy
	 */
	public static void checkPasswordPolicy(final String password, final String username) throws ShanoirUsersException {
		if (password != null && password.length() >= PASSWORD_MIN_LENGTH) {
			boolean hasAlpha = false;
			boolean hasNumeric = false;
			boolean hasSpecialChar = false;
			for (int i = 0 ; i < password.length() ; i++) {
				final String c = String.valueOf(password.charAt(i));
				if (ALPHA.contains(c)) {
					hasAlpha = true;
				} else if (NUMERIC.contains(c)) {
					hasNumeric = true;
				} else if (SPECIAL_CHARS.contains(c)) {
					hasSpecialChar = true;
				}
				if (hasAlpha && hasNumeric && hasSpecialChar) {
					return;
				}
			}
		}
		LOG.error("Password does not match policy for user " + username + " : ");
		throw new ShanoirUsersException(ErrorModelCode.PASSWORD_NOT_CORRECT);
	}

	/**
	 * Generates a random password. Password must contain at least 1 letter, 1
	 * number and 1 special character (%,$,#,@) and its minimum size is 8.
	 * 
	 * @return password
	 */
	public static String generatePassword() {
		final Random rnd = new Random();
		final char[] pwd = new char[PASSWORD_MIN_LENGTH];
		int index = 0;
		// 1 letter
		index = getNextIndex(rnd, pwd);
		pwd[index] = ALPHA.charAt(rnd.nextInt(ALPHA.length()));

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
	private static int getNextIndex(final Random rnd, final char[] pwd) {
		int index = rnd.nextInt(PASSWORD_MIN_LENGTH);
		while (pwd[index = rnd.nextInt(PASSWORD_MIN_LENGTH)] != 0) {
			;
		}
		return index;
	}

}
