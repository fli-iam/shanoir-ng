package org.shanoir.ng.keycloak;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.log4j.BasicConfigurator;
import org.keycloak.admin.client.resource.AuthenticationManagementResource;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationExecutionRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class KeycloakInitServer extends AbstractKeycloakInit {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(KeycloakInitServer.class);

	private static final String BROWSER_FLOW = "browser";
	private static final String NEW_BROWSER_FLOW = "Browser script";
	private static final String NEW_EXECUTION_CONFIG_NAME = "CheckExpirationDateConfig";
	private static final String REQUIRED_ACTION_NAME = "Record Login Date Action";
	private static final String SHANOIR_REALM_DISPLAY_NAME = "Shanoir";

	public static void main(String[] args) {
		BasicConfigurator.configure();

		loadParams();
		createAuthenticationFlow();
		updateRealm();
		updateRequiredAction();
	}

	/*
	 * Create authentication flow
	 */
	private static void createAuthenticationFlow() {
		LOG.info("Create authentication flow");

		final AuthenticationManagementResource authenticationManagement = getKeycloak().realm(getKeycloakRealm())
				.flows();

		// Create new flow
		LOG.info("Create new flow");
		Map<String, String> data = new HashMap<>();
		data.put("newName", NEW_BROWSER_FLOW);
		Response response = authenticationManagement.copy(BROWSER_FLOW, data);
		if (response.getStatus() != 201) {
			LOG.error("Error on flow copy");
		}

		// Get flow id
		LOG.info("Get flow id");
		final List<AuthenticationFlowRepresentation> flows = authenticationManagement.getFlows();
		String flowId = "";
		for (AuthenticationFlowRepresentation flow : flows) {
			if (NEW_BROWSER_FLOW.equals(flow.getAlias())) {
				flowId = flow.getId();
				break;
			}
		}

		// Create new execution (provider: Script)
		LOG.info("Create new execution (provider: Script)");
		final AuthenticationExecutionRepresentation authenticationExecution = new AuthenticationExecutionRepresentation();
		authenticationExecution.setAuthenticator("auth-script-based");
		authenticationExecution.setParentFlow(flowId);
		authenticationExecution.setRequirement("REQUIRED");
		response = getKeycloak().realm(getKeycloakRealm()).flows().addExecution(authenticationExecution);
		if (response.getStatus() != 201) {
			LOG.error("Error on execution creation");
		}

		// Get execution id
		LOG.info("Get execution id");
		final List<AuthenticationExecutionInfoRepresentation> exeutions = authenticationManagement
				.getExecutions(NEW_BROWSER_FLOW);
		AuthenticationExecutionInfoRepresentation executionInfo = null;
		for (AuthenticationExecutionInfoRepresentation execution : exeutions) {
			if ("Script".equals(execution.getDisplayName())) {
				executionInfo = execution;
				break;
			}
		}

		// Create new execution config (to prevent access from expired accounts)
		LOG.info("Create new execution config");
		final Map<String, String> config = new HashMap<>();
		config.put("scriptCode",
				"/*\n * Template for JavaScript based authenticator\'s.\n * See org.keycloak.authentication.authenticators.browser.ScriptBasedAuthenticatorFactory\n */\n\n// import enum for error lookup\n//AuthenticationFlowError = Java.type(\"org.keycloak.authentication.AuthenticationFlowError\");\n\n/**\n * An example authenticate function.\n *\n * The following variables are available for convenience:\n * user - current user {@see org.keycloak.models.UserModel}\n * realm - current realm {@see org.keycloak.models.RealmModel}\n * session - current KeycloakSession {@see org.keycloak.models.KeycloakSession}\n * httpRequest - current HttpRequest {@see org.jboss.resteasy.spi.HttpRequest}\n * script - current script {@see org.keycloak.models.ScriptModel}\n * LOG - current logger {@see org.jboss.logging.Logger}\n *\n * You one can extract current http request headers via:\n * httpRequest.getHttpHeaders().getHeaderString(\"Forwarded\")\n *\n * @param context {@see org.keycloak.authentication.AuthenticationFlowContext}\n */\nfunction authenticate(context) {\n\n    var username = user ? user.username : \"anonymous\";\n    LOG.info(script.name + \" trace auth for: \" + username);\n\n    var authShouldFail = false;\n    if (authShouldFail) {\n        context.failure(\"INVALID_USER\");\n        return;\n    }\n    \n    if (user.attributes[\'expirationDate\'] !== null && new Date().getTime() > user.attributes[\'expirationDate\'][0]) {\n        user.enabled = false;\n        context.failure(\"USER_DISABLED\");\n        return;\n    }\n\n    context.success();\n}");
		config.put("scriptDescription", "Check expiration date on login");
		config.put("scriptName", "CheckExpirationDate");
		final AuthenticatorConfigRepresentation authenticatorConfig = new AuthenticatorConfigRepresentation();
		authenticatorConfig.setAlias(NEW_EXECUTION_CONFIG_NAME);
		authenticatorConfig.setConfig(config);
		response = getKeycloak().realm(getKeycloakRealm()).flows().newExecutionConfig(executionInfo.getId(),
				authenticatorConfig);
		if (response.getStatus() != 201) {
			LOG.error("Error on execution config creation");
		}
	}

	/*
	 * Update realm
	 */
	private static void updateRealm() {
		LOG.info("Update realm " + getKeycloakRealm());

		final RealmRepresentation realm = getKeycloak().realm(getKeycloakRealm()).toRepresentation();
		realm.setBrowserFlow(NEW_BROWSER_FLOW);
		realm.setDisplayName(SHANOIR_REALM_DISPLAY_NAME);
		realm.setLoginTheme("shanoir-theme");
		realm.setPasswordPolicy("hashIterations and length and specialChars and digits and upperCase and lowerCase");
		realm.setResetPasswordAllowed(Boolean.TRUE);
	}

	/*
	 * Update required action
	 */
	private static void updateRequiredAction() {
		final List<RequiredActionProviderRepresentation> requiredActions = getKeycloak().realm(getKeycloakRealm())
				.flows().getRequiredActions();
		for (RequiredActionProviderRepresentation action : requiredActions) {
			//FIXME: what is the purpose ? (there is no action named "REQUIRED_ACTION_NAME")
			if (REQUIRED_ACTION_NAME.equals(action.getAlias())) {
				action.setEnabled(true);
				action.setDefaultAction(true);
				getKeycloak().realm(getKeycloakRealm()).flows().updateRequiredAction(REQUIRED_ACTION_NAME, action);
				break;
			}
		}
	}

}
