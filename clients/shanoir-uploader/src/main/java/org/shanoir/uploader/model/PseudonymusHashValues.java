package org.shanoir.uploader.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PseudonymusHashValues {

	@JsonIgnore
	private Long id;

	private String birthNameHash1;

	private String birthNameHash2;

	private String birthNameHash3;

	private String lastNameHash1;

	private String lastNameHash2;

	private String lastNameHash3;

	private String firstNameHash1;

	private String firstNameHash2;

	private String firstNameHash3;

	private String birthDateHash;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBirthNameHash1() {
		return birthNameHash1;
	}

	public void setBirthNameHash1(String birthNameHash1) {
		this.birthNameHash1 = birthNameHash1;
	}

	public String getBirthNameHash2() {
		return birthNameHash2;
	}

	public void setBirthNameHash2(String birthNameHash2) {
		this.birthNameHash2 = birthNameHash2;
	}

	public String getBirthNameHash3() {
		return birthNameHash3;
	}

	public void setBirthNameHash3(String birthNameHash3) {
		this.birthNameHash3 = birthNameHash3;
	}

	public String getLastNameHash1() {
		return lastNameHash1;
	}

	public void setLastNameHash1(String lastNameHash1) {
		this.lastNameHash1 = lastNameHash1;
	}

	public String getLastNameHash2() {
		return lastNameHash2;
	}

	public void setLastNameHash2(String lastNameHash2) {
		this.lastNameHash2 = lastNameHash2;
	}

	public String getLastNameHash3() {
		return lastNameHash3;
	}

	public void setLastNameHash3(String lastNameHash3) {
		this.lastNameHash3 = lastNameHash3;
	}

	public String getFirstNameHash1() {
		return firstNameHash1;
	}

	public void setFirstNameHash1(String firstNameHash1) {
		this.firstNameHash1 = firstNameHash1;
	}

	public String getFirstNameHash2() {
		return firstNameHash2;
	}

	public void setFirstNameHash2(String firstNameHash2) {
		this.firstNameHash2 = firstNameHash2;
	}

	public String getFirstNameHash3() {
		return firstNameHash3;
	}

	public void setFirstNameHash3(String firstNameHash3) {
		this.firstNameHash3 = firstNameHash3;
	}

	public String getBirthDateHash() {
		return birthDateHash;
	}

	public void setBirthDateHash(String birthDateHash) {
		this.birthDateHash = birthDateHash;
	}

}
