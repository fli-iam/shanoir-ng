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
