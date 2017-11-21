import { Injectable } from '@angular/core';
import { Response, Http } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Center } from './center.model';
import * as AppUtils from '../../utils/app.utils';
import { HandleErrorService } from '../../shared/utils/handle.error.service';

@Injectable()
export class CenterService {
    constructor(private http: Http, private handleErrorService: HandleErrorService) { }

    getCenters(): Promise<Center[]> {
        return this.http.get(AppUtils.BACKEND_API_CENTER_URL)
            .toPromise()
            .then(response => response.json() as Center[])
            .catch((error) => {
                console.error('Error while getting centers', error);
                return Promise.reject(error.message || error);
        });
    }

    getCentersNames(): Promise<Center[]> {
        return this.http.get(AppUtils.BACKEND_API_CENTER_NAMES_URL)
            .toPromise()
            .then(response => response.json() as Center[])
            .catch((error) => {
                console.error('Error while getting centers', error);
                return Promise.reject(error.message || error);
        });
    }

    delete(id: number): Promise<Response> {
        return this.http.delete(AppUtils.BACKEND_API_CENTER_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error delete center', error);
                return Promise.reject(error);
        });
    }

    getCenter (id: number): Promise<Center> {
        return this.http.get(AppUtils.BACKEND_API_CENTER_URL + '/' + id)
            .toPromise()
            .then(res => res.json() as Center)
            .catch((error) => {
                console.error('Error while getting center', error);
                return Promise.reject(error.message || error);
        });
    }

    create(center: Center): Observable<Center> {
        return this.http.post(AppUtils.BACKEND_API_CENTER_URL, JSON.stringify(center))
            .map(this.handleErrorService.extractData)
            .catch(this.handleErrorService.handleError);
    }

    update(id: number, center: Center): Observable<Center> {
        return this.http.put(AppUtils.BACKEND_API_CENTER_URL + '/' + id, JSON.stringify(center))
            .map(response => response.json() as Center)
            .catch(this.handleErrorService.handleError);
    }
}