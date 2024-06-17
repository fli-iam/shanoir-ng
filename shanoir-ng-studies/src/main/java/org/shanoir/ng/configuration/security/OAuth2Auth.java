package org.shanoir.ng.configuration.security;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SecurityScheme(
        type = SecuritySchemeType.OAUTH2,
        name = "OAuth2Auth",
        flows = @OAuthFlows(password = @OAuthFlow(
                authorizationUrl = "${SHANOIR_URL_SCHEME}://${SHANOIR_URL_HOST}/auth/realms/shanoir-ng/protocol/openid-connect/auth/",
                tokenUrl = "${SHANOIR_URL_SCHEME}://${SHANOIR_URL_HOST}/auth/realms/shanoir-ng/protocol/openid-connect/token",
                refreshUrl = "${SHANOIR_URL_SCHEME}://${SHANOIR_URL_HOST}/auth/realms/shanoir-ng/protocol/openid-connect/token"
        )))
public class OAuth2Auth {
}
