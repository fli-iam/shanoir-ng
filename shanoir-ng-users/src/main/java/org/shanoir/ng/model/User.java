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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

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

	@NotBlank
	@Column(unique = true)
	private String username;

	private String password;

	@NotBlank
	private String firstName;

	@NotBlank
	private String lastName;

	@NotBlank
	private String email;

	private String teamName;

	@NotNull
	private Boolean isMedical;

	@NotNull
	private Date creationDate;

	private Date expirationDate;

	private Date lastLogin;

	@NotBlank
	private Boolean canAccessToDicomAssociation;

	private String motivation;
	
	@Column(columnDefinition = "boolean default false")
	private Boolean isFirstExpirationNotificationSent;
	
	private Boolean isSecondExpirationNotificationSent;
	
	@Column(columnDefinition = "boolean default false")
	private Boolean isOnDemand;

	@ManyToOne
	@NotNull
	private Role role;

	/**
	 *
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(new Link(Links.REL_SELF, "user/" + getId()));
		//this.addLink(new Link(Links.REL_NEXT, new HRef("user/" + (getId() + 1))));
	}

	/**
	 * Get id
	 *
	 * @return id
	 **/
	@ApiModelProperty(value = "")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	/**
	 * Get username
	 *
	 * @return username
	 **/
	@ApiModelProperty(required = true, value = "")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Get password
	 *
	 * @return password
	 **/
	@ApiModelProperty(value = "")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	/**
	 * Get firstName
	 *
	 * @return firstName
	 **/
	@ApiModelProperty(value = "")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}


	/**
	 * Get lastName
	 *
	 * @return lastName
	 **/
	@ApiModelProperty(value = "")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}


	/**
	 * Get email
	 *
	 * @return email
	 **/
	@ApiModelProperty(required = true, value = "")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


	/**
	 * Get teamName
	 *
	 * @return teamName
	 **/
	@ApiModelProperty(value = "")
	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	/**
	 * Get isMedical
	 *
	 * @return isMedical
	 **/
	@ApiModelProperty(value = "")
	public Boolean getIsMedical() {
		return isMedical;
	}

	public void setIsMedical(Boolean isMedical) {
		this.isMedical = isMedical;
	}

	/**
	 * Get creationDate
	 *
	 * @return creationDate
	 **/
	@ApiModelProperty(value = "")
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}


	/**
	 * Get expirationDate
	 *
	 * @return expirationDate
	 **/
	@ApiModelProperty(value = "")
	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}


	/**
	 * Get lastLogin
	 *
	 * @return lastLogin
	 **/
	@ApiModelProperty(value = "")
	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}


	/**
	 * Get canAccessToDicomAssociation
	 *
	 * @return canAccessToDicomAssociation
	 **/
	@ApiModelProperty(value = "")
	public Boolean getCanAccessToDicomAssociation() {
		return canAccessToDicomAssociation;
	}

	public void setCanAccessToDicomAssociation(Boolean canAccessToDicomAssociation) {
		this.canAccessToDicomAssociation = canAccessToDicomAssociation;
	}


	/**
	 * Get motivation
	 *
	 * @return motivation
	 **/
	@ApiModelProperty(value = "")
	public String getMotivation() {
		return motivation;
	}

	public void setMotivation(String motivation) {
		this.motivation = motivation;
	}

	/**
	 * Get isFirstExpirationNotificationSent
	 *
	 * @return isFirstExpirationNotificationSent
	 **/
	@ApiModelProperty(value = "")
	public Boolean getIsFirstExpirationNotificationSent() {
		return isFirstExpirationNotificationSent;
	}

	public void setIsFirstExpirationNotificationSent(Boolean isFirstExpirationNotificationSent) {
		this.isFirstExpirationNotificationSent = isFirstExpirationNotificationSent;
	}

	/**
	 * Get isSecondExpirationNotificationSent
	 *
	 * @return isSecondExpirationNotificationSent
	 **/
	@ApiModelProperty(value = "")
	public Boolean getIsSecondExpirationNotificationSent() {
		return isSecondExpirationNotificationSent;
	}

	public void setIsSecondExpirationNotificationSent(Boolean isSecondExpirationNotificationSent) {
		this.isSecondExpirationNotificationSent = isSecondExpirationNotificationSent;
	}

	/**
	 * Get isOnDemand
	 *
	 * @return isOnDemand
	 **/
	@ApiModelProperty(value = "")
	public Boolean getIsOnDemand() {
		return isOnDemand;
	}

	public void setIsOnDemand(Boolean isOnDemand) {
		this.isOnDemand = isOnDemand;
	}

	/**
	 * Get role
	 *
	 * @return role
	 **/
	@ApiModelProperty(value = "")
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

}
