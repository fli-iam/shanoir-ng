package org.shanoir.ng.exchange.imports.subject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.springframework.stereotype.Component;

/**
 * This component calculates subject identifiers, for more information see:
 * https://github.com/fli-iam/shanoir-ng/wiki/DICOM-Import-Single-Subject-Process-Steps
 * Import Step A.4
 * 
 * @author mkain
 *
 */
@Component
public class IdentifierCalculator {

	private static final String SHA_256 = "SHA-256";
	
	private static final String SHA = "SHA";

	private static final String UTF_8 = "UTF-8";
	
	private static final int HASH_LENGTH = 14;
	
	/**
	 * This method calculates a subject identifier on using three hash values.
	 * This subject identifier is currently used by OFSEP.
	 * 
	 * @param firstNameHash
	 * @param birthNameHash
	 * @param birthDateHash
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	public String calculateIdentifierWithHashs(final String firstNameHash, final String birthNameHash, final String birthDateHash) throws UnsupportedEncodingException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(SHA_256);
		final String subjectIdentifierSeed = firstNameHash + birthNameHash + birthDateHash;
		md.update(subjectIdentifierSeed.getBytes(UTF_8));
		byte[] digestDoubleSHA256 = md.digest();
		StringBuffer subjectIdentifier = new StringBuffer();
		for (int i = 0; i < digestDoubleSHA256.length; i++) {
			String hex = Integer.toHexString(0xff & digestDoubleSHA256[i]);
			if (hex.length() == 1)
				subjectIdentifier.append('0');
			subjectIdentifier.append(hex);
		}
		return subjectIdentifier.toString();
	}

	/**
	 * This method calculates a subject identifier on using three direct values.
	 * This subject identifier is currently used by Neurinfo.
	 * 
	 * @param firstName
	 * @param lastName
	 * @param birthDate
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public String calculateIdentifier(final String firstName, final String lastName, final Date birthDate) throws NoSuchAlgorithmException {
		final String subjectIdentifierSeed = firstName + lastName + birthDate;
		String hex = "";
		int hashInt = -1;
		final MessageDigest msgDigest = MessageDigest.getInstance(SHA);
		msgDigest.update(subjectIdentifierSeed.getBytes());
		byte[] hash = msgDigest.digest();
		for (int i = 0; i < hash.length; i++) {
			hashInt = hash[i] & 0xFF;
			if (hashInt < 16) {
				hex += "0";
			}
			hex += (Integer.toString(hashInt, 16).toUpperCase() + "");
			hex = hex + Byte.toString(hash[i]);
		}
		hex = hex.substring(0, HASH_LENGTH);
		return hex;
	}

}