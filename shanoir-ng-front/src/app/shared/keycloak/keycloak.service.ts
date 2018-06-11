import { Injectable } from '@angular/core';

declare var Keycloak: any;

@Injectable()
export class KeycloakService {
    static auth: any = {};
    private gettingToken: boolean = false;
    private tokenPromise: Promise<string>;

    static init(): Promise<any> {
        const keycloakAuth: any = Keycloak({
            url: process.env.KEYCLOAK_BASE_URL,
            realm: 'shanoir-ng',
            clientId: 'shanoir-ng-front',
        });
        KeycloakService.auth.loggedIn = false;

        return new Promise((resolve, reject) => {
            keycloakAuth.init({ onLoad: 'login-required' })
                .success(() => {
                    KeycloakService.auth.loggedIn = true;
                    KeycloakService.auth.authz = keycloakAuth;
                    // Connected user id
                    KeycloakService.auth.userId = keycloakAuth.tokenParsed.userId;
                    KeycloakService.auth.logoutUrl = keycloakAuth.authServerUrl + '/realms/shanoir-ng/protocol/openid-connect/logout?redirect_uri='
                        + process.env.LOGOUT_REDIRECT_URL;
                    resolve();
                })
                .error(() => {
                    reject();
                });
        });
    }

    logout() {
        KeycloakService.auth.loggedIn = false;
        KeycloakService.auth.authz = null;
        window.location.href = KeycloakService.auth.logoutUrl;
    }

    getToken(): Promise<string> {
        if (!this.gettingToken) {
            console.log('token refreshing...');
            this.tokenPromise = new Promise<string>((resolve, reject) => {
                if (KeycloakService.auth.authz.token) {
                    KeycloakService.auth.authz.updateToken(5).success(() => {
                        this.gettingToken = false;
                        resolve(<string>KeycloakService.auth.authz.token);
                    })
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

    isUserGuest(): boolean {
        return KeycloakService.auth.authz && KeycloakService.auth.authz.hasRealmRole("ROLE_GUEST");
    }

}