package org.shanoir.uploader.cryptography;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.dicom.anonymize.Anonymizer;

/**
 * This class is used for password en-/decryption.
 * 
 * @author ifakhfakh
 * @author mkain
 *
 */
public class BlowfishAlgorithm {
	
	public static byte[] symmetricKey = new byte[1000];
	
	private static Logger logger = Logger.getLogger(BlowfishAlgorithm.class);
	
	public BlowfishAlgorithm() {
		symmetricKey =  generateSymmetricKey(ShUpConfig.PRIVATE_KEY);
	}
    
    private  byte[] generateSymmetricKey(String key) {
        try {
            byte[] knumb = key.getBytes();
            byte[] symmetricKey=getRawKey(knumb);
            return symmetricKey;
        }
        catch(Exception e) {
        	logger.error(e.getMessage());
            return null;
        }
    }
    
    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("Blowfish");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        kgen.init(128, sr); // 128, 256 and 448 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] raw;
        raw = skey.getEncoded();
        return raw;
    }
    
    public static byte[] encrypt( byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(symmetricKey, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    public static byte[] decrypt( byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(symmetricKey, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }
    
}