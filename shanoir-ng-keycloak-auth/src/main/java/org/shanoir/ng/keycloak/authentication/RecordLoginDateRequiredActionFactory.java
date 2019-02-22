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
