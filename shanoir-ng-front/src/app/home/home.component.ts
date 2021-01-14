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

import { Component } from '@angular/core';

import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { KeycloakService } from '../shared/keycloak/keycloak.service';
import { ImagesUrlUtil } from '../shared/utils/images-url.util';
import { HttpClient } from '@angular/common/http';
import { ServiceLocator } from '../utils/locator.service';

@Component({
    selector: 'home',
    templateUrl: 'home.component.html',
    styleUrls: ['home.component.css']
})

export class HomeComponent {

    shanoirBigLogoUrl: string = ImagesUrlUtil.SHANOIR_BLACK_LOGO_PATH;
    
    private http: HttpClient = ServiceLocator.injector.get(HttpClient);

    constructor(private breadcrumbsService: BreadcrumbsService) {
        //this.breadcrumbsService.nameStep('Home');
        this.breadcrumbsService.markMilestone();
    }

    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
    }

}