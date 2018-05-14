import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { ErrorObservable } from 'rxjs/observable/ErrorObservable';

import { Dataset } from './dataset.model';
import * as AppUtils from '../../utils/app.utils';

@Injectable()
export class DatasetService {

    constructor(private http: HttpClient) { }

    create(dataset: Dataset): Observable<Dataset> {
        return this.http.post<Dataset>(AppUtils.BACKEND_API_DATASET_URL, JSON.stringify(dataset))
            .map(res => res);
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_DATASET_URL + '/' + id)
            .toPromise()
            .catch((error) => {
                console.error('Error deleting dataset', error);
                return Promise.reject(error.message || error);
            });
    }

    get(id: number): Promise<Dataset> {
        return this.http.get<Dataset>(AppUtils.BACKEND_API_DATASET_URL + '/' + id)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting dataset', error);
                return Promise.reject(error.message || error);
            });
    }

    getAll(): Promise<Dataset[]> {
        return this.http.get<Dataset[]>(AppUtils.BACKEND_API_DATASET_URL)
            .toPromise()
            .then(response => response)
            .catch((error) => {
                console.error('Error while getting datasets', error);
                return Promise.reject(error.message || error);
            });
    }

    update(id: number, dataset: Dataset): Observable<Dataset> {
        return this.http.put<Dataset>(AppUtils.BACKEND_API_DATASET_URL + '/' + id, JSON.stringify(dataset))
            .map(response => response);
    }

}