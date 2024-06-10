package org.shanoir.ng.configuration.security;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SecurityScheme(
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        name = "BearerAuth",
        openIdConnectUrl = "https://localhost/auth")
public class BearerAuth {
}
