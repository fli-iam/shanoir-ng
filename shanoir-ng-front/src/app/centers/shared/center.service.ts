import { Injectable } from '@angular/core';
import { Response, Http } from '@angular/http';

import { Center } from './center.model';
import * as AppUtils from '../../utils/app.utils';

@Injectable()
export class CenterService {
    constructor(private http: Http) { }

    getCenters(): Promise<Center[]> {
        return this.http.get(AppUtils.BACKEND_API_CENTER_ALL_URL)
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
                return Promise.reject(error.message || error);
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
}