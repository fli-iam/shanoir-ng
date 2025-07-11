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
import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { ErrorHandler, Injectable } from '@angular/core';

import { TaskState } from 'src/app/async-tasks/task.model';
import { BidsElement } from "../../bids/model/bidsElement.model";
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { Page, Pageable } from '../../shared/components/table/pageable.model';
import * as AppUtils from '../../utils/app.utils';
import { ServiceLocator } from '../../utils/locator.service';
import { MrDataset } from '../dataset/mr/dataset.mr.model';
import { DatasetDTO, DatasetDTOService, MrDatasetDTO } from "./dataset.dto";
import { Dataset } from './dataset.model';
import { DatasetUtils } from './dataset.utils';
import { Observable } from 'rxjs';
import { DatasetType } from './dataset-type.model';

export type Format = 'nii' | 'dcm';
export type DatasetLight = {id: number, name: string, type: DatasetType, hasProcessings: boolean, studyId: number};

@Injectable()
export class DatasetService extends EntityService<Dataset> {

    readonly API_URL = AppUtils.BACKEND_API_DATASET_URL;
    readonly MAX_DATASETS_IN_ZIP_DL: number = 500;

    httpOptions = {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    };
    constructor(protected http: HttpClient) {
        super(http);
    }

    private datasetDTOService: DatasetDTOService = ServiceLocator.injector.get(DatasetDTOService);

    private errorService: ErrorHandler  = ServiceLocator.injector.get(ErrorHandler);

    deleteAll(ids: number[]) {
        return this.http.request<void>('delete', this.API_URL + '/delete', { body: JSON.stringify(ids) })
                .toPromise();
    }

    getBidsStructure(studyId: number): Promise<BidsElement> {
        if (!studyId) throw Error('study id is required');
        return this.http.get<BidsElement>(AppUtils.BACKEND_API_BIDS_STRUCTURE_URL + '/studyId/' + studyId)
            .toPromise();
    }

    refreshBidsStructure(studyId: number, studyName: string): Promise<BidsElement> {
        if (!studyId) throw Error('study id is required');
        return this.http.get<BidsElement>(AppUtils.BACKEND_API_BIDS_REFRESH_URL + '/studyId/' + studyId + '/studyName/' + studyName)
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

    getByExaminationId(examinationId: number) : Promise<Dataset[]> {
        return this.http.get<DatasetDTO[]>(AppUtils.BACKEND_API_DATASET_URL + '/examination/' + examinationId)
                .toPromise()
                .then(dtos => this.datasetDTOService.toEntityList(dtos, null, 'lazy'));
    }

    getByAcquisitionId(acquisitionId: number): Promise<Dataset[]> {
        return this.http.get<DatasetDTO[]>(AppUtils.BACKEND_API_DATASET_URL + '/acquisition/' + acquisitionId)
                .toPromise()
                .then(dtos => this.datasetDTOService.toEntityList(dtos));
    }

    getByStudyId(studyId: number): Promise<Dataset[]> {
        return this.http.get<DatasetDTO[]>(AppUtils.BACKEND_API_DATASET_URL + '/study/' + studyId)
                .toPromise()
                .then(dtos => this.datasetDTOService.toEntityList(dtos, [], 'lazy'));
    }

    getByStudyIdAndSubjectId(studyId: number, subjectId: number): Promise<Dataset[]> {
		if (!subjectId) {
			return this.getByStudyId(studyId);
		}
        return this.http.get<DatasetDTO[]>(AppUtils.BACKEND_API_DATASET_URL + '/find/subject/' + subjectId + '/study/' + studyId)
                .toPromise()
                .then(dtos => this.datasetDTOService.toEntityList(dtos));
    }

    getByIds(ids: Set<number>): Promise<DatasetLight[]> {
        const formData: FormData = new FormData();
        formData.set('datasetIds', Array.from(ids).join(","));
        return this.http.post<DatasetLight[]>(AppUtils.BACKEND_API_DATASET_URL + '/allById', formData)
            .toPromise();
    }

    countDatasetsByStudyId(studyId: number): Promise<number> {
        return this.http.get<number>(AppUtils.BACKEND_API_DATASET_URL + '/study/nb-datasets/' + studyId)
        .toPromise();
    }

    public downloadDatasets(ids: number[], format: string, converter ? : number, state?: TaskState): Observable<TaskState> {
        const formData: FormData = new FormData();
        formData.set('datasetIds', ids.join(","));
        formData.set("format", format);
        if (converter) {
            formData.set("converterId", "" + converter);
        }
        const url: string = AppUtils.BACKEND_API_DATASET_URL + '/massiveDownload';
        return AppUtils.downloadWithStatusPOST(url, formData, state);
    }

    downloadStatistics(studyNameInRegExp: string, studyNameOutRegExp: string, subjectNameInRegExp: string, subjectNameOutRegExp: string) {
        let params = new HttpParams()
            .set("studyNameInRegExp", studyNameInRegExp)
            .set("studyNameOutRegExp", studyNameOutRegExp)
            .set("subjectNameInRegExp", subjectNameInRegExp)
            .set("subjectNameOutRegExp", subjectNameOutRegExp);
        return this.http.get(
            AppUtils.BACKEND_API_DATASET_URL + '/downloadStatistics', { observe: 'response', responseType: 'blob', params: params})
            .toPromise().then(
            response => {
                if (response.status != 204) {
                    this.consoleService.log('error', 'Error during creation of statistics.');
                } else {
                    this.consoleService.log('info', 'Statistics are being prepared, check the Jobs page to see its progress.');
                }
            }
        )
    }

    downloadDicomMetadata(datasetId: number): Promise<any> {
        return this.http.get(
            AppUtils.BACKEND_API_DATASET_URL + '/dicom-metadata/' + datasetId,
            { responseType: 'json' }
        ).toPromise();
    }

    downloadToBlob(id: number, format: string, converterId: number = null): Promise<HttpResponse<Blob>> {
        if (!id) throw Error('Cannot download a dataset without an id');
        return this.http.get(
            AppUtils.BACKEND_API_DATASET_URL + '/download/' + id + '?format=' + format + (converterId ? ('&converterId=' + converterId) : ''),
            { observe: 'response', responseType: 'blob' }
        ).toPromise();
    }

    private downloadIntoBrowser(response: HttpResponse<Blob>){
        AppUtils.browserDownloadFileFromResponse(response);
    }

    protected mapEntity = (dto: DatasetDTO, quickResult?: Dataset, mode: 'eager' | 'lazy' = 'eager'): Promise<Dataset> => {
        quickResult = DatasetUtils.getDatasetInstance(dto.type);
        return this.datasetDTOService.toEntity(dto, quickResult, mode);
    }

    protected mapEntityList = (dtos: DatasetDTO[]): Promise<Dataset[]> => {
        let result: Dataset[] = [];
        if (dtos) this.datasetDTOService.toEntityList(dtos, result);
        return Promise.resolve(result);
    }

    public stringify(entity: Dataset) {
        let dto;
        if (entity instanceof MrDataset) {
            dto = new MrDatasetDTO(entity);
            dto.updatedMrMetadata = entity.updatedMrMetadata;
        } else {
            dto = new DatasetDTO(entity);
        }
        return JSON.stringify(dto, (key, value) => {
            return this.customReplacer(key, value, dto);
        });
    }
}
