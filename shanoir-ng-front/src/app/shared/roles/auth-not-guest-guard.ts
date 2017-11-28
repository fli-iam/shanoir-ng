import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { KeycloakService } from "../keycloak/keycloak.service";

@Injectable()
export class AuthNotGuestGuard implements CanActivate {

    constructor(private keycloakService: KeycloakService, private router: Router) {

    }

    canActivate() {
        if (this.keycloakService.isUserGuest() === true) {
            this.router.navigate(['/home']);
            return false;
        }
        return true;
    }

}