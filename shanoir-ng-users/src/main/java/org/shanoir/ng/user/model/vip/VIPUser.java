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

package org.shanoir.ng.user.model.vip;

/**
 * @author Alae ES-SAKI
 *
 * VIP User model to transfer user data
 */
public class VIPUser {

    private String firstName;
    private String lastName;
    private String email;
    private String institution;
    private String password;
    private VIPUserLevel level;

    private CountryCode countryCode;
    private String comments;
    private String[] accountTypes;

    public VIPUser() {
    }

    public VIPUser(String firstName, String lastName, String email, String institution, String password, VIPUserLevel level, CountryCode countryCode, String comments, String[] accountTypes) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.institution = institution;
        this.password = password;
        this.level = level;
        this.countryCode = countryCode;
        this.comments = comments;
        this.accountTypes = accountTypes;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public VIPUserLevel getLevel() {
        return level;
    }

    public void setLevel(VIPUserLevel level) {
        this.level = level;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String[] getAccountTypes() {
        return accountTypes;
    }

    public void setAccountTypes(String[] accountTypes) {
        this.accountTypes = accountTypes;
    }

    public CountryCode getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(CountryCode countryCode) {
        this.countryCode = countryCode;
    }
}
