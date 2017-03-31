package org.shanoir.ng.user;

import java.util.Date;

import org.shanoir.ng.role.Role;

/**
 * Shanoir old user DTO. Used to send messages to Shanoir old with RabbitMQ.
 * 
 * @author msimon
 *
 */
public class ShanoirOldUserDTO {

	private Long id;
	private Boolean canAccessToDicomAssociation;
	private Date createdOn;
	private String email;
	private Date expirationDate;
	private boolean firstExpirationNotificationSent;
	private String firstName;
	private Date lastLoginOn;
	private String lastName;
	private String passwordHash;
	private Role role;
	private boolean secondExpirationNotificationSent;
	private String username;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the canAccessToDicomAssociation
	 */
	public Boolean getCanAccessToDicomAssociation() {
		return canAccessToDicomAssociation;
	}

	/**
	 * @param canAccessToDicomAssociation
	 *            the canAccessToDicomAssociation to set
	 */
	public void setCanAccessToDicomAssociation(Boolean canAccessToDicomAssociation) {
		this.canAccessToDicomAssociation = canAccessToDicomAssociation;
	}

	/**
	 * @return the createdOn
	 */
	public Date getCreatedOn() {
		return createdOn;
	}

	/**
	 * @param createdOn
	 *            the createdOn to set
	 */
	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the expirationDate
	 */
	public Date getExpirationDate() {
		return expirationDate;
	}

	/**
	 * @param expirationDate
	 *            the expirationDate to set
	 */
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	/**
	 * @return the firstExpirationNotificationSent
	 */
	public boolean isFirstExpirationNotificationSent() {
		return firstExpirationNotificationSent;
	}

	/**
	 * @param firstExpirationNotificationSent
	 *            the firstExpirationNotificationSent to set
	 */
	public void setFirstExpirationNotificationSent(boolean firstExpirationNotificationSent) {
		this.firstExpirationNotificationSent = firstExpirationNotificationSent;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName
	 *            the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastLoginOn
	 */
	public Date getLastLoginOn() {
		return lastLoginOn;
	}

	/**
	 * @param lastLoginOn
	 *            the lastLoginOn to set
	 */
	public void setLastLoginOn(Date lastLoginOn) {
		this.lastLoginOn = lastLoginOn;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName
	 *            the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the passwordHash
	 */
	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * @param passwordHash
	 *            the passwordHash to set
	 */
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	/**
	 * @return the role
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * @param role
	 *            the role to set
	 */
	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * @return the secondExpirationNotificationSent
	 */
	public boolean isSecondExpirationNotificationSent() {
		return secondExpirationNotificationSent;
	}

	/**
	 * @param secondExpirationNotificationSent
	 *            the secondExpirationNotificationSent to set
	 */
	public void setSecondExpirationNotificationSent(boolean secondExpirationNotificationSent) {
		this.secondExpirationNotificationSent = secondExpirationNotificationSent;
	}

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
	public void setUsername(String username) {
		this.username = username;
	}

}
