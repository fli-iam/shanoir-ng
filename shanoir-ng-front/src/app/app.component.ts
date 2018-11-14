import { Component, ViewContainerRef, ElementRef } from '@angular/core';

import { BreadcrumbsService } from './breadcrumbs/breadcrumbs.service';
import { ModalService } from './shared/components/modals/modal.service';
import { KeycloakService } from './shared/keycloak/keycloak.service';
import { ServiceLocator } from './utils/locator.service';
import { GlobalService } from './shared/services/global.service';

@Component({
    selector: 'shanoir-ng-app',
    templateUrl: 'app.component.html'
})

export class AppComponent {

    constructor(
            public viewContainerRef: ViewContainerRef,
            private modalService: ModalService,
            private breadcrumbsService: BreadcrumbsService,
            private globalService: GlobalService,
            private element: ElementRef) {
        
        this.modalService.rootViewCRef = this.viewContainerRef;
        ServiceLocator.rootViewContainerRef = this.viewContainerRef;

        // let storedBCStr = sessionStorage.getItem('breadcrumbs');
        // if (storedBCStr) {
        //     console.log('storedBCStr', storedBCStr)
        //     let storedBC = JSON.parse(storedBCStr);
        //     this.breadcrumbsService.steps = storedBC.steps.map(step => Step.parse(JSON.stringify(step)));
        //     this.breadcrumbsService.steps.map(step => {
        //         step.waitStep = this.breadcrumbsService.steps.find(oneStep => oneStep.id == step.id);
        //     });
        //     if (storedBC.savedStep)
        //         this.breadcrumbsService.savedStep = Step.parse(storedBC.savedStep);
        // }
    }

    ngOnInit() {
        this.globalService.registerGlobalClick(this.element);
    }

    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
    }

}