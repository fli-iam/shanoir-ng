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

import { Component } from '@angular/core';
import { Task } from '../async-tasks/task.model';
import { TaskService } from '../async-tasks/task.service';

import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { DataUserAgreement } from '../dua/shared/dua.model';
import { KeycloakService } from '../shared/keycloak/keycloak.service';
import { ImagesUrlUtil } from '../shared/utils/images-url.util';
import { Study } from '../studies/shared/study.model';
import { StudyService } from '../studies/shared/study.service';
import { User } from '../users/shared/user.model';
import { UserService } from '../users/shared/user.service';

@Component({
    selector: 'home',
    templateUrl: 'home.component.html',
    styleUrls: ['home.component.css']
})

export class HomeComponent {

    shanoirBigLogoUrl: string = ImagesUrlUtil.SHANOIR_BLACK_LOGO_PATH;
    
    challengeDua: DataUserAgreement;
    challengeStudy: Study;
    studies: Study[];
    accountRequests: User[];
    jobs: Task[];
    solrInput: string;
    notifications: any[];
    loaded: boolean = false;

    constructor(
            private breadcrumbsService: BreadcrumbsService,
            private studyService: StudyService,
            private keycloakService: KeycloakService,
            private userService: UserService,
            private taskService: TaskService) {
        //this.breadcrumbsService.nameStep('Home');
        this.breadcrumbsService.markMilestone();
        this.load();
    }

    load() {
        this.studyService.getMyDUA().then(duas => {
            this.challengeDua = null;
            this.notifications = null;
            if (duas) {
                this.notifications = duas.slice(0, 10);
                for (let dua of duas) {
                    if (dua.isChallenge) {
                        this.challengeDua = dua;
                        return;
                    }
                }
            }
        }).then(() => {
            this.loaded = true;
            if (this.admin || !this.challengeDua) {
                this.fetchChallengeStudy()
                if (this.admin) {
                    this.fetchAccountRequests();
                }
                this.fetchJobs();
            }
        });
    }

    onSign() {
        this.load();
    }

    private fetchChallengeStudy() {
        this.studyService.getAll().then(studies => {
            if (studies) {
                this.studies = studies.slice(0, 8);
                for (let study of studies) {
                    if (study.challenge) {
                        this.challengeStudy = study;
                        return;
                    }
                }
            }
        });
    }

    downloadFile(filePath: string) {
        this.studyService.downloadFile(filePath, this.challengeStudy.id, 'protocol-file');
    }

    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
    }

    get admin(): boolean {
        return this.keycloakService.isUserAdmin();
    }

    fetchAccountRequests() {
        this.userService.getAllAccountRequests()
            .then(ar => this.accountRequests = ar.slice(0, 8));
    }

    fetchJobs() {
        this.taskService.getAll()
            .then(tasks => this.jobs = tasks.slice(0, 10));
    }

    canUserImportFromPACS(): boolean {
        return this.keycloakService.canUserImportFromPACS();
    }

}