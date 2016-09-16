package org.shanoir.ng.dto;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Login DTO.
 * 
 * @author msimon
 *
 */
public class LoginDTO {

    @NotEmpty
    private String login;

    @NotEmpty
    private String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
