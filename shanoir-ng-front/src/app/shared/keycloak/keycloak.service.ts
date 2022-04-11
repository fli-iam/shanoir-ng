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
import { Injectable } from '@angular/core';

import * as AppUtils from '../../utils/app.utils';


// The keycloak adapter supports two authentication methods : "login-required"
// and "check-sso", as documented in:
// https://www.keycloak.org/docs/latest/securing_apps/#_javascript_adapter)
//
// - "check-sso" performs the authentication in an hidden iframe and won't
//   require any redirection (if using a silent check url). It is fast but may
//   fail with modern browsers if the keycloak server is served from a
//   different host name.
//   https://www.keycloak.org/docs/latest/securing_apps/#_modern_browsers
//
// - "login-required" redirects the browser to the keycloak server, which
//   checks the session cookie, generates the token and redirects to us.
//   It is reliable but slower (because of the two redirections the
//   authentication is performed twice and the SPA is loaded twice).
const USE_LOGIN_REQUIRED = (<any>window).SHANOIR_KEYCLOAK_ADAPTER_MODE == "login-required";

declare var Keycloak: any;

@Injectable()
export class KeycloakService {
    static auth: any = {};
    // static auth: any = { loggedIn: true };
    private gettingToken: boolean = false;
    private tokenPromise: Promise<string>;

    static init(): Promise<any> {

        if (window.location.href.endsWith('/account-request')) {
            return Promise.resolve();
        }

        const keycloakAuth: any = Keycloak({
            url: AppUtils.KEYCLOAK_BASE_URL,
            realm: 'shanoir-ng',
            clientId: 'shanoir-ng-front',
        });
        KeycloakService.auth.loggedIn = true; // false;

        return new Promise((resolve, reject) => {
            keycloakAuth.init(
                USE_LOGIN_REQUIRED
                ? { onLoad: 'login-required' }
                : { onLoad: 'check-sso', silentCheckSsoRedirectUri: AppUtils.SILENT_CHECK_SSO_URL }
            )
                .then((authenticated) => {
                    if (authenticated) {
                        KeycloakService.auth.loggedIn = true;
                        KeycloakService.auth.authz = keycloakAuth;
                        // Connected user id
                        KeycloakService.auth.userId = keycloakAuth.tokenParsed.userId;
                        KeycloakService.auth.logoutUrl = keycloakAuth.authServerUrl + '/realms/shanoir-ng/protocol/openid-connect/logout?redirect_uri='
                            + AppUtils.LOGOUT_REDIRECT_URL;
                        resolve(null);
                    } else {
                        if (!USE_LOGIN_REQUIRED) {
                            // When the session cookie is invalid 'login-required'
                            // automatically redirects to the login form.
                            // But 'check-sso' only checks the cookie, we have
                            // to do an explicit redirection
                            window.location.replace(keycloakAuth.createLoginUrl());
                        }
                        reject();
                    }
                })
                .catch(() => {
                    reject();
                });
        });
    }

    logout() {
        if(KeycloakService.auth && KeycloakService.auth.authz) KeycloakService.auth.authz.logout();
    }
    
    getToken(): Promise<string> {
        if (!this.gettingToken) {
            this.gettingToken = true;
            this.tokenPromise = new Promise<string>((resolve, reject) => {
                if (KeycloakService.auth.authz.token) {
                    KeycloakService.auth.authz.updateToken(5).then(() => {
                        this.gettingToken = false;
                        resolve(<string>KeycloakService.auth.authz.token);
                    }).catch(() => {
                        reject();
                    });
                }
            });
        }
        return this.tokenPromise;
    }

    isUserAdmin(): boolean {
        return KeycloakService.auth.authz && KeycloakService.auth.authz.hasRealmRole("ROLE_ADMIN");
    }

    isUserExpert(): boolean {
        return KeycloakService.auth.authz && KeycloakService.auth.authz.hasRealmRole("ROLE_EXPERT");
    }

    isUserAdminOrExpert(): boolean {
        return this.isUserAdmin() || this.isUserExpert();
    }

    canUserImportFromPACS(): boolean {
        return this.isUserAdmin() || KeycloakService.auth.authz && KeycloakService.auth.authz.tokenParsed.canImportFromPACS;
    }
}
