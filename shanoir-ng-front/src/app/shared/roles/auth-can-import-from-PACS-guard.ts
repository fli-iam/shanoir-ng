import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { KeycloakService } from "../keycloak/keycloak.service";

@Injectable()
export class CanImportFromPACSGuard implements CanActivate {

    constructor(private keycloakService: KeycloakService, private router: Router) {

    }

    canActivate() {
        if (this.keycloakService.canUserImportFromPACS() === true) {
            return true;
        }
        this.router.navigate(['/home']);
        return false;
    }

}