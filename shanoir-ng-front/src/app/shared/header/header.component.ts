import { Component } from '@angular/core';

import { KeycloakService } from "../keycloak/keycloak.service";

@Component({
    selector: 'header',
    templateUrl: 'header.component.html',
    styleUrls: ['header.component.css']
})

export class HeaderComponent {
    shanoirLogoUrl: string;
    username: string = "";

    constructor(private keycloakService: KeycloakService) {
        this.shanoirLogoUrl = 'assets/images/logo.shanoir.white.png';
        if (KeycloakService.auth.authz && KeycloakService.auth.authz.tokenParsed) {
            this.username = KeycloakService.auth.authz.tokenParsed.name;
        }
    }

    logout(event: Event): void {
        event.preventDefault();
        this.keycloakService.logout();
    }
}