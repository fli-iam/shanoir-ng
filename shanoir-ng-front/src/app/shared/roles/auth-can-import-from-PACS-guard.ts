import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { KeycloakService } from "../keycloak/keycloak.service";
import { MsgBoxService } from '../msg-box/msg-box.service';

@Injectable()
export class CanImportFromPACSGuard implements CanActivate {

    constructor(
        private keycloakService: KeycloakService, 
        private router: Router,
        private msgService: MsgBoxService) {

    }

    canActivate() {
        if (this.keycloakService.canUserImportFromPACS() === true) {
            return true;
        }
        this.router.navigate(['/home']);
        this.msgService.log('warn', 'Sorry, you have no right to visit to this page.');
        return false;
    }

}