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
import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, firstValueFrom } from 'rxjs';

import { TaskState } from 'src/app/async-tasks/task.model';
import { SingleDownloadService } from 'src/app/shared/mass-download/single-download.service';

import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { Page, Pageable } from '../../shared/components/table/pageable.model';
import * as AppUtils from '../../utils/app.utils';

import {
    DatasetAcquisitionDatasetsDTO,
    DatasetAcquisitionDTO,
    DatasetAcquisitionDTOService,
    ExaminationDatasetAcquisitionDTO,
} from './dataset-acquisition.dto';
import { DatasetAcquisition } from './dataset-acquisition.model';
import { DatasetAcquisitionUtils } from './dataset-acquisition.utils';


@Injectable()
export class DatasetAcquisitionService extends EntityService<DatasetAcquisition> {

    protected dsAcqDtoService: DatasetAcquisitionDTOService = inject(DatasetAcquisitionDTOService);

    protected bcService: BreadcrumbsService = inject(BreadcrumbsService);

    API_URL = AppUtils.BACKEND_API_DATASET_ACQUISITION_URL;
    
    constructor(protected http: HttpClient, private downloadService: SingleDownloadService) {
        super(http)
    }

    getEntityInstance(entity): DatasetAcquisition {
        return DatasetAcquisitionUtils.getNewDAInstance(entity.type);
    }

    protected mapEntity = (entity: any): Promise<DatasetAcquisition> => {
        const result: DatasetAcquisition = this.getEntityInstance(entity);
        this.dsAcqDtoService.toDatasetAcquisition(entity, result);
        return Promise.resolve(result);
    }

    protected mapEntityList = (dtos: DatasetAcquisitionDTO[], result?: DatasetAcquisition[]): Promise<DatasetAcquisition[]> => {
        if (result == undefined) result = [];
        return this.dsAcqDtoService.toDatasetAcquisitions(dtos, result);
    }

    getPage(pageable: Pageable): Promise<Page<DatasetAcquisition>> {
        return firstValueFrom(this.http.get<Page<DatasetAcquisitionDTO>>(AppUtils.BACKEND_API_DATASET_ACQUISITION_URL, { 'params': pageable.toParams() }))
            .then((page: Page<DatasetAcquisitionDTO>) => {
                if (!page) return null;
                const immediateResult: DatasetAcquisition[] = [];
                this.dsAcqDtoService.toDatasetAcquisitions(page.content, immediateResult);
                return Page.transType<DatasetAcquisition>(page, immediateResult);
            });
    }

    getAllForExamination(examinationId: number): Promise<ExaminationDatasetAcquisitionDTO[]> { // TODO : services shouldn't return dtos
        return firstValueFrom(this.http.get<ExaminationDatasetAcquisitionDTO[]>(AppUtils.BACKEND_API_DATASET_ACQUISITION_URL + '/examination/' + examinationId));
    }

    getAllForDatasets(datasetIds: number[]): Promise<DatasetAcquisition[]> {
        return firstValueFrom(this.http.post<DatasetAcquisitionDatasetsDTO[]>(AppUtils.BACKEND_API_DATASET_ACQUISITION_URL + '/byDatasetIds', Array.from(datasetIds)))
            .then(dtos => this.mapEntityList(dtos));
    }

    getByStudycardId(studycardId: number): Promise<DatasetAcquisition[]> {
        return firstValueFrom(this.http.get<DatasetAcquisitionDatasetsDTO[]>(AppUtils.BACKEND_API_DATASET_ACQUISITION_URL + '/byStudyCard/' + studycardId))
            .then(dtos => this.mapEntityList(dtos));
    }

    public stringify(entity: DatasetAcquisition) {
        const dto = new DatasetAcquisitionDTO(entity);
        return JSON.stringify(dto, this.customReplacer);
    }

    postFile(fileToUpload: File, acquisitionId: number): Promise<any> {
        const endpoint = this.API_URL + '/extra-data-upload/' + acquisitionId;
        const formData: FormData = new FormData();
        formData.append('file', fileToUpload, fileToUpload.name);
        return firstValueFrom(this.http.post<any>(endpoint, formData));
    }

    downloadFile(fileName: string, acquisitionId: number, state?: TaskState): Observable<TaskState> {
        const endpoint: string = this.API_URL + '/extra-data-download/' + acquisitionId + "/" + fileName + "/";
        return this.downloadService.downloadSingleFile(endpoint, null, state);
    }
}