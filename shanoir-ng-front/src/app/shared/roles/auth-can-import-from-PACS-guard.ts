/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { KeycloakService } from "../keycloak/keycloak.service";
import { ConsoleService } from '../console/console.service';

@Injectable()
export class CanImportFromPACSGuard implements CanActivate {

    constructor(
        private keycloakService: KeycloakService, 
        private router: Router,
        private consoleService: ConsoleService) {

    }

    canActivate() {
        if (this.keycloakService.canUserImportFromPACS() === true) {
            return true;
        }
        this.router.navigate(['/home']);
        this.consoleService.log('warn', 'Sorry, you have no right to visit to this page.', ['route : ' + this.router.url]);
        return false;
    }

}