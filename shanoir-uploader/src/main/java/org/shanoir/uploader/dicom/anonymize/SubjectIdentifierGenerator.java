package org.shanoir.uploader.dicom.anonymize;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;
import org.shanoir.uploader.action.DicomDataTransferObject;
import org.shanoir.util.HashUtil;
import org.shanoir.util.ShanoirConstants;

public class SubjectIdentifierGenerator implements ISubjectIdentifierGenerator {

	private static Logger logger = Logger.getLogger(SubjectIdentifierGenerator.class);

	private static final String UTF_8 = "UTF-8";
	private static final String SHA_256 = "SHA-256";
	
	public String generateSubjectIdentifierWithPseudonymus(DicomDataTransferObject dicomData) {
		try {
			MessageDigest md = MessageDigest.getInstance(SHA_256);
			final String subjectIdentifierSeed = dicomData.getFirstNameHash1() + dicomData.getBirthNameHash1() + dicomData.getBirthDateHash();
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
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public String generateSubjectIdentifier(DicomDataTransferObject dicomData) {
		final String subjectIdentifierSeed = dicomData.getFirstName() + dicomData.getLastName() + dicomData.getBirthDate();
		return HashUtil.getHash(subjectIdentifierSeed, ShanoirConstants.PASSWORD_HASH_LENGTH);
	}

}
