import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { KeycloakService } from "../keycloak/keycloak.service";

@Injectable()
export class AuthAdminGuard implements CanActivate {

    constructor(private keycloakService: KeycloakService) {

    }

    canActivate() {
        return Observable.of(this.keycloakService.isUserAdmin());
    }

}