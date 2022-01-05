/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */
import { HttpClient, HttpEvent, HttpEventType, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { ErrorHandler, Injectable, OnDestroy } from '@angular/core';
import { saveAs } from 'file-saver';
import { Subscription } from 'rxjs';
import { Observable } from 'rxjs/Observable';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { LoadingBarComponent } from '../../shared/components/loading-bar/loading-bar.component';
import { Page, Pageable } from '../../shared/components/table/pageable.model';
import * as AppUtils from '../../utils/app.utils';
import { ServiceLocator } from '../../utils/locator.service';
import { DatasetDTO, DatasetDTOService } from './dataset.dto';
import { Dataset } from './dataset.model';
import { DatasetUtils } from './dataset.utils';

@Injectable()
export class DatasetService extends EntityService<Dataset> implements OnDestroy {

    API_URL = AppUtils.BACKEND_API_DATASET_URL;
    subscribtions: Subscription[] = [];
    
    httpOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };
    constructor(protected http: HttpClient) {
        super(http)
    }

    private datasetDTOService: DatasetDTOService = ServiceLocator.injector.get(DatasetDTOService);

    private errorService: ErrorHandler  = ServiceLocator.injector.get(ErrorHandler);

    deleteAll(ids: number[]) {
        return this.http.request<void>('delete', this.API_URL + '/delete', { body: JSON.stringify(ids) })
                .toPromise();
    }

    getEntityInstance(entity: Dataset) { 
        return DatasetUtils.getDatasetInstance(entity.type);
    }

    getPage(pageable: Pageable): Promise<Page<Dataset>> {
        return this.http.get<Page<Dataset>>(AppUtils.BACKEND_API_DATASET_URL, { 'params': pageable.toParams() })
            .toPromise()
            .then((page: Page<Dataset>) => {
                if (page && page.content) {
                    page.content = page.content.map(ds => Object.assign(ds, this.getEntityInstance(ds)));
                }
                return page;
            })
            .then(this.mapPage); 
    }

    getByAcquisitionId(acquisitionId: number): Promise<Dataset[]> {
        return this.http.get<DatasetDTO[]>(AppUtils.BACKEND_API_DATASET_URL + '/acquisition/' + acquisitionId)
                .toPromise()
                .then(dtos => this.datasetDTOService.toEntityList(dtos));
    }

    getByStudyId(studyId: number): Promise<Dataset[]> {
        return this.http.get<DatasetDTO[]>(AppUtils.BACKEND_API_DATASET_URL + '/study/' + studyId)
                .toPromise()
                .then(dtos => this.datasetDTOService.toEntityList(dtos));
    }

    getByStudyIdAndSubjectId(studyId: number, subjectId: number): Promise<Dataset[]> {
		if (!subjectId) {
			return this.getByStudyId(studyId);
		}
        return this.http.get<DatasetDTO[]>(AppUtils.BACKEND_API_DATASET_URL + '/find/subject/' + subjectId + '/study/' + studyId)
                .toPromise()
                .then(dtos => this.datasetDTOService.toEntityList(dtos));
    }
    
    progressBarFunc(event: HttpEvent<any>, progressBar: LoadingBarComponent, callback?): void {
       switch (event.type) {
            case HttpEventType.Sent:
              progressBar.progress = -1;
              break;
            case HttpEventType.DownloadProgress:
              progressBar.progress = (event.loaded / event.total);
              break;
            case HttpEventType.Response:
                progressBar.progress = 0;
                saveAs(event.body, this.getFilename(event));
                if (callback) {
                    callback();
                }
        }
    }

    public downloadDatasets(ids: number[], format: string, progressBar: LoadingBarComponent, callback?) {
        const formData: FormData = new FormData();
        formData.set('datasetIds', ids.join(","));
        formData.set("format", format);
        this.subscribtions.push(
           this.http.post(
           AppUtils.BACKEND_API_DATASET_URL + '/massiveDownload', formData, {
                reportProgress: true,
                observe: 'events',
                responseType: 'blob'
           }).subscribe((event: HttpEvent<any>) => this.progressBarFunc(event, progressBar, callback),
            error =>  {
                this.errorService. handleError(error);
                progressBar.progress = 0;
                if (callback) {
                    callback();
                }
            })
         );
    }

    public downloadDatasetsByStudy(studyId: number, format: string, progressBar: LoadingBarComponent) {
        let params = new HttpParams().set("studyId", '' + studyId).set("format", format);
        this.subscribtions.push(
           this.http.get(
           AppUtils.BACKEND_API_DATASET_URL + '/massiveDownloadByStudy',{
                reportProgress: true,
                observe: 'events',
                responseType: 'blob',
                params: params
            }).subscribe((event: HttpEvent<any>) => this.progressBarFunc(event, progressBar),
             error =>  {
                this.errorService. handleError(error);
                progressBar.progress = 0;
            })
         );
    }

    downloadStatistics(studyNameInRegExp: string, studyNameOutRegExp: string, subjectNameInRegExp: string, subjectNameOutRegExp: string) {
        let params = new HttpParams().set("studyNameInRegExp", studyNameInRegExp)
                                        .set("studyNameOutRegExp", studyNameOutRegExp)
                                        .set("subjectNameInRegExp", subjectNameInRegExp)
                                        .set("subjectNameOutRegExp", subjectNameOutRegExp);
        return this.http.get(
            AppUtils.BACKEND_API_DATASET_URL + '/downloadStatistics', { observe: 'response', responseType: 'blob', params: params})
            .toPromise().then(
            response => {
                this.downloadIntoBrowser(response);
            }
        )
    }

    download(dataset: Dataset, format: string, converterId: number = null): Promise<void> {
        if (!dataset.id) throw Error('Cannot download a dataset without an id');
        return this.downloadFromId(dataset.id, format, converterId);
    }

    downloadFromId(datasetId: number, format: string, converterId: number = null): Promise<void> {
        if (!datasetId) throw Error('Cannot download a dataset without an id');
        return this.downloadToBlob(datasetId, format, converterId).then(
            response => {
                this.downloadIntoBrowser(response);
            }
        ).catch(error => {
            this.errorService. handleError(error);
        });
    }

    downloadToBlob(id: number, format: string, converterId: number = null): Promise<HttpResponse<Blob>> {
        if (!id) throw Error('Cannot download a dataset without an id');
        return this.http.get(
            AppUtils.BACKEND_API_DATASET_URL + '/download/' + id + '?format=' + format + (converterId ? ('&converterId=' + converterId) : ''),
            { observe: 'response', responseType: 'blob' }
        ).toPromise();
    }

    exportBIDSBySubjectId(subjectId: number, subjectName: string, studyName: string): void {
        if (!subjectId) throw Error('subject id is required');
        this.http.get(AppUtils.BACKEND_API_DATASET_URL + '/exportBIDS/subjectId/' + subjectId 
            + '/subjectName/' + subjectName + '/studyName/' + studyName, 
            { observe: 'response', responseType: 'blob' }
        ).subscribe(response => {this.downloadIntoBrowser(response);});
    }

    getUrls(id: number): Observable<HttpResponse<any>> {
        if (!id) throw Error('Cannot get the urls of a dataset without an id');
        return this.http.get<any>(AppUtils.BACKEND_API_DATASET_URL + '/urls/' + id);
    }

    prepareUrl(id: number, url: string, format: string): Observable<any> {
        if (!id) throw Error('Cannot get the urls of a dataset without an id');
        // return this.http.get<any>(AppUtils.BACKEND_API_DATASET_URL + '/urls/' + id + '/url/?url=' + url + '&format=' + format);

        let httpOptions: any = Object.assign( { responseType: 'text' }, this.httpOptions);
        
        return this.http.post<string>(`${AppUtils.BACKEND_API_DATASET_URL}/prepare-url/${encodeURIComponent(id)}?format=${encodeURIComponent(format)}`, { url: url }, httpOptions);
    }

    private getFilename(response: HttpResponse<any>): string {
        const prefix = 'attachment;filename=';
        let contentDispHeader: string = response.headers.get('Content-Disposition');
        return contentDispHeader.slice(contentDispHeader.indexOf(prefix) + prefix.length, contentDispHeader.length);
    }

    private downloadIntoBrowser(response: HttpResponse<Blob>){
        AppUtils.browserDownloadFile(response.body, this.getFilename(response));
    }

    protected mapEntity = (dto: DatasetDTO): Promise<Dataset> => {
        let result: Dataset = DatasetUtils.getDatasetInstance(dto.type);
        this.datasetDTOService.toEntity(dto, result);
        return Promise.resolve(result);
    }

    protected mapEntityList = (dtos: DatasetDTO[]): Promise<Dataset[]> => {
        let result: Dataset[] = [];
        if (dtos) this.datasetDTOService.toEntityList(dtos, result);
        return Promise.resolve(result);
    }
    
    public stringify(entity: Dataset) {
        let dto = new DatasetDTO(entity);
        return JSON.stringify(dto, (key, value) => {
            return this.customReplacer(key, value, dto);
        });
    }

    ngOnDestroy() {
        for(let subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
    }
}