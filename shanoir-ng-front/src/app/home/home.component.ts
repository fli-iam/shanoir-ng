import { Component } from '@angular/core';

import { KeycloakService } from '../shared/keycloak/keycloak.service';

@Component({
    selector: 'home',
    templateUrl: 'home.component.html',
    styleUrls: ['home.component.css']
})

export class HomeComponent {
    shanoirBigLogoUrl: string;

    constructor() {
        this.shanoirBigLogoUrl = 'assets/images/logo.shanoir.black.png';
    }
    
    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
    }

}