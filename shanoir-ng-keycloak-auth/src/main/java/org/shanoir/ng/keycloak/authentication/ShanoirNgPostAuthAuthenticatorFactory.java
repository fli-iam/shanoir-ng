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

	private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
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


	private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();

	public List<ProviderConfigProperty> getConfigProperties() {
		return configProperties;
	}

}
