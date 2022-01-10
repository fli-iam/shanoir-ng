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
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import * as AppUtils from '../../utils/app.utils';
import { StudyUserRight } from './study-user-right.enum';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';


@Injectable()
export class StudyRightsService {
    
    constructor(private http: HttpClient, private keycloakService: KeycloakService) {

    }

    public canDownloadStudy(studyId: number): Promise<boolean> {
        return this.hasRightForStudy(studyId, StudyUserRight.CAN_DOWNLOAD);
    }

    public canAdministrateStudy(studyId: number): Promise<boolean> {
        return this.hasRightForStudy(studyId, StudyUserRight.CAN_ADMINISTRATE);
    }

    public hasRightForStudy(studyId: number, right: StudyUserRight): Promise<boolean> {
        if (this.keycloakService.isUserAdmin()) return Promise.resolve(true);
        return this.getMyRightsForStudy(studyId).then(studyRights => studyRights.includes(right));
    }

    public getMyRightsForStudy(studyId: number): Promise<StudyUserRight[]> {
        return this.http.get<StudyUserRight[]>(AppUtils.BACKEND_API_STUDY_RIGHTS + '/' + studyId)
            .toPromise()
            .then(rights => rights ? rights : []);
    }

    public getMyRights(): Promise<Map<number, StudyUserRight[]>> {
        return this.http.get<Map<number, StudyUserRight[]>>(AppUtils.BACKEND_API_STUDY_RIGHTS + '/all')
            .toPromise()
            .then(rights => {
                return rights ? Object.entries(rights).reduce((map: Map<number, StudyUserRight[]>, entry) => map.set(parseInt(entry[0]), entry[1]), new Map()) : new Map();
            });
    }

    hasOnStudyToImport(): Promise<boolean> {
        if (this.keycloakService.isUserAdmin()) return Promise.resolve(true);
        return this.http.get<boolean>(AppUtils.BACKEND_API_STUDY_HAS_ONE_STUDY_TO_IMPORT)
            .toPromise();
    }
    
}
