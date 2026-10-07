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

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Password en-/decryption with AES-256-GCM (authenticated encryption).
 * The key is derived per value from the passphrase with PBKDF2-HMAC-SHA256
 * and a random salt. JDK only, no external libraries.
 *
 * Format: ENC2$<iterations>$<base64url(salt | iv | ciphertext+tag)>
 */
public class AesGcmAlgorithm {

    public static final String PREFIX = "ENC2";
    private static final String SEP = "$";

    private static final int ITERATIONS = 600_000;
    private static final int MIN_ITERATIONS = 10_000;      // sanity bounds against tampered values
    private static final int MAX_ITERATIONS = 10_000_000;
    private static final int SALT_LEN = 16;
    private static final int IV_LEN = 12;
    private static final int KEY_BITS = 256;
    private static final int TAG_BITS = 128;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final char[] passphrase;

    public AesGcmAlgorithm(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Encryption key must not be empty");
        }
        this.passphrase = key.toCharArray();
    }

    /** True if the value was produced by this class. */
    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX + SEP);
    }

    public String encrypt(String clear) throws GeneralSecurityException {
        byte[] salt = randomBytes(SALT_LEN);
        byte[] iv = randomBytes(IV_LEN);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(salt, ITERATIONS), new GCMParameterSpec(TAG_BITS, iv));
        cipher.updateAAD(aad(ITERATIONS));
        byte[] ct = cipher.doFinal(clear.getBytes(StandardCharsets.UTF_8));

        byte[] payload = new byte[SALT_LEN + IV_LEN + ct.length];
        System.arraycopy(salt, 0, payload, 0, SALT_LEN);
        System.arraycopy(iv, 0, payload, SALT_LEN, IV_LEN);
        System.arraycopy(ct, 0, payload, SALT_LEN + IV_LEN, ct.length);

        return PREFIX + SEP + ITERATIONS + SEP
                + Base64.getUrlEncoder().withoutPadding().encodeToString(payload);
    }

    public String decrypt(String encoded) throws GeneralSecurityException {
        if (!isEncrypted(encoded)) {
            throw new GeneralSecurityException("Not an " + PREFIX + " value");
        }
        String[] parts = encoded.split("\\$", 3);
        if (parts.length != 3) {
            throw new GeneralSecurityException("Malformed encrypted value");
        }
        int iterations;
        byte[] payload;
        try {
            iterations = Integer.parseInt(parts[1]);
            payload = Base64.getUrlDecoder().decode(parts[2]);
        } catch (IllegalArgumentException e) {
            throw new GeneralSecurityException("Malformed encrypted value", e);
        }
        if (iterations < MIN_ITERATIONS || iterations > MAX_ITERATIONS
                || payload.length < SALT_LEN + IV_LEN + TAG_BITS / 8) {
            throw new GeneralSecurityException("Malformed encrypted value");
        }

        byte[] salt = Arrays.copyOfRange(payload, 0, SALT_LEN);
        byte[] iv = Arrays.copyOfRange(payload, SALT_LEN, SALT_LEN + IV_LEN);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(salt, iterations), new GCMParameterSpec(TAG_BITS, iv));
        cipher.updateAAD(aad(iterations));
        byte[] clear = cipher.doFinal(payload, SALT_LEN + IV_LEN, payload.length - SALT_LEN - IV_LEN);
        return new String(clear, StandardCharsets.UTF_8);
    }

    private SecretKey deriveKey(byte[] salt, int iterations) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(passphrase, salt, iterations, KEY_BITS);
        try {
            byte[] raw = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            SecretKey key = new SecretKeySpec(raw, "AES");
            Arrays.fill(raw, (byte) 0);
            return key;
        } finally {
            spec.clearPassword();
        }
    }

    /** Binds format version and iteration count to the ciphertext. */
    private static byte[] aad(int iterations) {
        return (PREFIX + SEP + iterations).getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] randomBytes(int n) {
        byte[] b = new byte[n];
        RANDOM.nextBytes(b);
        return b;
    }

}
