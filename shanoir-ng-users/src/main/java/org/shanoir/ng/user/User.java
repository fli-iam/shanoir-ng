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

package org.shanoir.ng.user;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.accountrequest.AccountRequestInfo;
import org.shanoir.ng.role.Role;
import org.shanoir.ng.shared.dateTime.LocalDateAnnotations;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.EditableOnlyBy;
import org.shanoir.ng.shared.validation.ExtensionWithMotivation;
import org.shanoir.ng.shared.validation.Unique;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * User
 */
@Entity
@Table(name = "users")
@JsonPropertyOrder({ "_links", "id", "firstName", "lastName", "username", "email" })
@ExtensionWithMotivation
public class User extends HalEntity implements UserDetails {

	/**
	 * UID
	 */
	private static final long serialVersionUID = -5277815428510293236L;

	private boolean accountRequestDemand;

	@OneToOne(orphanRemoval = true)
	private AccountRequestInfo accountRequestInfo;

	private boolean canAccessToDicomAssociation;

	@LocalDateAnnotations
	private LocalDate creationDate;

	@NotBlank
	@Column(unique = true)
	@Unique
	private String email;
	
	@EditableOnlyBy(roles = { "ROLE_ADMIN" })
	@LocalDateAnnotations
	private LocalDate expirationDate;

	@Embedded
	private ExtensionRequestInfo extensionRequestInfo;

	private boolean extensionRequestDemand;
	
	private boolean firstExpirationNotificationSent;
	
	@NotBlank
	private String firstName;

	private String keycloakId;

	@LocalDateAnnotations
	private LocalDate lastLogin;

	@NotNull
	private String lastName;

	@ManyToOne
	@NotNull
	@EditableOnlyBy(roles = { "ROLE_ADMIN" })
	private Role role;
	
	private boolean secondExpirationNotificationSent;

	@NotBlank
	@Column(unique = true)
	@Unique
	private String username;

	private String teamName;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "user/" + getId());
	}

	/**
	 * @return the accountRequestDemand
	 */
	public boolean isAccountRequestDemand() {
		return accountRequestDemand;
	}

	/**
	 * @param accountRequestDemand
	 *            the accountRequestDemand to set
	 */
	public void setAccountRequestDemand(final boolean accountRequestDemand) {
		this.accountRequestDemand = accountRequestDemand;
	}

	/**
	 * @return the accountRequestInfo
	 */
	public AccountRequestInfo getAccountRequestInfo() {
		return accountRequestInfo;
	}

	/**
	 * @param accountRequestInfo
	 *            the accountRequestInfo to set
	 */
	public void setAccountRequestInfo(final AccountRequestInfo accountRequestInfo) {
		this.accountRequestInfo = accountRequestInfo;
	}

	/**
	 * @return the canAccessToDicomAssociation
	 */
	public boolean isCanAccessToDicomAssociation() {
		return canAccessToDicomAssociation;
	}

	/**
	 * @param canAccessToDicomAssociation
	 *            the canAccessToDicomAssociation to set
	 */
	public void setCanAccessToDicomAssociation(final boolean canAccessToDicomAssociation) {
		this.canAccessToDicomAssociation = canAccessToDicomAssociation;
	}

	/**
	 * @return the creationDate
	 */
	public LocalDate getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate
	 *            the creationDate to set
	 */
	public void setCreationDate(final LocalDate creationDate) {
		this.creationDate = creationDate;
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
	public void setEmail(final String email) {
		this.email = email;
	}

	/**
	 * @return the expirationDate
	 */
	public LocalDate getExpirationDate() {
		return expirationDate;
	}

	/**
	 * @param expirationDate
	 *            the expirationDate to set
	 */
	public void setExpirationDate(final LocalDate expirationDate) {
		this.expirationDate = expirationDate;
	}

	/**
	 * @return the extensionRequestInfo
	 */
	public ExtensionRequestInfo getExtensionRequestInfo() {
		return extensionRequestInfo;
	}

	/**
	 * @param extensionRequestInfo the extensionRequestInfo to set
	 */
	public void setExtensionRequestInfo(ExtensionRequestInfo extensionRequestInfo) {
		this.extensionRequestInfo = extensionRequestInfo;
	}

	/**
	 * @return the extensionRequestDemand
	 */
	public boolean isExtensionRequestDemand() {
		return extensionRequestDemand;
	}
	
	/**
	 * @param extensionRequestDemand
	 *            the extensionRequestDemand to set
	 */
	public void setExtensionRequestDemand(boolean extensionRequestDemand) {
		this.extensionRequestDemand = extensionRequestDemand;
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
	public void setFirstExpirationNotificationSent(final boolean firstExpirationNotificationSent) {
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
	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the keycloakId
	 */
	@JsonIgnore
	public String getKeycloakId() {
		return keycloakId;
	}

	/**
	 * @param keycloakId
	 *            the keycloakId to set
	 */
	public void setKeycloakId(String keycloakId) {
		this.keycloakId = keycloakId;
	}

	/**
	 * @return the lastLogin
	 */
	public LocalDate getLastLogin() {
		return lastLogin;
	}

	/**
	 * @param lastLogin
	 *            the lastLogin to set
	 */
	public void setLastLogin(final LocalDate lastLogin) {
		this.lastLogin = lastLogin;
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
	public void setLastName(final String lastName) {
		this.lastName = lastName;
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
	public void setRole(final Role role) {
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
	public void setSecondExpirationNotificationSent(final boolean secondExpirationNotificationSent) {
		this.secondExpirationNotificationSent = secondExpirationNotificationSent;
	}

	/**
	 * @return the teamName
	 */
	public String getTeamName() {
		return teamName;
	}

	/**
	 * @param teamName
	 *            the teamName to set
	 */
	public void setTeamName(final String teamName) {
		this.teamName = teamName;
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
	public void setUsername(final String username) {
		this.username = username;
	}

	@Override
	@JsonIgnore
	public Collection<GrantedAuthority> getAuthorities() {
		return Arrays.asList(role);
	}

	@Override
	@JsonIgnore
	public String getPassword() {
		return null;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	@JsonIgnore
	public boolean isEnabled() {
		return expirationDate == null || expirationDate.isAfter(LocalDate.now());
	}

}
