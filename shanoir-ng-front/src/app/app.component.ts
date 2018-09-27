import { Component, ViewContainerRef } from '@angular/core';

import { KeycloakService } from './shared/keycloak/keycloak.service';
import { ModalService } from './shared/components/modals/modal.service';
import { LocationStrategy } from '@angular/common';
import { BreadcrumbsService } from './breadcrumbs/breadcrumbs.service';
import { ServiceLocator } from './utils/locator.service';

@Component({
    selector: 'shanoir-ng-app',
    templateUrl: 'app.component.html'
})

export class AppComponent {

    constructor(
            public viewContainerRef: ViewContainerRef,
            private modalService: ModalService,
            private location: LocationStrategy,
            private breadcrumbsService: BreadcrumbsService) {
        
        this.modalService.rootViewCRef = this.viewContainerRef;
        ServiceLocator.rootViewContainerRef = this.viewContainerRef;
        
        location.onPopState(() => {
            this.breadcrumbsService.notifyBeforeBack();
        });

        window.onbeforeunload = function() { return "Warning! Your work may be lost!"; };
    }

    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
    }

}