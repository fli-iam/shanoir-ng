package org.shanoir.ng.model;

import java.util.Date;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.model.hateoas.HalEntity;
import org.shanoir.ng.model.hateoas.Link;
import org.shanoir.ng.model.hateoas.Links;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.annotations.ApiModelProperty;

/**
 * User
 */
@Entity
@Table(name = "users")
@JsonPropertyOrder({ "_links", "id", "firstName", "lasName", "username", "email" })
public class User extends HalEntity {

	@JsonProperty("id")
	@Id
	private Long id = null;

	@JsonProperty("username")
	@NotBlank
	private String username = null;

	@JsonProperty("password")
	private String password = null;

	@JsonProperty("firstName")
	@NotBlank
	private String firstName = null;

	@JsonProperty("lastName")
	@NotBlank
	private String lastName = null;

	@JsonProperty("email")
	@NotBlank
	private String email = null;

	@JsonProperty("teamName")
	private String teamName = null;

	@JsonProperty("canImportFromPACS")
	private Boolean canImportFromPacs = null;

	@JsonProperty("creationDate")
	@NotNull
	private Date creationDate = null;

	@JsonProperty("expirationDate")
	private Date expirationDate = null;

	@JsonProperty("lastLogin")
	private Date lastLogin = null;

	@JsonProperty("canAccessToDicomAssociation")
	private Boolean canAccessToDicomAssociation = null;

	@JsonProperty("motivation")
	private String motivation = null;

	@JsonProperty("role")
	@ManyToOne @JoinColumn(name="ROLE_ID")
	@NotNull
	private Role role = null;


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
	 * Get canImportFromPACS
	 *
	 * @return canImportFromPACS
	 **/
	@ApiModelProperty(value = "")
	public Boolean getCanImportFromPACS() {
		return canImportFromPacs;
	}

	public void setCanImportFromPACS(Boolean canImportFromPACS) {
		this.canImportFromPacs = canImportFromPACS;
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

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		User user = (User) o;
		if (this.id != null && user.id != null) {
			return Objects.equals(this.id, user.id);
		}
		return Objects.equals(this.username, user.username) && Objects.equals(this.password, user.password)
				&& Objects.equals(this.firstName, user.firstName) && Objects.equals(this.lastName, user.lastName)
				&& Objects.equals(this.email, user.email) && Objects.equals(this.teamName, user.teamName)
				&& Objects.equals(this.canImportFromPacs, user.canImportFromPacs)
				&& Objects.equals(this.creationDate, user.creationDate)
				&& Objects.equals(this.expirationDate, user.expirationDate)
				&& Objects.equals(this.lastLogin, user.lastLogin)
				&& Objects.equals(this.canAccessToDicomAssociation, user.canAccessToDicomAssociation)
				&& Objects.equals(this.motivation, user.motivation) && Objects.equals(this.role, user.role);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, username, password, firstName, lastName, email, teamName, canImportFromPacs,
				creationDate, expirationDate, lastLogin, canAccessToDicomAssociation, motivation, role);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class User {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    username: ").append(toIndentedString(username)).append("\n");
		sb.append("    password: ").append(toIndentedString(password)).append("\n");
		sb.append("    firstName: ").append(toIndentedString(firstName)).append("\n");
		sb.append("    lastName: ").append(toIndentedString(lastName)).append("\n");
		sb.append("    email: ").append(toIndentedString(email)).append("\n");
		sb.append("    teamName: ").append(toIndentedString(teamName)).append("\n");
		sb.append("    canImportFromPACS: ").append(toIndentedString(canImportFromPacs)).append("\n");
		sb.append("    creationDate: ").append(toIndentedString(creationDate)).append("\n");
		sb.append("    expirationDate: ").append(toIndentedString(expirationDate)).append("\n");
		sb.append("    lastLogin: ").append(toIndentedString(lastLogin)).append("\n");
		sb.append("    canAccessToDicomAssociation: ").append(toIndentedString(canAccessToDicomAssociation))
				.append("\n");
		sb.append("    motivation: ").append(toIndentedString(motivation)).append("\n");
		sb.append("    role: ").append(toIndentedString(role)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
