import { Injectable } from '@angular/core';
import { Response, Http } from '@angular/http';

import { Center } from './center.model';
import * as AppUtils from 'app/utils/app.utils';

@Injectable()
export class CenterService {
    constructor(private http: Http) { }

    getCenters(): Promise<Center[]> {
        return this.http.get(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_CENTER_ALL_URL)
            .toPromise()
            .then(response => response.json() as Center[])
            .catch((error) => {
                console.error('Error while getting centers', error);
                return Promise.reject(error.message || error);
        });
    }
}