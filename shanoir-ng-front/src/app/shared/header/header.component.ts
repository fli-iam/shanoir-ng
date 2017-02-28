import { Component } from '@angular/core';

import { KeycloakService } from "../keycloak/keycloak.service";

@Component({
    selector: 'header',
    moduleId: module.id,
    templateUrl: 'header.component.html',
    styleUrls: ['../css/common.css', 'header.component.css']
})

export class HeaderComponent {
    shanoirLogoUrl: string;

    constructor(private keycloakService: KeycloakService) {
        this.shanoirLogoUrl = '/assets/logo.shanoir.white.png';
    }

    logout(event): void {
        event.preventDefault();
        this.keycloakService.logout();
    }
}