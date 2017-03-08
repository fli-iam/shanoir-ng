import { Component } from '@angular/core';

import { KeycloakService } from '../shared/keycloak/keycloak.service';

@Component({
    selector: 'home',
    templateUrl: 'home.component.html',
    styleUrls: ['home.component.css']
})

export class HomeComponent {

    constructor() {
    }
    
    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
    }

}