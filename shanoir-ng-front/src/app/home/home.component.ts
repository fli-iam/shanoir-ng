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

import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { DataUserAgreement } from '../dua/shared/dua.model';
import { KeycloakService } from '../shared/keycloak/keycloak.service';
import { ImagesUrlUtil } from '../shared/utils/images-url.util';
import { Study } from '../studies/shared/study.model';
import { StudyService } from '../studies/shared/study.service';

@Component({
    selector: 'home',
    templateUrl: 'home.component.html',
    styleUrls: ['home.component.css']
})

export class HomeComponent {

    shanoirBigLogoUrl: string = ImagesUrlUtil.SHANOIR_BLACK_LOGO_PATH;
    
    challengeDua: DataUserAgreement;
    challengeStudy: Study;

    constructor(
            private breadcrumbsService: BreadcrumbsService,
            private studyService: StudyService) {
        //this.breadcrumbsService.nameStep('Home');
        this.breadcrumbsService.markMilestone();
        this.studyService.getMyDUA().then(duas => {
            if (duas) {
                for (let dua of duas) {
                    if (dua.isChallenge) {
                        this.challengeDua = dua;
                        return;
                    }
                }
            }
        }).then(() => {
            if (!this.challengeDua) {
                this.fetchChallengeStudy()
            }
        });
    }

    onSign() {
        this.fetchChallengeStudy();
    }

    private fetchChallengeStudy() {
        this.studyService.getAll().then(studies => {
            if (studies) {
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

}