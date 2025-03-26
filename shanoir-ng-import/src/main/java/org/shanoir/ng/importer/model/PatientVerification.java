package org.shanoir.ng.importer.model;

public class PatientVerification {

    private String firstName;

    private String lastName;

    private String birthName;

    private String birthDate;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthName() {
        return birthName;
    }

    public void setBirthName(String birthName) {
        this.birthName = birthName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

	public String[] displayPatientVerification() {
		return new String[] {
				firstName,
				lastName,
				birthName,
				birthDate
		};
	}

}
