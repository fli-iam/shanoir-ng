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

import java.util.ArrayList;
import java.util.List;

import org.keycloak.Config.Scope;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;



public class ShanoirNgPostAuthAuthenticatorFactory implements AuthenticatorFactory {

    public Authenticator create(final KeycloakSession session) {
        return new ShanoirNgPostAuthAuthenticator();
    }

    public void init(final Scope config) {
    }

    public void postInit(final KeycloakSessionFactory factory) {
    }

    public void close() {
    }

    public String getId() {
        return "shanoir-ng-post-auth";
    }

    public String getDisplayText() {
        return "Shanoir NG post-auth actions";
    }

    public boolean isUserSetupAllowed() {
        // FIXME: not clear (might mess up with required actions)
        return false;
    }

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
        AuthenticationExecutionModel.Requirement.REQUIRED,
        AuthenticationExecutionModel.Requirement.DISABLED
    };
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    public String getReferenceCategory() {
        return "post-auth";
    }

    public String getDisplayType() {
        return "Shanoir NG post-auth actions";
    }

    public String getHelpText() {
        return "Post-authentication actions needed by shanoir (check IP, check expiration date, update last login date)";
    }

    public boolean isConfigurable() {
        return false;
    }


    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<ProviderConfigProperty>();

    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

}
