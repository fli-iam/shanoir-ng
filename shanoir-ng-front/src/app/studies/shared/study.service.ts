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
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, OnDestroy } from '@angular/core';
import { Observable, Subscription } from 'rxjs';

import { TaskState } from 'src/app/async-tasks/task.model';
import { SingleDownloadService } from 'src/app/shared/mass-download/single-download.service';
import { Tag } from 'src/app/tags/tag.model';
import { DataUserAgreement } from '../../dua/shared/dua.model';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { IdName } from '../../shared/models/id-name.model';
import { Profile } from '../../shared/models/profile.model';
import * as AppUtils from '../../utils/app.utils';
import { StudyUserRight } from './study-user-right.enum';
import { StudyUser } from "./study-user.model";
import {
    CenterStudyDTO,
    StudyDTO,
    StudyDTOService,
    StudyLight,
    StudyStorageVolumeDTO
} from './study.dto';
import { Study } from './study.model';
import {SubjectDTO} from "../../subjects/shared/subject.dto";
import {Subject} from "../../subjects/shared/subject.model";

@Injectable()
export class StudyService extends EntityService<Study> implements OnDestroy {

    API_URL = AppUtils.BACKEND_API_STUDY_URL;
    private _duasToSign: number = 0;
    subscriptions: Subscription[] = [];
    fileUploads: Map<number, Promise<void>> = new Map(); // current uploads
    private studyVolumesCache: Map<number, StudyStorageVolumeDTO> = new Map();

    constructor(protected http: HttpClient, private keycloakService: KeycloakService, private studyDTOService: StudyDTOService,
            private downloadService: SingleDownloadService) {
        super(http);
    }

    getEntityInstance() { return new Study(); }

    get(id: number, mode: 'eager' | 'lazy' = 'eager', withStorageVolume = false): Promise<Study> {
        return this.http.get<any>(this.API_URL + '/' + id
            + (withStorageVolume ? '?withStorageVolume=true' : ''))
            .toPromise()
            .then(this.mapEntity);
    }

    getStudiesLight(): Promise<StudyLight[]> {
      return this.http.get<StudyLight[]>(AppUtils.BACKEND_API_STUDY_STUDIES_LIGHT_URL)
        .toPromise().then((typeResult: StudyLight[]) => {
          return typeResult;
        });
    }

    findStudiesByUserId(): Promise<Study[]> {
        return this.http.get<Study[]>(AppUtils.BACKEND_API_STUDY_URL)
        .toPromise()
        .then(entities => entities?.map((entity) => Object.assign(new Study(), entity)) || []);
    }

    getStudiesNames(): Promise<IdName[]> {
        return this.http.get<IdName[]>(AppUtils.BACKEND_API_STUDY_ALL_NAMES_URL)
            .toPromise();
    }

    getStudyNamesAndCenters(): Promise<Study[]> {
        return this.http.get<CenterStudyDTO[]>(AppUtils.BACKEND_API_STUDY_URL + '/namesAndCenters')
            .toPromise().then(dtos => dtos.map(dto => StudyDTOService.centerStudyDTOtoStudy(dto)));
    }

    getStudiesProfiles(): Promise<Profile[]> {
      return this.http.get<Profile[]>(AppUtils.BACKEND_API_PROFILE_ALL_PROFILES_URL)
        .toPromise();
    }

    getPublicStudiesData(): Promise<StudyLight[]> {
      return this.http.get<StudyLight[]>(AppUtils.BACKEND_API_STUDY_PUBLIC_STUDIES_DATA_URL)
        .toPromise().then((typeResult: StudyLight[]) => {
          return typeResult;
        });
    }

    getChallenges(): Promise<IdName[]> {
        return this.http.get<IdName[]>(AppUtils.BACKEND_API_STUDY_CHALLENGES_URL)
            .toPromise().then((typeResult: IdName[]) => {
                return typeResult;
            });
    }

    getPublicStudiesConnected(): Promise<IdName[]> {
        return this.http.get<IdName[]>(AppUtils.BACKEND_API_STUDY_PUBLIC_STUDIES_CONNECTED_URL)
            .toPromise().then((typeResult: IdName[]) => {
                return typeResult;
            });
    }

