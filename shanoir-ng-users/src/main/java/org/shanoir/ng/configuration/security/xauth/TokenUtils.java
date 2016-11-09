package org.shanoir.ng.configuration.security.xauth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.codec.Hex;

/**
 * Token utility class.
 * 
 * @author msimon
 *
 */
public class TokenUtils {

    public static final String MAGIC_KEY = "obfuscate";

    /**
     * Create token from user details.
     * 
     * @param userDetails user details.
     * @return token.
     */
    public String createToken(final UserDetails userDetails) {
    	final long expires = System.currentTimeMillis() + 1000L * 60 * 60;
        return userDetails.getUsername() + ":" + expires + ":" + computeSignature(userDetails, expires);
    }

    /**
     * Get user name from token.
     * 
     * @param authToken token.
     * @return name.
     */
    public String getUserNameFromToken(final String authToken) {
        if (null == authToken) {
            return null;
        }
        final String[] parts = authToken.split(":");
        return parts[0];
    }

   /**
     * Validate a token by user details comparison.
     * 
     * @param authToken token to validate.
     * @param userDetails user details.
     * @return true if token is validated.
     */
    public boolean validateToken(final String authToken, final UserDetails userDetails) {
    	final String[] parts = authToken.split(":");
    	final long expires = Long.parseLong(parts[1]);
    	final String signature = parts[2];
    	final String signatureToMatch = computeSignature(userDetails, expires);
        return expires >= System.currentTimeMillis() && signature.equals(signatureToMatch);
    }
    
    /*
     * Compute token signature.
     * 
     * @param userDetails user details.
     * @param expires expiration time.
     * @return signature.
     */
    private String computeSignature(final UserDetails userDetails, final long expires) {
    	final StringBuilder signatureBuilder = new StringBuilder();
        signatureBuilder.append(userDetails.getUsername()).append(":");
        signatureBuilder.append(expires).append(":");
        signatureBuilder.append(userDetails.getPassword()).append(":");
        signatureBuilder.append(TokenUtils.MAGIC_KEY);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }
        return new String(Hex.encode(digest.digest(signatureBuilder.toString().getBytes())));
    }

}
