import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { EquipmentDicom } from '../../import/dicom-data.model';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import * as AppUtils from '../../utils/app.utils';
import { Study } from './study.model';

@Injectable()
export class StudyService {
    constructor(private http: HttpClient, private msgBoxService: MsgBoxService) { }
    
    findStudiesByUserId(): Promise<Study[]> {
        return this.http.get<Study[]>(AppUtils.BACKEND_API_STUDY_URL)
        .map(entities => entities.map((entity) => Object.assign(new Study(), entity)))
        .toPromise();
    }
    
    findStudiesWithStudyCardsByUserAndEquipment(equipment: EquipmentDicom): Promise<Study[]> {
        return this.http.post<Study[]>(AppUtils.BACKEND_API_STUDY_WITH_CARDS_BY_USER_EQUIPMENT_URL, JSON.stringify(equipment))
            .map(entities => entities.map((entity) => Object.assign(new Study(), entity)))
            .toPromise();
    }

    create(study: Study): Promise<Study> {
        return this.http.post<Study>(AppUtils.BACKEND_API_STUDY_URL, JSON.stringify(study))
        .map((entity) => Object.assign(new Study(), entity))
        .toPromise();
    }
    
    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_STUDY_URL + '/' + id)
        .toPromise();
    }
    
    getStudies(): Promise<Study[]> {
        return this.http.get<Study[]>(AppUtils.BACKEND_API_STUDY_URL)
            .map(entities => entities.map((entity) => Object.assign(new Study(), entity)))
            .toPromise();
    }
    
    getStudiesNames(): Promise<IdNameObject[]> {
        return this.http.get<IdNameObject[]>(AppUtils.BACKEND_API_STUDY_ALL_NAMES_URL)
            .toPromise();
    }
    
    findSubjectsByStudyId(studyId: number): Promise<SubjectWithSubjectStudy[]> {
        return this.http.get<SubjectWithSubjectStudy[]>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + studyId + '/allSubjects')
            .toPromise();
    }
    
    getStudy(id: number): Promise<Study> {
        return this.http.get<Study>(AppUtils.BACKEND_API_STUDY_URL + '/' + id)
            .map((entity) => Object.assign(new Study(), entity))
            .toPromise();
    }
    
    update(id: number, study: Study): Promise<void> {
        return this.http.put<void>(AppUtils.BACKEND_API_STUDY_URL + '/' + id, JSON.stringify(study))
            .toPromise();
    }
}