    getStudyUserFromStudyId(studyId: number): Promise<StudyUser[]> {
        return this.http.get<StudyUser[]>(AppUtils.BACKEND_API_STUDY_DELETE_USER + '/' + studyId)
            .toPromise().then((su : StudyUser[]) => {
                return su;
            });
    }

    findSubjectsByStudyId(studyId: number): Promise<Subject[]> {
        return this.http.get<SubjectDTO[]>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + studyId + '/allSubjects')
            .toPromise().then(this.mapSubjectList);
    }

    findSubjectsByStudyIdPreclinical(studyId: number, preclinical: boolean): Promise<Subject[]> {
        return this.http.get<SubjectDTO[]>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + studyId + '/allSubjects?preclinical=' + preclinical)
            .toPromise().then(this.mapSubjectList);
    }

    private findStudiesIcanAdmin(): Promise<Study[]> {
        if (this.keycloakService.isUserAdmin()) {
            return this.getAll();
        } else {
            return this.getAll().then(studies => {
                const myId: number = KeycloakService.auth.userId;
                return studies?.filter(study => {
                    return study.studyUserList.filter(su => su.userId == myId && su.studyUserRights.includes(StudyUserRight.CAN_ADMINISTRATE)).length > 0;
                });
            });
        }
    }

    findStudyIdsIcanAdmin(): Promise<number[]> {
        return this.findStudiesIcanAdmin().then(studies => studies?.map(study => study.id));
    }

    findStudyIdNamesIcanAdmin(): Promise<IdName[]> {
        return this.findStudiesIcanAdmin().then(studies => studies?.map(study => new IdName(study.id, study.name)));
    }

    uploadFile(fileToUpload: File, studyId: number, fileType: 'protocol-file'|'dua'): Promise<any> {
        const endpoint = this.API_URL + '/' + fileType + '-upload/' + studyId;
        const formData: FormData = new FormData();
        if (fileType == 'dua') {
            formData.append('file', fileToUpload, 'DUA-' + fileToUpload.name);
        } else if (fileType == 'protocol-file') {
            formData.append('file', fileToUpload, fileToUpload.name);
        }
        const promise: Promise<void> = this.http.post<any>(endpoint, formData).toPromise();
        // keep a track on the current uploadings
        if (this.fileUploads.has(studyId)) {
            this.fileUploads.set(studyId, Promise.all([this.fileUploads.get(studyId), promise]).then(() => null));
        } else {
            this.fileUploads.set(studyId, promise);
        }
        return promise;
    }


    deleteFile(studyId: number, fileType: 'protocol-file'|'dua'): Observable<any> {
        const endpoint = this.API_URL + '/' + fileType + '-delete/' + studyId;
        return this.http.delete(endpoint);
    }

    downloadProtocolFile(fileName: string, studyId: number, state?: TaskState) {
        const endpoint = this.API_URL + '/protocol-file-download/' + studyId + "/" + fileName + "/";
        return this.downloadService.downloadSingleFile(endpoint, null, state);
    }

    buildProtocolFileUrl(fileName: string, studyId: number): string {
        return this.API_URL + '/protocol-file-download/' + studyId + "/" + fileName;
    }

    downloadDuaFile(fileName: string, studyId: number, state?: TaskState) {
        const endpoint = this.API_URL + '/dua-download/' + studyId + "/" + fileName + "/";
        return this.downloadService.downloadSingleFile(endpoint, null, state);
    }

    downloadDuaBlob(fileName: string, studyId: number): Promise<Blob> {
        const endpoint = this.API_URL + '/dua-download/' + studyId + "/" + fileName + "/";
        let params: HttpParams = new HttpParams();
        //params
        return AppUtils.downloadBlob(endpoint);
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

    hasDUAByStudyId(studyId: number): Promise<boolean> {
        return this.http.get<boolean>(AppUtils.BACKEND_API_STUDY_URL + '/dua/study/' + studyId)
            .toPromise()
            .then(dua => {
                return dua;
            });
    }

    deleteUserFromStudy(studyId: number, userId: number): Promise<void> {
      return this.http.delete<void>(AppUtils.BACKEND_API_STUDY_DELETE_USER + "/" + studyId + "/" + userId)
        .toPromise();
    }

    exportBIDSByStudyId(studyId: number) {
        if (!studyId) throw Error('study id is required');
        this.http.get(AppUtils.BACKEND_API_BIDS_EXPORT_URL + '/studyId/' + studyId, {
            reportProgress: true,
            observe: 'events',
            responseType: 'blob'
        });
    }

    protected getIgnoreList(): string[] {
        return super.getIgnoreList().concat(['completeMembers']);
    }

    public stringify(entity: Study) {
        console.log("stringify study : ", entity);
        let dto = new StudyDTO(entity);
        let test = JSON.stringify(dto, (key, value) => {
            return this.customReplacer(key, value, dto);
        });
        return test;
    }

    protected mapEntity = (dto: StudyDTO, result?: Study): Promise<Study> => {
        if (result == undefined) result = this.getEntityInstance();
        return this.studyDTOService.toEntity(dto, result);
    }

    protected mapEntityList = (dtos: StudyDTO[], result?: Study[]): Promise<Study[]> => {
        if (result == undefined) result = [];
        if (dtos) return this.studyDTOService.toEntityList(dtos, result);
    }

    private mapSubjectList = (dtos: SubjectDTO[], result?: Subject[]): Promise<Subject[]> => {
        if (result == undefined) result = [];
        if (dtos) return this.studyDTOService.toSubjectList(dtos, result);
    }

    ngOnDestroy() {
        for(let subscribtion of this.subscriptions) {
            subscribtion.unsubscribe();
        }
    }

    getStudyDetailedStorageVolume(id: number): Promise<StudyStorageVolumeDTO> {
        return this.http.get<StudyStorageVolumeDTO>(AppUtils.BACKEND_API_STUDY_URL + '/detailedStorageVolume/' + id)
            .toPromise();
    }

    getStudiesStorageVolume(ids: number[]): Promise<Map<number, StudyStorageVolumeDTO>> {
        // separate cached and uncached volumes
        let cachedVolumes: Map<number, StudyStorageVolumeDTO> = new Map();
        ids.forEach(id => {
            if (this.studyVolumesCache.has(id)) {
                cachedVolumes.set(id, this.studyVolumesCache.get(id));
            }
        });
        ids = ids.filter(id => !cachedVolumes.has(id));
        let rets: Promise<Map<number, StudyStorageVolumeDTO>>[] = [];
        if (cachedVolumes.size > 0) rets.push(Promise.resolve(cachedVolumes));

        if (ids.length > 0) { // fetch volumes from server
            const formData: FormData = new FormData();
            formData.set('studyIds', ids.join(","));
            rets.push(this.http.post<Map<number, StudyStorageVolumeDTO>>(AppUtils.BACKEND_API_STUDY_URL + '/detailedStorageVolume', formData)
                .toPromise()
                .then(volumes => {
                    return volumes ? Object.entries(volumes).reduce((map: Map<number, StudyStorageVolumeDTO>, entry) => map.set(parseInt(entry[0]), entry[1]), new Map()) : new Map();
                }).then(volumes => {
                    volumes.forEach((value, key) => {
                        this.studyVolumesCache.set(key, value);
                    });
                    return volumes;
                })
            );
        }
        // aggregate results
        return Promise.all(rets).then(results => {
            let totalVolumes: Map<number, StudyStorageVolumeDTO> = new Map();
            results?.forEach(result => {
                result.forEach((val, key) => totalVolumes.set(key, val));
            });
            return totalVolumes;
        });

    }

    storageVolumePrettyPrint(size: number) {
        return AppUtils.getSizeStr(size);
    }

    getTagsFromStudyId(studyId: number): Promise<Tag[]> {
        return this.http.get<any[]>(AppUtils.BACKEND_API_STUDY_URL + '/tags/' + studyId)
            .toPromise()
            .then(dtos => dtos?.map(dto => StudyDTOService.tagDTOToTag(dto)));
    }

    getStudiesByRight(right: StudyUserRight): Promise<number[]> {
        return this.http.get<any[]>(AppUtils.BACKEND_API_STUDY_URL + '/studyUser/right/' + right)
            .toPromise();
    }
}
