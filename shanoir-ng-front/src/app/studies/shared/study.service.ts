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
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { BidsElement } from '../../bids/model/bidsElement.model';
import { DataUserAgreement } from '../../dua/shared/dua.model';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { IdName } from '../../shared/models/id-name.model';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import * as AppUtils from '../../utils/app.utils';
import { StudyUserRight } from './study-user-right.enum';
import { Study, StudyDTO } from './study.model';


@Injectable()
export class StudyService extends EntityService<Study> {

    API_URL = AppUtils.BACKEND_API_STUDY_URL;

    private _duasToSign: number = 0;

    constructor(protected http: HttpClient, private keycloakService: KeycloakService) {
        super(http)
    }

    getEntityInstance() { return new Study(); }
    
    findStudiesByUserId(): Promise<Study[]> {
        return this.http.get<Study[]>(AppUtils.BACKEND_API_STUDY_URL)
        .toPromise()
        .then(entities => entities.map((entity) => Object.assign(new Study(), entity)));
    }

    getStudiesNames(): Promise<IdName[]> {
        return this.http.get<IdName[]>(AppUtils.BACKEND_API_STUDY_ALL_NAMES_URL)
            .toPromise();
    }

    getChallenges(): Promise<IdName[]> {
        return this.http.get<IdName[]>(AppUtils.BACKEND_API_STUDY_CHALLENGES_URL)
            .toPromise().then((typeResult: IdName[]) => {
                return typeResult;
            });
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

    private findStudiesIcanAdmin(): Promise<Study[]> {
        if (this.keycloakService.isUserAdmin()) {
            return this.getAll();
        } else {
            return this.getAll().then(studies => {
                const myId: number = KeycloakService.auth.userId;
                return studies.filter(study => {
                    return study.studyUserList.filter(su => su.userId == myId && su.studyUserRights.includes(StudyUserRight.CAN_ADMINISTRATE)).length > 0;
                });
            });
        }
    }

    findStudyIdsIcanAdmin(): Promise<number[]> {
        return this.findStudiesIcanAdmin().then(studies => studies.map(study => study.id));
    }

    findStudyIdNamesIcanAdmin(): Promise<IdName[]> {
        return this.findStudiesIcanAdmin().then(studies => studies.map(study => new IdName(study.id, study.name)));
    }

    uploadFile(fileToUpload: File, studyId: number, fileType: 'protocol-file'|'dua'): Observable<any> {
        const endpoint = this.API_URL + '/' + fileType + '-upload/' + studyId;
        const formData: FormData = new FormData();
        if (fileType == 'dua') {
            formData.append('file', fileToUpload, 'DUA-' + fileToUpload.name);
        } else if (fileType == 'protocol-file') {
            formData.append('file', fileToUpload, fileToUpload.name);
        }
        return this.http.post<any>(endpoint, formData);
    }

    deleteFile(studyId: number, fileType: 'protocol-file'|'dua'): Observable<any> {
        const endpoint = this.API_URL + '/' + fileType + '-delete/' + studyId;
        return this.http.delete(endpoint);
    }

    downloadFile(fileName: string, studyId: number, fileType: 'protocol-file'|'dua'): void {
        this.downloadBlob(fileName, studyId, fileType).then(response => {
            if (response.status == 200) {
                this.downloadIntoBrowser(response);
            }
        })
    }

    downloadBlob(fileName: string, studyId: number, fileType: 'protocol-file'|'dua'): Promise<HttpResponse<Blob>> {
        const endpoint = this.API_URL + '/' + fileType + '-download/' + studyId + "/" + fileName + "/";
        return this.http.get(endpoint, { observe: 'response', responseType: 'blob' }).toPromise();
    }

    getMyDUA(): Promise<DataUserAgreement[]> {
        return this.http.get<DataUserAgreement[]>(AppUtils.BACKEND_API_STUDY_URL + '/dua')
                .toPromise()
                .then(duas => {
                    this._duasToSign = duas ? duas.length : 0;
                    return duas;
                });
    }

    get duasToSign(): number {
        return this._duasToSign;
    }

    acceptDUA(duaId: number): Promise<void> {
        return this.http.put<any>(AppUtils.BACKEND_API_STUDY_URL + '/dua/' + duaId, null)
                .toPromise()
                .then(() => {
                    this.getMyDUA();
                });
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

    protected getIgnoreList(): string[] {
        return super.getIgnoreList().concat(['completeMembers']);
    }

    public stringify(entity: Study) {
        let dto = new StudyDTO(entity);
        return JSON.stringify(dto, (key, value) => {
            return this.customReplacer(key, value, dto);
        });
    }
}