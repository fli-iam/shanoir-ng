import { Injectable } from '@angular/core';
import { Response, Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Study } from './study.model';
import * as AppUtils from '../../utils/app.utils';
import { HandleErrorService } from '../../shared/utils/handle.error.service';

@Injectable()
export class StudyService {
    constructor(private http: Http, private handleErrorService: HandleErrorService) { }

    create(study: Study): Observable<Study> {
        return this.http.post(AppUtils.BACKEND_API_STUDY_URL, JSON.stringify(study))
            .map(this.handleErrorService.extractData)
            .catch(this.handleErrorService.handleError);
    }

    delete(id: number): Promise<Response> {
        return this.http.delete(AppUtils.BACKEND_API_STUDY_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete study', error);
                return Promise.reject(error);
        });
    }

    getStudies(): Promise<Study[]> {
        return this.http.get(AppUtils.BACKEND_API_STUDY_URL)
            .toPromise()
            .then(response => response.json() as Study[])
            .catch((error) => {
                console.error('Error while getting studies', error);
                return Promise.reject(error.message || error);
        });
    }

    getStudy (id: number): Promise<Study> {
        return this.http.get(AppUtils.BACKEND_API_STUDY_URL + '/' + id)
            .toPromise()
            .then(res => res.json() as Study)
            .catch((error) => {
                console.error('Error while getting study', error);
                return Promise.reject(error.message || error);
        });
    }

    update(id: number, study: Study): Observable<Study> {
        return this.http.put(AppUtils.BACKEND_API_STUDY_URL + '/' + id, JSON.stringify(study))
            .map(response => response.json() as Study)
            .catch(this.handleErrorService.handleError);
    }
}