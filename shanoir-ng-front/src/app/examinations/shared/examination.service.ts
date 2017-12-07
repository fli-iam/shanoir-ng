import { Injectable } from '@angular/core';
import { Response, Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Examination } from './examination.model';
import * as AppUtils from '../../utils/app.utils';
import { HandleErrorService } from '../../shared/utils/handle-error.service';

@Injectable()
export class ExaminationService {
    constructor(private http: Http, private handleErrorService: HandleErrorService) { }

    getExaminations(): Promise<Examination[]> {
        return this.http.get(AppUtils.BACKEND_API_EXAMINATION_URL)
            .toPromise()
            .then(response => response.json() as Examination[])
            .catch((error) => {
                console.error('Error while getting examinations', error);
                return Promise.reject(error.message || error);
        });
    }

    delete(id: number): Promise<Response> {
        return this.http.delete(AppUtils.BACKEND_API_EXAMINATION_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete examination', error);
                return Promise.reject(error.message || error);
        });
    }

    getExamination (id: number): Promise<Examination> {
        return this.http.get(AppUtils.BACKEND_API_EXAMINATION_URL + '/' + id)
            .toPromise()
            .then(res => res.json() as Examination)
            .catch((error) => {
                console.error('Error while getting examination', error);
                return Promise.reject(error.message || error);
        });
    }

    create(examination: Examination): Observable<Examination> {
        return this.http.post(AppUtils.BACKEND_API_EXAMINATION_URL, JSON.stringify(examination))
            .map(this.handleErrorService.extractData)
            .catch(this.handleErrorService.handleError);
    }

    update(id: number, examination: Examination): Observable<Examination> {
        return this.http.put(AppUtils.BACKEND_API_EXAMINATION_URL + '/' + id, JSON.stringify(examination))
            .map(response => response.json() as Examination)
            .catch(this.handleErrorService.handleError);
    }

    findExaminationsBySubjectId(subjectId: number): Promise<Examination[]> {
        return this.http.get(AppUtils.BACKEND_API_EXAMINATION_ALL_BY_SUBJECT_URL + '/' + subjectId)
            .toPromise()
            .then(response => response.json() as Examination[])
            .catch((error) => {
                console.error('Error while getting examinations by subject id', error);
                return Promise.reject(error.message || error);
        });
    }
}