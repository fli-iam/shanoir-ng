import { Component } from '@angular/core';
import { Router, NavigationStart } from '@angular/router';

import { KeycloakService } from './shared/keycloak/keycloak.service';

import '../assets/css/common.css';

@Component({
    selector: 'shanoir-ng-app',
    templateUrl: 'app.component.html'
})

export class AppComponent {

    constructor(private router: Router) {
        router.events.subscribe(e => {
            if (e instanceof NavigationStart && e.url !== '/accountRequest') {
                if (!KeycloakService.auth.loggedIn) {
                    KeycloakService.init()
                        .then(() => { })
                        .catch(() => window.location.reload());
                }
            }
        });
    }

    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
    }

}