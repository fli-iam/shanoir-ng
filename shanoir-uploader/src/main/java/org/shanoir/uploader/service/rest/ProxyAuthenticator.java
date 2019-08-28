package org.shanoir.uploader.service.rest;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * 
 * This classed is needed in order to set the proxy.
 * 
 * @author atouboul
 *
 */
public class ProxyAuthenticator extends Authenticator {

    private String user, password;

    public ProxyAuthenticator(String user, String password) {
        this.user = user;
        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password.toCharArray());
    }
}
