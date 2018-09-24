import { Component, ViewContainerRef } from '@angular/core';

import { KeycloakService } from './shared/keycloak/keycloak.service';
import { ModalService } from './shared/components/modals/modal.service';
import { LocationStrategy } from '@angular/common';

@Component({
    selector: 'shanoir-ng-app',
    templateUrl: 'app.component.html'
})

export class AppComponent {

    constructor(
            public viewContainerRef: ViewContainerRef,
            private modalService: ModalService,
            location: LocationStrategy) {
        
        this.modalService.rootViewCRef = this.viewContainerRef;
        
        location.onPopState(() => {
            console.log(window.location, window.history);
          });
    }

    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
    }

}