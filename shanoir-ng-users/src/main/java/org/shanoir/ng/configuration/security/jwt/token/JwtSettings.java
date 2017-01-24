package org.shanoir.ng.configuration.security.jwt.token;

import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT settings.
 * 
 * @author msimon
 *
 */
@Configuration
@ConfigurationProperties(prefix = "shanoir.security.jwt")
public class JwtSettings {

	public static final String REFRESH_TOKEN = "REFRESH_TOKEN";

	public static final String SCOPES = "scopes";

	public static final String USER_ID = "userId";

	/**
	 * {@link JwtToken} can be refreshed during this timeframe.
	 */
	private Integer refreshTokenExpTime;

	/**
	 * {@link JwtToken} will expire after this time.
	 */
	private Integer tokenExpirationTime;

	/**
	 * Token issuer.
	 */
	private String tokenIssuer;

	/**
	 * Key is used to sign {@link JwtToken}.
	 */
	private String tokenSigningKey;

	/**
	 * @return the refreshTokenExpTime
	 */
	public Integer getRefreshTokenExpTime() {
		return refreshTokenExpTime;
	}

	/**
	 * @param refreshTokenExpTime
	 *            the refreshTokenExpTime to set
	 */
	public void setRefreshTokenExpTime(Integer refreshTokenExpTime) {
		this.refreshTokenExpTime = refreshTokenExpTime;
	}

	/**
	 * @return the tokenExpirationTime
	 */
	public Integer getTokenExpirationTime() {
		return tokenExpirationTime;
	}

	/**
	 * @param tokenExpirationTime
	 *            the tokenExpirationTime to set
	 */
	public void setTokenExpirationTime(Integer tokenExpirationTime) {
		this.tokenExpirationTime = tokenExpirationTime;
	}

	/**
	 * @return the tokenIssuer
	 */
	public String getTokenIssuer() {
		return tokenIssuer;
	}

	/**
	 * @param tokenIssuer
	 *            the tokenIssuer to set
	 */
	public void setTokenIssuer(String tokenIssuer) {
		this.tokenIssuer = tokenIssuer;
	}

	/**
	 * @return the tokenSigningKey
	 */
	public String getTokenSigningKey() {
		return tokenSigningKey;
	}

	/**
	 * @param tokenSigningKey
	 *            the tokenSigningKey to set
	 */
	public void setTokenSigningKey(String tokenSigningKey) {
		this.tokenSigningKey = tokenSigningKey;
	}

	/**
	 * Get token expiration date. Current date + token expiration time.
	 * 
	 * @return token expiration date
	 */
	public Date getTokenExpirationDate() {
		DateTime currentTime = new DateTime();
		return currentTime.plusMinutes(tokenExpirationTime).toDate();
	}

}
