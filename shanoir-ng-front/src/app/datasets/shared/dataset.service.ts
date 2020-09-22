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

import { HttpResponse, HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { Page, Pageable } from '../../shared/components/table/pageable.model';
import * as AppUtils from '../../utils/app.utils';
import { ServiceLocator } from '../../utils/locator.service';
import { DatasetDTO, DatasetDTOService } from './dataset.dto';
import { Dataset } from './dataset.model';
import 'rxjs/add/operator/map'


@Injectable()
export class DatasetService extends EntityService<Dataset> {

    API_URL = AppUtils.BACKEND_API_DATASET_URL;
    
    httpOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };

    constructor(protected http: HttpClient) {
        super(http)
    }

    private datasetDTOService: DatasetDTOService = ServiceLocator.injector.get(DatasetDTOService);

    getEntityInstance(entity: Dataset) { 
        return AppUtils.getDatasetInstance(entity.type);
    }

    getPage(pageable: Pageable): Promise<Page<Dataset>> {
        return this.http.get<Page<Dataset>>(AppUtils.BACKEND_API_DATASET_URL, { 'params': pageable.toParams() })
            .map((page: Page<Dataset>) => {
                if (page && page.content) {
                    page.content = page.content.map(ds => Object.assign(ds, this.getEntityInstance(ds)));
                }
                return page;
            })
            .toPromise()
            .then(this.mapPage);
    }

    public downloadDatasets(ids: number[], format: string) {
        let params = new HttpParams().set("datasetIds", ids.join(',')).set("format", format);
        return this.http.get(
            AppUtils.BACKEND_API_DATASET_URL + '/massiveDownload',
            { observe: 'response', responseType: 'blob', params: params})
            .toPromise().then(
            response => {
                this.downloadIntoBrowser(response);
            }
        )
    }

    public downloadDatasetsByStudy(studyId: number, format: string) {
        let params = new HttpParams().set("studyId", '' + studyId).set("format", format);
        return this.http.get(
            AppUtils.BACKEND_API_DATASET_URL + '/massiveDownloadByStudy',
            { observe: 'response', responseType: 'blob', params: params})
            .toPromise().then(
            response => {
                this.downloadIntoBrowser(response);
            }
        )
    }

    download(dataset: Dataset, format: string): Promise<void> {
        if (!dataset.id) throw Error('Cannot download a dataset without an id');
        return this.downloadToBlob(dataset.id, format).toPromise().then(
            response => {
                this.downloadIntoBrowser(response);
            }
        );
    }

    downloadStatistics(studyNameRegExp: string) {
        let params = new HttpParams().set("studyNameRegExp", '' + studyNameRegExp);
        return this.http.get(
            AppUtils.BACKEND_API_DATASET_URL + '/downloadStatistics', { observe: 'response', responseType: 'blob', params: params})
            .toPromise().then(
            response => {
                this.downloadIntoBrowser(response);
            }
        )
    }

    downloadToBlob(id: number, format: string): Observable<HttpResponse<Blob>> {
        if (!id) throw Error('Cannot download a dataset without an id');
        return this.http.get(
            AppUtils.BACKEND_API_DATASET_URL + '/download/' + id + '?format=' + format, 
            { observe: 'response', responseType: 'blob' }
        ).map(response => response);
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
        let result: Dataset = AppUtils.getDatasetInstance(dto.type);
        this.datasetDTOService.toEntity(dto, result);
        return Promise.resolve(result);
    }

    protected mapEntityList = (dtos: DatasetDTO[]): Promise<Dataset[]> => {
        let result: Dataset[] = [];
        if (dtos) this.datasetDTOService.toEntityList(dtos, result);
        return Promise.resolve(result);
    }
}