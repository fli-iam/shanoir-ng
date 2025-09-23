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

import { DatasetProcessingType } from '../../enum/dataset-processing-type.enum';
import { MrDataset } from '../dataset/mr/dataset.mr.model';
import { DatasetProcessing } from './dataset-processing.model';
import { DatasetProcessingService } from './dataset-processing.service';

@Injectable()
export class DatasetProcessingDTOService {

    private datasetProcessingService: DatasetProcessingService;

    constructor() {}

    setDatasetProcessingService(datasetProcessingService: DatasetProcessingService) {
        this.datasetProcessingService = datasetProcessingService;
    }

    /**
     * Convert from DTO to Entity
     * Warning : DO NOT USE THIS IN A LOOP, use toEntityList instead
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntity(dto: DatasetProcessingInDTO, result?: DatasetProcessing): Promise<DatasetProcessing> {
        if (!result) result = new DatasetProcessing();
        DatasetProcessingDTOService.mapSyncFields(dto, result);
        let promises: Promise<any>[] = [];
        return Promise.all(promises).then(([]) => {
            return result;
        });
    }

    /**
     * Convert from a DTO list to an Entity list
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntityList(dtos: DatasetProcessingInDTO[], result?: DatasetProcessing[]): Promise<DatasetProcessing[]>{
        if (!result) result = [];
        let promises: Promise<any>[] = [];
        if (dtos) {
            for (let dto of dtos ? dtos : []) {
                let entity = new DatasetProcessing();
                DatasetProcessingDTOService.mapSyncFields(dto, entity);
                result.push(entity);
            }
        }
        return Promise.all(promises).then(() => {
            return result;
        })
    }

    static mapSyncFields(dto: DatasetProcessingInDTO, entity: DatasetProcessing): DatasetProcessing {
        entity.id = dto.id;
        entity.comment = dto.comment;
        entity.datasetProcessingType = dto.datasetProcessingType;
        if(dto.inputDatasets) {
            entity.inputDatasets = dto.inputDatasets.map(id => { 
                let dataset = new MrDataset();
                dataset.id = id;
                return dataset;
            })
        }
        if(dto.outputDatasets) {
            entity.outputDatasets = dto.outputDatasets.map(id => {
                let dataset = new MrDataset();
                dataset.id = id;
                return dataset;
            })
        }
        entity.processingDate = new Date(dto.processingDate);
        entity.studyId = dto.studyId;
        entity.parentId = dto.parentId;
        return entity;
    }

}

export class DatasetProcessingInDTO {

    id: number;
    comment: string;
    datasetProcessingType: DatasetProcessingType;
    inputDatasets: number[];
    outputDatasets: number[];
	processingDate: Date;
    studyId: number;
    parentId: number;
}

export class DatasetProcessingOutDTO {

    id: number;
    comment: string;
    datasetProcessingType: DatasetProcessingType;
    inputDatasets: {id: number, studyId: number}[];
    outputDatasets: {id: number, studyId: number}[];
	processingDate: Date;
    studyId: number;
    parentId: number;

    constructor(datasetProcessing: DatasetProcessing) {
        this.id = datasetProcessing.id;
        this.comment = datasetProcessing.comment;
        this.datasetProcessingType = datasetProcessing.datasetProcessingType;
        this.inputDatasets = datasetProcessing.inputDatasets.map(ds => {return {id: ds.id, studyId: ds.study?.id}});
        this.outputDatasets = datasetProcessing.outputDatasets.map(ds => {return {id: ds.id, studyId: ds.study?.id}});
        this.processingDate = datasetProcessing.processingDate;
        this.studyId = datasetProcessing.studyId;
        this.parentId = datasetProcessing.parentId;
    }
}
