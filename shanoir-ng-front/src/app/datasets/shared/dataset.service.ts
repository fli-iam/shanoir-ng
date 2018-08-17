import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import { ErrorObservable } from 'rxjs/observable/ErrorObservable';

import { Dataset, DatasetMetadata } from './dataset.model';
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
            .toPromise();
    }

    get(id: number): Promise<Dataset> {
        return this.http.get<Dataset>(AppUtils.BACKEND_API_DATASET_URL + '/' + id)
            .toPromise();
    }

    getAll(): Promise<Dataset[]> {
        return this.http.get<Dataset[]>(AppUtils.BACKEND_API_DATASET_URL)
            .toPromise();
    }

    update(dataset: Dataset): Observable<Dataset> {
        if (!dataset.id) throw Error('Cannot update a dataset without an id');
        return this.http.put<Dataset>(AppUtils.BACKEND_API_DATASET_URL + '/' + dataset.id, JSON.stringify(dataset));
    }

    download(dataset: Dataset, format: string): void {
        if (!dataset.id) throw Error('Cannot download a dataset without an id');
        this.downloadToBlob(dataset.id, format).subscribe(
            response => {
                this.downloadIntoBrowser(response);
            }
        );
    }

    downloadToBlob(id: number, format: string): Observable<HttpResponse<Blob>> {
        if (!id) throw Error('Cannot download a dataset without an id');
        return this.http.get(
            AppUtils.BACKEND_API_DATASET_URL + '/download/' + id + '?format=' + format, 
            { observe: 'response', responseType: 'blob' }
        ).map(response => response);
    }

    private getFilename(response: HttpResponse<any>): string {
        const prefix = 'attachment;filename=';
        let contentDispHeader: string = response.headers.get('Content-Disposition');
        return contentDispHeader.slice(contentDispHeader.indexOf(prefix) + prefix.length, contentDispHeader.length);
    }

    private downloadIntoBrowser(response: HttpResponse<Blob>){
        AppUtils.browserDownloadFile(response.body, this.getFilename(response));
    }
}