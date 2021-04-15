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

import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectService } from '../../subjects/shared/subject.service';
import { DatasetType } from './dataset-type.model';
import { Dataset, DatasetMetadata } from './dataset.model';
import { DatasetUtils } from './dataset.utils';
import { MrDataset, EchoTime, FlipAngle, InversionTime, MrDatasetMetadata, RepetitionTime, MrQualityProcedureType, MrDatasetNature } from '../dataset/mr/dataset.mr.model';
import { DiffusionGradient } from '../../dataset-acquisitions/modality/mr/mr-protocol.model';
import { Channel, Event, EegDataset } from '../dataset/eeg/dataset.eeg.model';
import { DatasetProcessing } from './dataset-processing.model';
import { DatasetProcessingType } from 'src/app/enum/dataset-processing-type.enum';
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
    public toEntity(dto: DatasetProcessingDTO, result?: DatasetProcessing): Promise<DatasetProcessing> {
        if (!result) result = new DatasetProcessing();
        DatasetProcessingDTOService.mapSyncFields(dto, result);
        let promises: Promise<any>[] = [];
        if (dto.inputDatasetIds && dto.inputDatasetIds.length > 0) {
            promises.push(this.datasetProcessingService.getInputDatasets(dto.id).then(inputDatasets => result.inputDatasets = inputDatasets));
        }
        if (dto.outputDatasetIds && dto.outputDatasetIds.length > 0) {
            promises.push(this.datasetProcessingService.getOutputDatasets(dto.id).then(outputDatasets => result.outputDatasets = outputDatasets));
        }
        return Promise.all(promises).then(([]) => {
            return result;
        });
    }

    /**
     * Convert from a DTO list to an Entity list
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntityList(dtos: DatasetProcessingDTO[], result?: DatasetProcessing[]): Promise<DatasetProcessing[]>{
        if (!result) result = [];
        let promises: Promise<any>[] = [];
        if (dtos) {
            for (let dto of dtos) {
                let entity = new DatasetProcessing();
                DatasetProcessingDTOService.mapSyncFields(dto, entity);
                result.push(entity);
            }
        }
        return Promise.all(promises).then(() => {
            return result;
        })
    }

    static mapSyncFields(dto: DatasetProcessingDTO, entity: DatasetProcessing): DatasetProcessing {
        entity.id = dto.id;
        entity.comment = dto.comment;
        entity.datasetProcessingType = dto.datasetProcessingType;
        if(dto.inputDatasetIds) {
            entity.inputDatasets = dto.inputDatasetIds.map((datasetId)=> {
                let dataset = new MrDataset();
                dataset.id = datasetId;
                return dataset;
            })
        }
        if(dto.outputDatasetIds) {
            entity.outputDatasets = dto.outputDatasetIds.map((datasetId)=> {
                let dataset = new MrDataset();
                dataset.id = datasetId;
                return dataset;
            })
        }
        entity.processingDate = dto.processingDate;
        entity.studyId = dto.studyId;
        return entity;
    }

}

export class DatasetProcessingDTO {

    id: number;
    comment: string;
    datasetProcessingType: DatasetProcessingType;
    inputDatasetIds: number[];
    outputDatasetIds: number[];
	processingDate: Date;
    studyId: number;

    constructor(datasetProcessing: DatasetProcessing) {
        this.id = datasetProcessing.id;
        this.comment = datasetProcessing.comment;
        this.datasetProcessingType = datasetProcessing.datasetProcessingType;
        this.inputDatasetIds = datasetProcessing.inputDatasets.map((dataset)=> dataset.id);
        this.outputDatasetIds = datasetProcessing.outputDatasets.map((dataset)=> dataset.id);
        this.processingDate = datasetProcessing.processingDate;
        this.studyId = datasetProcessing.studyId;
    }
}