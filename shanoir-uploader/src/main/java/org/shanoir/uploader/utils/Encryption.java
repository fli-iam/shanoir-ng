package org.shanoir.uploader.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jboss.seam.util.Hex;
import org.shanoir.uploader.cryptography.BlowfishAlgorithm;

/**
 * This class is used to crypt/uncrypt the user and proxy passwords
 * 
 * @author ifakhfak
 *
 */
public class Encryption {

	private Logger logger = Logger.getLogger(Encryption.class);
	
	private BlowfishAlgorithm blow;
	
	public Encryption(String key) {
		this.blow = new BlowfishAlgorithm(key);
	}

	/**
	 * decrypt password
	 * @param shanoirUploaderFolder
	 * @param propertyObject
	 * @param propertyString
	 * @param propertyFile
	 */
	public void decryptIfEncryptedString(File shanoirUploaderFolder, Properties propertyObject, String propertyString,
			String propertyFile) {
		String unknownString = propertyObject.getProperty(propertyString);
		try {
			logger.debug("Start decrypting string " + propertyString);
			byte[] ibyte = Hex.decodeHex(unknownString.toCharArray());
			byte[] dbyte = blow.decrypt(ibyte);
			String uncryptedString = new String(dbyte);
			propertyObject.setProperty(propertyString, uncryptedString);
			logger.debug("End decrypt encrypted string");
		} catch (RuntimeException e) {
			logger.warn(propertyString + " is not well configured : the string is not crypted");
			logger.debug("Start encrypting string");
			try {
				String encryptedPassword = cryptEncryptedString(unknownString);
				propertyObject.setProperty(propertyString, encryptedPassword);
				// store encrypted pass
				final File propertiesFile = new File(shanoirUploaderFolder + File.separator + propertyFile);
				OutputStream out = new FileOutputStream(propertiesFile);
				propertyObject.store(out, "SHANOIR Server Configuration");
				// get non crypted password
				propertyObject.setProperty(propertyString, unknownString);
				logger.debug("End encrypt String");
				out.close();
			} catch (FileNotFoundException e1) {
				logger.error(e1.getMessage());
			} catch (IOException e1) {
				logger.error(e1.getMessage());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * encrypt password
	 * @param stringToEncrypt
	 * @return
	 */
	public String cryptEncryptedString(String stringToEncrypt) {
		try {
			byte[] ibyte = stringToEncrypt.getBytes();
			byte[] encryptedPasswordByte;
			encryptedPasswordByte = blow.encrypt(ibyte);
			return String.valueOf(Hex.encodeHex(encryptedPasswordByte));
		} catch (Exception e) {
			logger.error("Issue during encryption", e);
			return "";
		}
	}

}
