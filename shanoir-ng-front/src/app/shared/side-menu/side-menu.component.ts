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
import { Component, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DatePipe } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { environment } from '../../../environments/environment';
import { VERSION } from '../../../environments/version';
import { SolrService } from '../../solr/solr.service';
import { StudyService } from '../../studies/shared/study.service';
import { UserService } from '../../users/shared/user.service';
import { ConfirmDialogService } from "../components/confirm-dialog/confirm-dialog.service";
import { ConsoleService } from '../console/console.service';
import { KeycloakService } from '../keycloak/keycloak.service';
import { NotificationsService } from '../notifications/notifications.service';
import { ImagesUrlUtil } from '../utils/images-url.util';
import { LoadingBarComponent } from '../components/loading-bar/loading-bar.component';


@Component({
    selector: 'side-menu',
    templateUrl: 'side-menu.component.html',
    styleUrls: ['side-menu.component.css', environment.production ? 'prod.css' : 'dev.css'],
    imports: [RouterLink, RouterLinkActive, LoadingBarComponent, DatePipe]
})

export class SideMenuComponent {

    public shanoirLogoUrl: string = ImagesUrlUtil.SHANOIR_WHITE_LOGO_PATH;
    public username: string = "";
    public userId: number = 0;
    public state: SideMenuState;
    public VERSION = VERSION;
    private sessionKey: string = KeycloakService.auth.userId + 'menuState';
    accessRequestsToValidate: number;


    constructor(
            public keycloakService: KeycloakService,
            private solrService: SolrService,
            private consoleService: ConsoleService,
            public notificationsService: NotificationsService,
            private studyService: StudyService,
            private userService: UserService,
            private confirmDialogService: ConfirmDialogService,
            private destroyRef: DestroyRef) {

        if (KeycloakService.auth.authz && KeycloakService.auth.authz.tokenParsed) {
            this.username = KeycloakService.auth.authz.tokenParsed.name;
            this.userId = KeycloakService.auth.userId;
        }

        const storedState = sessionStorage.getItem(this.sessionKey);
        if (storedState) this.state = JSON.parse(storedState) as SideMenuState;
        else this.state = new SideMenuState();

        this.userService.accessRequets
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(nb => {
            if (nb) {
                this.accessRequestsToValidate = nb;
            } else {
                this.accessRequestsToValidate = 0;
            }
        });
    }

    logout(event: Event): void {
        event.preventDefault();
        this.keycloakService.logout();
    }

    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
    }

    isUserAdmin(): boolean {
        return this.keycloakService.isUserAdmin();
    }

    canUserImportFromPACS(): boolean {
        return this.keycloakService.canUserImportFromPACS();
    }

    indexToSolr() {
        this.confirmDialogService.confirm('Index solr',
            'Indexing solr can take some time, it won\'t be available during this time. Are you sure ?')
            .then(userChoice => {
                if (userChoice) {
                    this.solrService.indexAll().then(() => {
                        this.consoleService.log('info', 'Indexation launched !');
                    });
                }
            });
    }

    duasToSign(): number {
        return this.studyService.duasToSign;
    }

    saveState() {
        sessionStorage.setItem(this.sessionKey, JSON.stringify(this.state));
    }
}

export class SideMenuState {

    public dataOpened: boolean = false;
    public precOpened: boolean = false;
    public eqOpened: boolean = false;
    public uploadOpened: boolean = false;
    public adminOpened: boolean = false;
    public notifOpened: boolean = false;
    public jobsOpened: boolean = true;
}
