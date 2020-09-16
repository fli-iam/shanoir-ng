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

import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { IdName } from '../../shared/models/id-name.model';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import * as AppUtils from '../../utils/app.utils';
import { Study } from './study.model';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { StudyUserRight } from './study-user-right.enum';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { BidsElement } from '../../bids/model/bidsElement.model';

@Injectable()
export class StudyService extends EntityService<Study> {

    API_URL = AppUtils.BACKEND_API_STUDY_URL;

    constructor(protected http: HttpClient) {
        super(http)
    }

    getEntityInstance() { return new Study(); }
    
    findStudiesByUserId(): Promise<Study[]> {
        return this.http.get<Study[]>(AppUtils.BACKEND_API_STUDY_URL)
        .map(entities => entities.map((entity) => Object.assign(new Study(), entity)))
        .toPromise();
    }

    getStudiesNames(): Promise<IdName[]> {
        return this.http.get<IdName[]>(AppUtils.BACKEND_API_STUDY_ALL_NAMES_URL)
            .toPromise();
    }

    getStudyNamesAndCenters(): Promise<Study[]> {
        return this.http.get<Study[]>(AppUtils.BACKEND_API_STUDY_ALL_NAMES_AND_CENTERS_URL)
            .toPromise();
    }
    
    findSubjectsByStudyId(studyId: number): Promise<SubjectWithSubjectStudy[]> {
        return this.http.get<SubjectWithSubjectStudy[]>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + studyId + '/allSubjects')
            .toPromise();
    }

    findSubjectsByStudyIdPreclinical(studyId: number, preclinical: boolean): Promise<SubjectWithSubjectStudy[]> {
        return this.http.get<SubjectWithSubjectStudy[]>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + studyId + '/allSubjects?preclinical=' + preclinical)
            .toPromise();
    }

    findStudiesIcanAdmin(): Promise<number[]> {
        return this.getAll().then(studies => {
            const myId: number = KeycloakService.auth.userId;
            return studies.filter(study => {
                return study.studyUserList.filter(su => su.userId == myId && su.studyUserRights.includes(StudyUserRight.CAN_ADMINISTRATE)).length > 0;
            }).map(study => study.id);
        });
    }

    uploadFile(fileToUpload: File, studyId: number): Observable<any> {
        const endpoint = this.API_URL + '/protocol-file-upload/' + studyId;
        const formData: FormData = new FormData();
        formData.append('file', fileToUpload, fileToUpload.name);
        return this.http.post<any>(endpoint, formData);
    }

    deleteFile(studyId: number): Observable<any> {
        const endpoint = this.API_URL + '/protocol-file-delete/' + studyId;
        return this.http.delete(endpoint);
    }

    downloadFile(fileName: string, studyId: number): void {
        const endpoint = this.API_URL + '/protocol-file-download/' + studyId + "/" + fileName + "/";
        this.http.get(endpoint, { observe: 'response', responseType: 'blob' }).subscribe(response => {
            if (response.status == 200) {
                this.downloadIntoBrowser(response);
            }
        });;
    }

    private getFilename(response: HttpResponse<any>): string {
        const prefix = 'attachment;filename=';
        let contentDispHeader: string = response.headers.get('Content-Disposition');
        return contentDispHeader.slice(contentDispHeader.indexOf(prefix) + prefix.length, contentDispHeader.length);
    }

    private downloadIntoBrowser(response: HttpResponse<Blob>){
        AppUtils.browserDownloadFile(response.body, this.getFilename(response));
    }

    exportBIDSByStudyId(studyId: number): Promise<void> {
        if (!studyId) throw Error('study id is required');
        return this.http.get(AppUtils.BACKEND_API_STUDY_BIDS_EXPORT_URL + '/studyId/' + studyId,
            { observe: 'response', responseType: 'blob' }
        ).toPromise().then(response => {this.downloadIntoBrowser(response);});
    }

    getBidsStructure(studyId: number): Promise<BidsElement> {
        if (!studyId) throw Error('study id is required');
        return this.http.get<BidsElement>(AppUtils.BACKEND_API_STUDY_BIDS_STRUCTURE_URL + '/studyId/' + studyId)
            .toPromise();
    }
}