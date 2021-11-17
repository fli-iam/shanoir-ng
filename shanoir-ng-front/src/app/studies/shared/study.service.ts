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
import { HttpClient, HttpEvent, HttpEventType, HttpResponse } from '@angular/common/http';
import { Injectable, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { Observable } from 'rxjs/Observable';

import { BidsElement } from '../../bids/model/bidsElement.model';
import { DataUserAgreement } from '../../dua/shared/dua.model';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { LoadingBarComponent } from '../../shared/components/loading-bar/loading-bar.component';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { IdName } from '../../shared/models/id-name.model';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import * as AppUtils from '../../utils/app.utils';
import { StudyUserRight } from './study-user-right.enum';
import { CenterStudyDTO, StudyDTO, StudyDTOService, SubjectWithSubjectStudyDTO } from './study.dto';
import { Study } from './study.model';


@Injectable()
export class StudyService extends EntityService<Study> implements OnDestroy {

    API_URL = AppUtils.BACKEND_API_STUDY_URL;

    private _duasToSign: number = 0;
    
    subscribtions: Subscription[] = [];

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
        return this.http.get<CenterStudyDTO[]>(AppUtils.BACKEND_API_STUDY_ALL_NAMES_AND_CENTERS_URL)
            .toPromise().then(dtos => dtos.map(dto => StudyDTOService.centerStudyDTOtoStudy(dto)));
    }
    
    findSubjectsByStudyId(studyId: number): Promise<SubjectWithSubjectStudy[]> {
        return this.http.get<SubjectWithSubjectStudyDTO[]>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + studyId + '/allSubjects')
            .toPromise().then(this.mapSubjectWithSubjectStudyList);
    }

    findSubjectsByStudyIdPreclinical(studyId: number, preclinical: boolean): Promise<SubjectWithSubjectStudy[]> {
        return this.http.get<SubjectWithSubjectStudyDTO[]>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + studyId + '/allSubjects?preclinical=' + preclinical)
            .toPromise().then(this.mapSubjectWithSubjectStudyList);
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

    downloadFile(fileName: string, studyId: number, fileType: 'protocol-file'|'dua', progressBar: LoadingBarComponent): Promise<HttpResponse<Blob>> {
       const endpoint = this.API_URL + '/' + fileType + '-download/' + studyId + "/" + fileName + "/";
       if (progressBar) {
           this.subscribtions.push(
           this.http.get(endpoint, {
                    reportProgress: true,
                    observe: 'events',
                    responseType: 'blob'
                }).subscribe((event: HttpEvent<any>) => this.progressBarFunc(event, progressBar))
           );
        } else {
            return this.http.get(endpoint, {
                    observe: 'response',
                    responseType: 'blob'
                }).toPromise();

        }
        return null;
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

    progressBarFunc(event: HttpEvent<any>, progressBar: LoadingBarComponent): void {
       switch (event.type) {
            case HttpEventType.Sent:
              progressBar.progress = -1;
              break;
            case HttpEventType.DownloadProgress:
              progressBar.progress = (event.loaded / event.total);
              break;
            case HttpEventType.Response:
                saveAs(event.body, this.getFilename(event));
                progressBar.progress = 0;
        }
    }

    exportBIDSByStudyId(studyId: number, progressBar: LoadingBarComponent) {
        if (!studyId) throw Error('study id is required');
        this.subscribtions.push(
               this.http.get(AppUtils.BACKEND_API_STUDY_BIDS_EXPORT_URL + '/studyId/' + studyId, {
                    reportProgress: true,
                    observe: 'events',
                    responseType: 'blob'
                }).subscribe((event: HttpEvent<any>) => this.progressBarFunc(event, progressBar))
         );
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
        let test = JSON.stringify(dto, (key, value) => {
            return this.customReplacer(key, value, dto);
        });
        return test;
    }

    protected mapEntity = (dto: StudyDTO, result?: Study): Promise<Study> => {
        if (result == undefined) result = this.getEntityInstance();
        return StudyDTOService.toEntity(dto, result);
    }

    protected mapEntityList = (dtos: StudyDTO[], result?: Study[]): Promise<Study[]> => {
        if (result == undefined) result = [];
        if (dtos) return StudyDTOService.toEntityList(dtos, result);
    }

    private mapSubjectWithSubjectStudyList = (dtos: SubjectWithSubjectStudyDTO[], result?: SubjectWithSubjectStudy[]): Promise<SubjectWithSubjectStudy[]> => {
        if (result == undefined) result = [];
        if (dtos) return StudyDTOService.toSubjectWithSubjectStudyList(dtos, result);
    }

    ngOnDestroy() {
        for(let subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
    }
}