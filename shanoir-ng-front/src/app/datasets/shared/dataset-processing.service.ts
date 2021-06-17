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
import { Injectable } from '@angular/core';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import * as AppUtils from '../../utils/app.utils';
import { DatasetProcessing } from './dataset-processing.model';
import { HttpClient } from '@angular/common/http';
import { DatasetProcessingDTO, DatasetProcessingDTOService } from './dataset-processing.dto';
import { DatasetDTO, DatasetDTOService } from './dataset.dto';
import { Dataset } from './dataset.model';

@Injectable()
export class DatasetProcessingService extends EntityService<DatasetProcessing> {

    API_URL = AppUtils.BACKEND_API_DATASET_PROCESSING_URL;

    constructor(protected http: HttpClient, 
        private datasetDTOService: DatasetDTOService,
        private datasetProcessingDTOService: DatasetProcessingDTOService) {
        super(http)
        datasetProcessingDTOService.setDatasetProcessingService(this);
    }

    getInputDatasets(datasetProcessingId: number): Promise<Dataset[]> {
        return this.http.get<DatasetDTO[]>(this.API_URL + '/' + datasetProcessingId + '/inputDatasets/')
            .toPromise().then(dtos => this.datasetDTOService.toEntityList(dtos));
    }

    getOutputDatasets(datasetProcessingId: number): Promise<Dataset[]> {
        return this.http.get<DatasetDTO[]>(this.API_URL + '/' + datasetProcessingId + '/outputDatasets/')
            .toPromise().then(dtos => this.datasetDTOService.toEntityList(dtos));
    }

    getEntityInstance() { return new DatasetProcessing(); }

    protected mapEntity = (dto: DatasetProcessingDTO): Promise<DatasetProcessing> => {
        let result: DatasetProcessing = new DatasetProcessing();
        this.datasetProcessingDTOService.toEntity(dto, result);
        return Promise.resolve(result);
    }

    protected mapEntityList = (dtos: DatasetProcessingDTO[]): Promise<DatasetProcessing[]> => {
        let result: DatasetProcessing[] = [];
        if (dtos) this.datasetProcessingDTOService.toEntityList(dtos, result);
        return Promise.resolve(result);
    }
    
    public stringify(entity: DatasetProcessing) {
        let dto = new DatasetProcessingDTO(entity);
        return JSON.stringify(dto, (key, value) => {
            return this.customReplacer(key, value, dto);
        });
    }
}