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

import { Component, ElementRef, ViewContainerRef, HostBinding } from '@angular/core';

import { BreadcrumbsService } from './breadcrumbs/breadcrumbs.service';
import { ModalService } from './shared/components/modals/modal.service';
import { KeycloakService } from './shared/keycloak/keycloak.service';
import { GlobalService } from './shared/services/global.service';
import { ServiceLocator } from './utils/locator.service';
import { slideRight, parent, slideMarginLeft } from './shared/animations/animations';


@Component({
    selector: 'shanoir-ng-app',
    templateUrl: 'app.component.html',
    styleUrls: ['app.component.css'],
    animations: [ slideRight, slideMarginLeft, parent ]
})

export class AppComponent {

    @HostBinding('@parent') private menuOpen: boolean = true; 

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

    toggleMenu(open: boolean) {
        this.menuOpen = open;
    }

    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
    }

}