package org.shanoir.ng.keycloak.authentication;

import org.keycloak.Config.Scope;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Action factory used to send request to MS users to update login date after
 * validated login.
 * 
 * @author msimon
 *
 */
public class RecordLoginDateRequiredActionFactory implements RequiredActionFactory {

	public RequiredActionProvider create(final KeycloakSession session) {
		return new RecordLoginDateRequiredActionProvider();
	}

	public void init(final Scope config) {
	}

	public void postInit(final KeycloakSessionFactory factory) {
	}

	public void close() {
	}

	public String getId() {
		return "record-login-date-action";
	}

	public String getDisplayText() {
		return "Record Login Date Action";
	}

}
