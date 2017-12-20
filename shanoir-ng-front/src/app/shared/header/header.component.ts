import { Component } from '@angular/core';

import { KeycloakService } from "../keycloak/keycloak.service";
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';

@Component({
    selector: 'header',
    templateUrl: 'header.component.html',
    styleUrls: ['header.component.css']
})

export class HeaderComponent {
    shanoirLogoUrl: string = ImagesUrlUtil.SHANOIR_WHITE_SMALL_LOGO_PATH;
    userLogoUrl: string;
    username: string = "";

    constructor(private keycloakService: KeycloakService) {
        if (KeycloakService.auth.authz && KeycloakService.auth.authz.tokenParsed) {
            this.username = KeycloakService.auth.authz.tokenParsed.name;
        }
    }

    logout(event: Event): void {
        event.preventDefault();
        this.keycloakService.logout();
    }
}