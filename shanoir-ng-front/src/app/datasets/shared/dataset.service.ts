import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
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

    update(dataset: Dataset): Observable<Dataset> {
        if (!dataset.id) throw Error('Cannot update a dataset without an id');
        return this.http.put<Dataset>(AppUtils.BACKEND_API_DATASET_URL + '/' + dataset.id, JSON.stringify(dataset))
            .map(response => response);
    }

    download(dataset: Dataset): void {
        if (!dataset.id) throw Error('Cannot download a dataset without an id');
        this.http.get(
            AppUtils.BACKEND_API_DATASET_URL + '/download/' + dataset.id, 
            { observe: 'response', responseType: 'blob' }
        ).subscribe(
            response => {
                let blob: Blob = new Blob([response], { type: 'application/zip' });
                AppUtils.downloadFile(blob, this.getFilename(response));
            }
        );
    }

    private getFilename(response: HttpResponse<Object>): string {
        const prefix = 'attachment;filename=';
        let contentDispHeader: string = response.headers.get('Content-Disposition');
        return contentDispHeader.slice(contentDispHeader.indexOf(prefix) + prefix.length, contentDispHeader.length);
    }
}