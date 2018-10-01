import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { IdNameObject } from '../../shared/models/id-name-object.model';
import * as AppUtils from '../../utils/app.utils';
import { Center } from './center.model';

@Injectable()
export class CenterService {
    constructor(private http: HttpClient) { }

    getCenters(): Promise<Center[]> {
        return this.http.get<Center[]>(AppUtils.BACKEND_API_CENTER_URL)
            .map(centers => centers.map((center) => Object.assign(new Center(), center))) 
            .toPromise();
    }

    getCentersNames(): Promise<Center[]> {
        return this.http.get<Center[]>(AppUtils.BACKEND_API_CENTER_NAMES_URL)
            .toPromise();
    }

    getCentersNamesForExamination(): Promise<IdNameObject[]> {
        return this.http.get<IdNameObject[]>(AppUtils.BACKEND_API_CENTER_NAMES_URL)
            .toPromise();
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_CENTER_URL + '/' + id)
            .toPromise();
    }

    getCenter(id: number): Promise<Center> {
        return this.http.get<Center>(AppUtils.BACKEND_API_CENTER_URL + '/' + id)
            .map(center =>  Object.assign(new Center(), center))
            .toPromise();
    }

    create(center: Center): Promise<Center> {
        return this.http.post<Center>(AppUtils.BACKEND_API_CENTER_URL, JSON.stringify(center))
            .map(center =>  Object.assign(new Center(), center))
            .toPromise();
    }

    update(id: number, center: Center): Promise<void> {
        return this.http.put<void>(AppUtils.BACKEND_API_CENTER_URL + '/' + id, JSON.stringify(center))
            .toPromise();
    }
}