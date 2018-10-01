import { Component, OnInit } from '@angular/core';

import { KeycloakService } from '../shared/keycloak/keycloak.service';
import { ImagesUrlUtil } from '../shared/utils/images-url.util';
import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';

@Component({
    selector: 'home',
    templateUrl: 'home.component.html',
    styleUrls: ['home.component.css']
})

export class HomeComponent implements OnInit {

    shanoirBigLogoUrl: string = ImagesUrlUtil.SHANOIR_BLACK_LOGO_PATH;

    constructor(private breadcrumbsService: BreadcrumbsService) {
    }

    ngOnInit() {
        this.breadcrumbsService.reset();
    }
    
    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
    }

}