package org.shanoir.ng.dto;

import java.util.Date;
import java.util.List;

/**
 * User DTO.
 *
 * @author msimon
 *
 */
public class UserDTO {

	private String username;

	private List<String> authorities;

	private String token;

	private Date tokenTimeout;

	private String refreshToken;

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(final String username) {
		this.username = username;
	}

	/**
	 * @return the authorities
	 */
	public List<String> getAuthorities() {
		return authorities;
	}

	/**
	 * @param authorities
	 *            the authorities to set
	 */
	public void setAuthorities(final List<String> authorities) {
		this.authorities = authorities;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token
	 *            the token to set
	 */
	public void setToken(final String token) {
		this.token = token;
	}

	/**
	 * @return the tokenTimeout
	 */
	public Date getTokenTimeout() {
		return tokenTimeout;
	}

	/**
	 * @param tokenTimeout
	 *            the tokenTimeout to set
	 */
	public void setTokenTimeout(final Date tokenTimeout) {
		this.tokenTimeout = tokenTimeout;
	}

	/**
	 * @return the refreshToken
	 */
	public String getRefreshToken() {
		return refreshToken;
	}

	/**
	 * @param refreshToken
	 *            the refreshToken to set
	 */
	public void setRefreshToken(final String refreshToken) {
		this.refreshToken = refreshToken;
	}

}