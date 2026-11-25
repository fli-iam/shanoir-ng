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

package org.shanoir.ng.keycloak.authentication;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;



public class ShanoirNgPostAuthAuthenticator implements Authenticator {

    private static final Logger LOG = Logger.getLogger(ShanoirNgPostAuthAuthenticator.class);

    // List of IP adresses or networks from which admin accounts are
    // allowed to log in.
    // - if null,  then admin accounts can log in from anywhere
    // - if empty, then admin accounts cannot log in at all
    //   (may happen if the given IPs are invalid)
    //
    // This list is initialised from the 'allowed.admin.ips' system
    // property, which may contain a comma-separated list of IP addresses
    // or networks. If empty then admin accounts allowed to log in from
    // anywhere.
    //
    // eg: -Dallowed.admin.ips=192.0.2.1,2001:db8:1::/64
    //
    private static ArrayList<IPAddress> allowedAdminIps = null;
    static {
        final String propName = "allowed.admin.ips";
        final String envVar = System.getProperty(propName);
        if ((envVar != null) && !envVar.isEmpty())
        {
            allowedAdminIps = new ArrayList();

            for (String ip: envVar.split(",")) {
                try {
                    allowedAdminIps.add(new IPAddressString(ip).toAddress());
                }
                catch (AddressStringException e) {
                    LOG.errorv("ignored invalid IP {0} in property {1} ({2})",
                            ip, propName, e);
                }
            }
        }
    }

    // Url for updating the lastLoginDate of user accounts
    //
    // It is initialised from the 'ms.users.url' system property.
    //
    // eg: -Dms.users.url=http://users:9901
    //
    private static final String URL_RECORD_LOGIN_DATE =
            System.getProperty("ms.users.url", "http://users:9901") + "/last_login_date";

    // timeout of the call to ms users, so that a slow or stopped service cannot hold logins
    private static final Duration LOGIN_DATE_TIMEOUT = Duration.ofSeconds(5);

    // one client for all logins (it is thread-safe and reuses its connections)
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(LOGIN_DATE_TIMEOUT)
            .build();

    // date format for the expirationDate user attribute
    // (SimpleDateFormat is not thread-safe: a new one for each login)
    private static final String EXP_DATE_PATTERN = "yyy-MM-dd";

    public void close() {
    }

    public void action(AuthenticationFlowContext context) {
    }

    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    public boolean requiresUser() {
        return true;
    }

    public void authenticate(AuthenticationFlowContext context) {
        final RealmModel realm = context.getRealm();
        final UserModel user = context.getUser();
        final String username = user.getUsername();
        final boolean isAdmin = user.hasRole(realm.getRole("ROLE_ADMIN"));
        final String remoteAddr = context.getConnection().getRemoteAddr();
        final String flowPath = context.getFlowPath();

        LOG.infov("post-auth: flowPath={0} user={1} isAdmin={2} remoteAddr={3}",
                flowPath, username, isAdmin, remoteAddr);

        //
        // ensure the the account has not expired
        //
        String expDateText = null;
        Date expDate;
        try {
            expDateText = user.getAttributeStream("expirationDate").iterator().next();

            // FIXME: the expirationDate in the keycloak db are not consistent:
            // older entries are stored as a timestamp and newer entries are
            // stored as 'YYYY-MM-DD'.
            try {
                expDate = new Date(Long.parseUnsignedLong(expDateText));
            }
            catch (NumberFormatException e) {
                expDate = new SimpleDateFormat(EXP_DATE_PATTERN).parse(expDateText);
            }
        }
        catch (java.util.NoSuchElementException e) {
            // no expiration date
            expDate = null;
        }
        catch (java.text.ParseException e) {
            LOG.errorv("denied login for user {0} with a malformatted expiration date: {1}",
                    username, expDateText);
            context.failure(AuthenticationFlowError.INTERNAL_ERROR);
            return;
        }
        if (expDate != null && expDate.before(new Date())) {
            LOG.infov("denied login for user {0} with an expired account", username);
            user.setEnabled(false);
            context.failure(AuthenticationFlowError.USER_DISABLED);
            return;
        }

        //
        // ensure that admin accounts are accessed from an authorized IP
        //
        if (isAdmin && (allowedAdminIps != null)) {
            boolean ok = false;

            final IPAddress remoteIp = new IPAddressString(remoteAddr).getAddress();
            if (remoteIp != null) {
                for (IPAddress allowedIp: allowedAdminIps) {
                    if (allowedIp.contains(remoteIp)) {
                        ok = true;
                        break;
                    }
                }
            }
            if (!ok) {
                LOG.warnv("denied admin login by {0} from unauthorized address {1}",
                        username, remoteAddr);
                context.failure(AuthenticationFlowError.INVALID_USER);
                return;
            }
        }

        //
        // update last login date
        // (runs in every authentication flow except 'reset-credentials')
        //
        if (!"reset-credentials".equals(flowPath)) {

            try {
                final HttpRequest request = HttpRequest.newBuilder(URI.create(URL_RECORD_LOGIN_DATE))
                        .timeout(LOGIN_DATE_TIMEOUT)
                        .header("Content-type", "application/json")
                        // request body: the username
                        .POST(HttpRequest.BodyPublishers.ofString(username, StandardCharsets.UTF_8))
                        .build();

                final HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
                if (response.statusCode() >= 300) {
                    LOG.errorv("Unable to update last login date for {0} (POST {1} returned HTTP {2})",
                            username, URL_RECORD_LOGIN_DATE, response.statusCode());
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                LOG.errorv("Interrupted while updating last login date for {0}", username);
            } catch (final Exception e) {
                LOG.errorv("Unable to update last login date for {0} (POST {1} returned: {2})",
                        username, URL_RECORD_LOGIN_DATE, e);
            }
        }

        context.success();
    }
}
