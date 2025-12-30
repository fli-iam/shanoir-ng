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

package org.shanoir.uploader.cryptography;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used for password en-/decryption.
 *
 * @author ifakhfakh
 * @author mkain
 *
 */
public class BlowfishAlgorithm {

    private static final Logger LOG = LoggerFactory.getLogger(BlowfishAlgorithm.class);

    private static byte[] symmetricKey = new byte[1000];

    public BlowfishAlgorithm(String key) {
        symmetricKey = generateSymmetricKey(key);
    }

    private byte[] generateSymmetricKey(String key) {
        try {
            byte[] knumb = key.getBytes();
            byte[] symmetricKey = getRawKey(knumb);
            return symmetricKey;
        } catch (Exception e) {
            LOG.error(e.getMessage());
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

    public static byte[] encrypt(byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(symmetricKey, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    public static byte[] decrypt(byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(symmetricKey, "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

}
