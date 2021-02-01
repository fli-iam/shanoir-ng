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

import { Component, ElementRef, ViewContainerRef, HostBinding, HostListener } from '@angular/core';

import { BreadcrumbsService } from './breadcrumbs/breadcrumbs.service';
import { ModalService } from './shared/components/modals/modal.service';
import { KeycloakService } from './shared/keycloak/keycloak.service';
import { GlobalService } from './shared/services/global.service';
import { ServiceLocator } from './utils/locator.service';
import { slideRight, parent, slideMarginLeft } from './shared/animations/animations';
import { WindowService } from './shared/services/window.service';
import { KeycloakSessionService } from './shared/session/keycloak-session.service';
import { ConfirmDialogService } from './shared/components/confirm-dialog/confirm-dialog.service';


@Component({
    selector: 'app-root',
    templateUrl: 'app.component.html',
    styleUrls: ['app.component.css'],
    animations: [ slideRight, slideMarginLeft, parent ]
})

export class AppComponent {

    @HostBinding('@parent') public menuOpen: boolean = true;

    constructor(
            public viewContainerRef: ViewContainerRef,
            private modalService: ModalService,
            private breadcrumbsService: BreadcrumbsService,
            private globalService: GlobalService,
            private windowService: WindowService,
            private element: ElementRef,
            private keycloakSessionService: KeycloakSessionService,
            private confirmService: ConfirmDialogService) {
        
        this.modalService.rootViewCRef = this.viewContainerRef;
        ServiceLocator.rootViewContainerRef = this.viewContainerRef;

        // let storedBCStr = sessionStorage.getItem('breadcrumbs');
        // if (storedBCStr) {
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
        this.windowService.width = window.innerWidth;

        let hasDUA: boolean = true;
        if (hasDUA && !this.keycloakSessionService.hasBeenAskedDUA) {
            this.keycloakSessionService.hasBeenAskedDUA = true;
            const title: string = 'Data User Agreement awaiting for signing';
            const text: string = 'You are a member of at least one study that needs you to accept its data user agreement. '
                + 'Until you have agreed those terms you cannot access to any data from these studies. '
                + 'Would you like to review those terms now?';
            const buttons = {ok: 'Yes, proceed to the signing page', cancel: 'Later'};
            this.confirmService.confirm(title, text, buttons).then(response => {
                    console.log('response', response)
                });
        }
    }

    @HostListener('window:resize', ['$event'])
    onResize(event) {
        this.windowService.width = event.target.innerWidth;
    }

    toggleMenu(open: boolean) {
        this.menuOpen = open;
    }

    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
    }

}