package org.shanoir.ng.keycloak.authentication;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.AddressStringException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;



public class ShanoirNgPostAuthAuthenticator implements Authenticator {

	private static final Logger logger = Logger.getLogger(ShanoirNgPostAuthAuthenticator.class);

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
		final String env_var = System.getProperty(propName);
		if ((env_var != null) && !env_var.isEmpty())
		{
			allowedAdminIps = new ArrayList();

			for (String ip: env_var.split(",")) {
				try {
					allowedAdminIps.add(new IPAddressString(ip).toAddress());
				}
				catch (AddressStringException e) {
					logger.errorv("ignored invalid IP {0} in property {1} ({2})",
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
	private static final String urlRecordLoginDate =
		System.getProperty("ms.users.url", "http://users:9901") + "/last_login_date";

	// date format for the expirationDate user attribute
	private static final SimpleDateFormat expDateFormat = new SimpleDateFormat("yyy-MM-dd");

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

		logger.infov("post-auth: flowPath={0} user={1} isAdmin={2} remoteAddr={3}",
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
				expDate = expDateFormat.parse(expDateText);
			}
		}
		catch (java.util.NoSuchElementException e) {
			// no expiration date
			expDate = null;
		}
		catch (java.text.ParseException e) {
			logger.errorv("denied login for user {0} with a malformatted expiration date: {1}",
					username, expDateText);
			context.failure(AuthenticationFlowError.INTERNAL_ERROR);
			return;
		}
		if (expDate != null && expDate.before(new Date())) {
			logger.infov("denied login for user {0} with an expired	account", username);
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
				logger.warnv("denied admin login by {0} from unauthorized address {1}",
						username, remoteAddr);
				context.failure(AuthenticationFlowError.INVALID_USER);
				return;
			}
		}

		//
		// update last login date
		// (runs in every authentication flow except 'reset-credentials')
		//
		if (flowPath != "reset-credentials") {

			final HttpClient client = HttpClientBuilder.create().build();
			try {
				final HttpPost request = new HttpPost(urlRecordLoginDate);
				request.addHeader("Content-type", "application/json");

				// request body
				request.setEntity(new StringEntity(user.getUsername()));

				client.execute(request);
			} catch (final Exception e) {
				logger.errorv("Unable to update last login date for {0} (POST {1} returned: {2})",
						username, urlRecordLoginDate, e);
			}
		}

		context.success();
	}
}
