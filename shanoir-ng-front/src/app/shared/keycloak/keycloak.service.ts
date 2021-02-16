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
            keycloakAuth.init({ onLoad: 'login-required' })
                .success(() => {
                    KeycloakService.auth.loggedIn = true;
                    KeycloakService.auth.authz = keycloakAuth;
                    // Connected user id
                    KeycloakService.auth.userId = keycloakAuth.tokenParsed.userId;
                    KeycloakService.auth.logoutUrl = keycloakAuth.authServerUrl + '/realms/shanoir-ng/protocol/openid-connect/logout?redirect_uri='
                        + AppUtils.LOGOUT_REDIRECT_URL;
                    resolve(null);
                })
                .error(() => {
                    reject();
                });
        });
    }

    logout() {
        KeycloakService.auth.authz.logout();
    }
    
    getToken(): Promise<string> {
        if (!this.gettingToken) {
            this.gettingToken = true;
            this.tokenPromise = new Promise<string>((resolve, reject) => {
                if (KeycloakService.auth.authz.token) {
                    KeycloakService.auth.authz.updateToken(5).success(() => {
                        this.gettingToken = false;
                        resolve(<string>KeycloakService.auth.authz.token);
                    }).error(() => {
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
