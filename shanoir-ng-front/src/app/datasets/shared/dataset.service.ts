import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { Page, Pageable } from '../../shared/components/table/pageable.model';
import * as AppUtils from '../../utils/app.utils';
import { Dataset } from './dataset.model';

@Injectable()
export class DatasetService {

    constructor(private http: HttpClient) { }

    create(dataset: Dataset): Promise<Dataset> {
        return this.http.post<Dataset>(AppUtils.BACKEND_API_DATASET_URL, JSON.stringify(dataset))
            .toPromise();
    }

    delete(id: number): Promise<void> {
        return this.http.delete<void>(AppUtils.BACKEND_API_DATASET_URL + '/' + id)
            .toPromise();
    }

    get(id: number): Promise<Dataset> {
        return this.http.get<Dataset>(AppUtils.BACKEND_API_DATASET_URL + '/' + id)
            .map(AppUtils.mapType)
            .toPromise();
    }

    getPage(pageable: Pageable): Promise<any> {
        return this.http.get<any>(AppUtils.BACKEND_API_DATASET_URL, { 'params': pageable.toParams() })
            .map((page: Page<Dataset>) => {
                page.content = page.content.map(AppUtils.mapType);
                return page;
            })
            .toPromise();
    }

    update(dataset: Dataset): Promise<void> {
        if (!dataset.id) throw Error('Cannot update a dataset without an id');
        return this.http.put<void>(AppUtils.BACKEND_API_DATASET_URL + '/' + dataset.id, JSON.stringify(dataset))
            .toPromise();
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