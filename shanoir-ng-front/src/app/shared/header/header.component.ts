import { Component } from '@angular/core';

import { KeycloakService } from "../keycloak/keycloak.service";

@Component({
    selector: 'header',
    templateUrl: 'header.component.html',
    styleUrls: ['header.component.css']
})

export class HeaderComponent {
    shanoirLogoUrl: string;

    constructor(private keycloakService: KeycloakService) {
        this.shanoirLogoUrl = '/assets/images/logo.shanoir.white.png';
    }

    logout(event: Event): void {
        event.preventDefault();
        this.keycloakService.logout();
    }
}