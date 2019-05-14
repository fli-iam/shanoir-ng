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