package org.shanoir.ng.configuration.security;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SecurityScheme(
        type = SecuritySchemeType.OAUTH2,
        name = "OAuth2Auth",
        flows = @OAuthFlows(password = @OAuthFlow(
                authorizationUrl = "https://shanoir-ofsep-qualif.irisa.fr/auth/",
                tokenUrl = "https://shanoir-ofsep-qualif.irisa.fr/auth/realms/shanoir-ng/protocol/openid-connect/token",
                refreshUrl = "")))
public class OAuth2Auth {
}
