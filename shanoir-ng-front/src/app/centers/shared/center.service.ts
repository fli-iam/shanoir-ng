import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

import { Center } from './center.model';
import * as AppUtils from '../../utils/app.utils';
import { HandleErrorService } from '../../shared/utils/handle-error.service';
import { IdNameObject } from '../../shared/models/id-name-object.model';

@Injectable()
export class CenterService {
    constructor(private http: HttpClient, private handleErrorService: HandleErrorService) { }

    getCenters(): Promise<Center[]> {
        return this.http.get<Center[]>(AppUtils.BACKEND_API_CENTER_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting centers', error);
                return Promise.reject(error.message || error);
            });
    }

    getCentersNames(): Promise<Center[]> {
        return this.http.get<Center[]>(AppUtils.BACKEND_API_CENTER_NAMES_URL)
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

    getCenter(id: number): Promise<Center> {
        return this.http.get<Center>(AppUtils.BACKEND_API_CENTER_URL + '/' + id)
            .toPromise()
            .then(res => res)
            .catch((error) => {
                console.error('Error while getting center', error);
                return Promise.reject(error.message || error);
            });
    }

    create(center: Center): Observable<Center> {
        return this.http.post<Center>(AppUtils.BACKEND_API_CENTER_URL, JSON.stringify(center))
            .map(res => res)
            .catch(this.handleErrorService.handleError);
    }

    update(id: number, center: Center): Observable<Center> {
        return this.http.put<Center>(AppUtils.BACKEND_API_CENTER_URL + '/' + id, JSON.stringify(center))
            .map(response => response)
            .catch(this.handleErrorService.handleError);
    }
}