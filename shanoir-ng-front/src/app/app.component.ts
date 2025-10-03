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

import { Component, ElementRef, HostBinding, HostListener, ViewChild, ViewContainerRef, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { parent, slideMarginLeft, slideRight } from './shared/animations/animations';
import { ConfirmDialogService } from './shared/components/confirm-dialog/confirm-dialog.service';
import { ConsoleComponent } from './shared/console/console.component';
import { KeycloakService } from './shared/keycloak/keycloak.service';
import { GlobalService } from './shared/services/global.service';
import { WindowService } from './shared/services/window.service';
import { KeycloakSessionService } from './shared/session/keycloak-session.service';
import { StudyService } from './studies/shared/study.service';
import { TreeService } from './studies/study/tree.service';
import { UserService } from './users/shared/user.service';
import { ServiceLocator } from './utils/locator.service';
import { NotificationsService } from './shared/notifications/notifications.service';

@Component({
    selector: 'app-root',
    templateUrl: 'app.component.html',
    styleUrls: ['app.component.css'],
    animations: [slideRight, slideMarginLeft, parent],
    standalone: false
})

export class AppComponent implements OnInit {

    @HostBinding('@parent') public menuOpen: boolean = true;
    @ViewChild('console') consoleComponenent: ConsoleComponent;

    constructor(
            public viewContainerRef: ViewContainerRef,
            private globalService: GlobalService,
            private windowService: WindowService,
            private element: ElementRef,
            private keycloakSessionService: KeycloakSessionService,
            private confirmService: ConfirmDialogService,
            protected router: Router,
            private studyService: StudyService,
            private userService: UserService,
            public treeService: TreeService,
            private notificationsService: NotificationsService) {
        
        ServiceLocator.rootViewContainerRef = this.viewContainerRef;
    }

    ngOnInit() {
        this.globalService.registerGlobalClick(this.element);
        this.windowService.width = window.innerWidth;
        if(this.keycloakSessionService.isAuthenticated()) {
            this.userService.getAccessRequestsForAdmin();
            this.duaAlert();
        }
    }

    @HostListener('window:resize', ['$event'])
    onResize(event) {
        this.windowService.width = event.target.innerWidth;
    }

    @HostListener('window:beforeunload')
    canDeactivate(): boolean {
        return !this.notificationsService.hasOnGoingDownloads();
    }


    toggleMenu(open: boolean) {
        this.menuOpen = open;
    }

    toggleTree(open: boolean) {
        this.treeService.treeOpened = open;
    }

    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
    }

    private duaAlert() {
        this.studyService.getMyDUA().then(dua => {
            const hasDUA: boolean = dua && dua.length > 0;
            if (hasDUA && !this.keycloakSessionService.hasBeenAskedDUA) {
                this.keycloakSessionService.hasBeenAskedDUA = true;
                if (this.router.url != '/dua' && this.router.url != '/home') {
                    this.askForDuaSigning();
                }
            }
        });
    }

    private askForDuaSigning() {
        const title: string = 'Data User Agreement awaiting for signing';
        const text: string = 'You are a member of at least one study that needs you to accept its data user agreement. '
            + 'Until you have agreed those terms you cannot access to any data from these studies. '
            + 'Would you like to review those terms now?';
        const buttons = {yes: 'Yes, proceed to the signing page', cancel: 'Later'};
        this.confirmService.confirm(title, text, buttons).then(response => {
                if (response == true) this.router.navigate(['/dua']);
            });
    }
}