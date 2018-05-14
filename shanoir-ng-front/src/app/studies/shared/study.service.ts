import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { Study } from './study.model';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import * as AppUtils from '../../utils/app.utils';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { EquipmentDicom } from "../../import/dicom-data.model";

@Injectable()
export class StudyService {
    constructor(private http: HttpClient) { }

    findStudiesByUserId(): Promise<Study[]> {
        return this.http.get<Study[]>(AppUtils.BACKEND_API_STUDY_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting studies by user id', error);
                return Promise.reject(error.message || error);
            });
    }

    findStudiesWithStudyCardsByUserAndEquipment(equipment: EquipmentDicom): Promise<Study[]> {
        return this.http.post<Study[]>(AppUtils.BACKEND_API_STUDY_WITH_CARDS_BY_USER_EQUIPMENT_URL, JSON.stringify(equipment))
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting studies by user and equipment', error);
                return Promise.reject(error.message || error);
            });
    }

    create(study: Study): Observable<Study> {
        return this.http.post<Study>(AppUtils.BACKEND_API_STUDY_URL, JSON.stringify(study))
            .map(res => res);
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_STUDY_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete study', error);
                return Promise.reject(error);
            });
    }

    getStudies(): Promise<Study[]> {
        return this.http.get<Study[]>(AppUtils.BACKEND_API_STUDY_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting studies', error);
                return Promise.reject(error.message || error);
            });
    }

    getStudiesNames(): Promise<IdNameObject[]> {
        return this.http.get<IdNameObject[]>(AppUtils.BACKEND_API_STUDY_ALL_NAMES_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting studies', error);
                return Promise.reject(error.message || error);
            });
    }

    findSubjectsByStudyId(studyId: number): Promise<SubjectWithSubjectStudy[]> {
        return this.http.get<SubjectWithSubjectStudy[]>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + studyId + '/allSubjects')
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting subjects by study id', error);
                return Promise.reject(error.message || error);
            });
    }
    
    getStudy(id: number, withData: boolean): Promise<Study> {
        return this.http.get<Study>(AppUtils.BACKEND_API_STUDY_URL + '/' + id + '?withdata=' + withData)
            .toPromise()
            .then(res => res)
            .catch((error) => {
                console.error('Error while getting study', error);
                return Promise.reject(error.message || error);
            });
    }

    update(id: number, study: Study): Observable<Study> {
        return this.http.put<Study>(AppUtils.BACKEND_API_STUDY_URL + '/' + id, JSON.stringify(study))
            .map(response => response);
    }
}