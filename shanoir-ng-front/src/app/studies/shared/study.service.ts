import { Injectable } from '@angular/core';
import { Response, Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Study } from './study.model';
import { Subject } from '../../subjects/shared/subject.model';
import * as AppUtils from '../../utils/app.utils';
import { HandleErrorService } from '../../shared/utils/handle.error.service';

@Injectable()
export class StudyService {
    constructor(private http: Http, private handleErrorService: HandleErrorService) { }

    findStudiesByUserId(): Promise<Study[]> {
        return this.http.get(AppUtils.BACKEND_API_STUDY_ALL_BY_USER_URL)
            .toPromise()
            .then(response => response.json() as Study[])
            .catch((error) => {
                console.error('Error while getting studies by user id', error);
                return Promise.reject(error.message || error);
        });
    }

    findStudiesWithStudyCardsByUserId(): Promise<Study[]> {
        return this.http.get(AppUtils.BACKEND_API_STUDY_WITH_CARDS_BY_USER_URL)
            .toPromise()
            .then(response => response.json() as Study[])
            .catch((error) => {
                console.error('Error while getting studies by user id', error);
                return Promise.reject(error.message || error);
        });
    }

    findSubjectsByStudyId(studyId: number): Promise<Subject[]> {
        return this.http.get(AppUtils.BACKEND_API_SUBJECT_URL + '/' + studyId + '/allSubjects')
            .toPromise()
            .then(response => response.json() as Subject[])
            .catch((error) => {
                console.error('Error while getting subjects by study id', error);
                return Promise.reject(error.message || error);
        });
    }
}