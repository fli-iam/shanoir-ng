import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { Subject } from './subject.model';
import * as AppUtils from '../../utils/app.utils';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { SubjectStudy } from "./subject-study.model";

@Injectable()
export class SubjectService {
    constructor(private http: HttpClient) { }

    getSubjects(): Promise<Subject[]> {
        return this.http.get<Subject[]>(AppUtils.BACKEND_API_SUBJECT_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting subjects', error);
                return Promise.reject(error.message || error);
            });
    }

    getCentersNames(): Promise<Subject[]> {
        return this.http.get<Subject[]>(AppUtils.BACKEND_API_CENTER_NAMES_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting centers', error);
                return Promise.reject(error.message || error);
            });
    }

    getCentersNamesForExamination(): Promise<IdNameObject[]> {
        return this.http.get<IdNameObject[]>(AppUtils.BACKEND_API_CENTER_NAMES_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting centers', error);
                return Promise.reject(error.message || error);
            });
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_CENTER_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete center', error);
                return Promise.reject(error);
            });
    }

    getSubject(id: number): Promise<Subject> {
        return this.http.get<Subject>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + id)
            .toPromise()
            .then(res => res)
            .catch((error) => {
                console.error('Error while getting subject', error);
                return Promise.reject(error.message || error);
            });
    }

    create(subject: Subject): Observable<Subject> {
        return this.http.post<Subject>(AppUtils.BACKEND_API_SUBJECT_URL, JSON.stringify(subject))
            .map(res => res);
    }

    update(id: number, subject: Subject): Observable<Subject> {
        return this.http.put<Subject>(AppUtils.BACKEND_API_SUBJECT_URL + '/' + id, JSON.stringify(subject))
            .map(response => response);
    }

    updateSubjectStudyValues(subjectStudy: SubjectStudy): Observable<SubjectStudy> {
        return this.http.put<SubjectStudy>(AppUtils.BACKEND_API_SUBJECT_STUDY_URL + '/' + subjectStudy.id, JSON.stringify(subjectStudy))
            .map(res => res);
    }
}