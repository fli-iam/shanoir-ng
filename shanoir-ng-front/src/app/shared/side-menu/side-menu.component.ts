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

import { Component, EventEmitter, Output } from '@angular/core';
import { slideDown } from '../animations/animations';
import { KeycloakService } from '../keycloak/keycloak.service';
import { ImagesUrlUtil } from '../utils/images-url.util';


@Component({
    selector: 'side-menu',
    templateUrl: 'side-menu.component.html',
    styleUrls: ['side-menu.component.css'],
    animations: [ slideDown ]
})

export class SideMenuComponent {

    private shanoirLogoUrl: string = ImagesUrlUtil.SHANOIR_WHITE_LOGO_PATH;
    private username: string = "";
    private dataOpened: boolean = true;
    private eqOpened: boolean = true;
    private uploadOpened: boolean = true;
    private adminOpened: boolean = true;

    constructor(private keycloakService: KeycloakService) {
        if (KeycloakService.auth.authz && KeycloakService.auth.authz.tokenParsed) {
            this.username = KeycloakService.auth.authz.tokenParsed.name;
        }
    }

    logout(event: Event): void {
        event.preventDefault();
        this.keycloakService.logout();
    }

    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
    }

    isUserAdmin(): boolean {
        return this.keycloakService.isUserAdmin();
    }
    
    canUserImportFromPACS(): boolean {
        return this.keycloakService.canUserImportFromPACS();
    }

}