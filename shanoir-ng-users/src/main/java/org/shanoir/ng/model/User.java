package org.shanoir.ng.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.model.hateoas.HalEntity;
import org.shanoir.ng.model.hateoas.Link;
import org.shanoir.ng.model.hateoas.Links;
import org.shanoir.ng.model.validation.Unique;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * User
 */
@Entity
@Table(name = "users")
@JsonPropertyOrder({ "_links", "id", "firstName", "lasName", "username", "email" })
public class User extends HalEntity {

	@Id
	@GeneratedValue
	private Long id;

	private boolean canAccessToDicomAssociation;

	private Date creationDate;

	@NotBlank @Unique
	private String email;

	private Date expirationDate;

	@NotBlank
	private String firstName;

	private boolean isFirstExpirationNotificationSent;

	@NotNull
	private boolean isMedical;

	private boolean isOnDemand;

	private boolean isSecondExpirationNotificationSent;

	private Date lastLogin;

	@NotNull
	private String lastName;

	private String password;

	@NotBlank
	@Column(unique = true)
	@Unique
	private String username;

	private String teamName;

	@ManyToOne
	@NotNull
	private Role role;


	/**
	 *
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(new Link(Links.REL_SELF, "user/" + getId()));
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the canAccessToDicomAssociation
	 */
	public boolean isCanAccessToDicomAssociation() {
		return canAccessToDicomAssociation;
	}

	/**
	 * @param canAccessToDicomAssociation the canAccessToDicomAssociation to set
	 */
	public void setCanAccessToDicomAssociation(boolean canAccessToDicomAssociation) {
		this.canAccessToDicomAssociation = canAccessToDicomAssociation;
	}

	/**
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
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
	 * @param expirationDate the expirationDate to set
	 */
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the isFirstExpirationNotificationSent
	 */
	public boolean isFirstExpirationNotificationSent() {
		return isFirstExpirationNotificationSent;
	}

	/**
	 * @param isFirstExpirationNotificationSent the isFirstExpirationNotificationSent to set
	 */
	public void setFirstExpirationNotificationSent(boolean isFirstExpirationNotificationSent) {
		this.isFirstExpirationNotificationSent = isFirstExpirationNotificationSent;
	}

	/**
	 * @return the isMedical
	 */
	public boolean isMedical() {
		return isMedical;
	}

	/**
	 * @param isMedical the isMedical to set
	 */
	public void setMedical(boolean isMedical) {
		this.isMedical = isMedical;
	}

	/**
	 * @return the isOnDemand
	 */
	public boolean isOnDemand() {
		return isOnDemand;
	}

	/**
	 * @param isOnDemand the isOnDemand to set
	 */
	public void setOnDemand(boolean isOnDemand) {
		this.isOnDemand = isOnDemand;
	}

	/**
	 * @return the isSecondExpirationNotificationSent
	 */
	public boolean isSecondExpirationNotificationSent() {
		return isSecondExpirationNotificationSent;
	}

	/**
	 * @param isSecondExpirationNotificationSent the isSecondExpirationNotificationSent to set
	 */
	public void setSecondExpirationNotificationSent(boolean isSecondExpirationNotificationSent) {
		this.isSecondExpirationNotificationSent = isSecondExpirationNotificationSent;
	}

	/**
	 * @return the lastLogin
	 */
	public Date getLastLogin() {
		return lastLogin;
	}

	/**
	 * @param lastLogin the lastLogin to set
	 */
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the role
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * @param role the role to set
	 */
	public void setRole(Role role) {
		this.role = role;
	}

	/**
	 * @return the teamName
	 */
	public String getTeamName() {
		return teamName;
	}

	/**
	 * @param teamName the teamName to set
	 */
	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

}
