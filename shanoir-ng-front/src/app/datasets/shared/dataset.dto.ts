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
import { Injectable, Injector } from '@angular/core';

import { DiffusionGradient } from '../../dataset-acquisitions/modality/mr/mr-protocol.model';
import { DatasetAcquisitionDTO, DatasetAcquisitionDTOService } from '../../dataset-acquisitions/shared/dataset-acquisition.dto';
import { DatasetAcquisitionUtils } from '../../dataset-acquisitions/shared/dataset-acquisition.utils';
import { DatasetProcessingService } from '../../datasets/shared/dataset-processing.service';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectService } from '../../subjects/shared/subject.service';
import { Tag } from "../../tags/tag.model";
import { Channel, EegDataset, Event } from '../dataset/eeg/dataset.eeg.model';
import { EchoTime, FlipAngle, InversionTime, MrDataset, MrDatasetNature, MrQualityProcedureType, RepetitionTime } from '../dataset/mr/dataset.mr.model';
import { DatasetProcessing } from './dataset-processing.model';
import { DatasetType } from './dataset-type.model';
import { Dataset, DatasetMetadata } from './dataset.model';
import { DatasetUtils } from './dataset.utils';
import { DatasetProcessingDTO, DatasetProcessingDTOService } from './dataset-processing.dto';

@Injectable()
export class DatasetDTOService {
    constructor(
        private studyService: StudyService,
        private subjectService: SubjectService,
        private injector: Injector
    ) {}

    /**
     * Convert from DTO to Entity
     * Warning : DO NOT USE THIS IN A LOOP, use toEntityList instead
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntity(dto: DatasetDTO, result?: Dataset, mode: 'eager' | 'lazy' = 'eager'): Promise<Dataset> {
        if (!result) result = DatasetUtils.getDatasetInstance(dto.type);
        DatasetDTOService.mapSyncFields(dto, result);
        let promises: Promise<any>[] = [];
        if (mode == 'eager') {
            if (dto.studyId) promises.push(this.studyService.get(dto.studyId).then(study => result.study = study));
            if (dto.subjectId) promises.push(this.subjectService.get(dto.subjectId).then(subject => result.subject = subject));
            return Promise.all(promises).then(([]) => {
                return result;
            });
        } else if (mode == 'lazy') {
            return Promise.resolve(result);
        }
    }

    /**
     * Convert from a DTO list to an Entity list
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntityList(dtos: DatasetDTO[], result?: Dataset[], mode: 'eager' | 'lazy' = 'eager'): Promise<Dataset[]>{
        if (!result) result = [];
        let subjectIds = new Set<number>;
        if (dtos) {
            for (let dto of dtos ? dtos : []) {
                if (dto.subjectId) {
                    subjectIds.add(dto.subjectId);
                }
                let entity = DatasetUtils.getDatasetInstance(dto.type);
                DatasetDTOService.mapSyncFields(dto, entity);
                result.push(entity);
            }
        }
        if (mode == 'eager') {
            let promises = [
                this.studyService.getStudiesNames().then(studies => {
                    for (let entity of result) {
                        if (entity.study)
                            entity.study.name = studies.find(study => study.id == entity.study.id)?.name;
                    }
                }),
                this.subjectService.getSubjectsNames(subjectIds).then(subjects => {
                    for (let entity of result) {
                        if (entity.subject)
                            entity.subject.name = subjects.find(subject => subject.id == entity.subject.id)?.name;
                    }
                })
            ];
            return Promise.all(promises).then(() => {
                return result;
            })
        } else if (mode == 'lazy') {
            return Promise.resolve(result);
        }
    }

    static mapSyncFields(dto: DatasetDTO, entity: Dataset): Dataset {
        entity.id = dto.id;
        entity.creationDate = dto.creationDate;
        entity.name = dto.name;
        entity.type = dto.type;
        entity.source = dto.source;
        entity.copies = dto.copies;
        entity.originMetadata = dto.originMetadata;
        entity.updatedMetadata = dto.updatedMetadata;
        entity.inPacs = dto.inPacs;
        if (dto.studyId) {
            entity.study = new Study();
            entity.study.id = dto.studyId;
        }
        if (dto.studyName) {
            if (!entity.study) entity.study = new Study();
            entity.study.name = dto.studyName;
        }
        if (dto.subjectId) {
            entity.subject = new Subject();
            entity.subject.id = dto.subjectId;
        }
        if (dto.subjectName) {
            if (!entity.subject) entity.subject = new Subject();
            entity.subject.name = dto.subjectName;
        }
        if (dto.datasetAcquisition) {
            let dsAcq = DatasetAcquisitionUtils.getNewDAInstance(dto.datasetAcquisition.type);
            DatasetAcquisitionDTOService.mapSyncFields(dto.datasetAcquisition, dsAcq);
            entity.datasetAcquisition = dsAcq;
        }
        if (entity.type == 'Mr') {
            this.mapSyncFieldsMr(dto as MrDatasetDTO, entity as MrDataset);
        }
        if (entity.type == 'Eeg') {
            this.mapSyncFieldsEeg(dto as EegDatasetDTO, entity as EegDataset);
        }
        if(dto.processings) {
            for(let p of dto.processings) {
                let processing = DatasetProcessingDTOService.mapSyncFields(p, new DatasetProcessing());
                entity.processings.push(processing);
            }
        }
		if (dto.datasetProcessing) {
			let process = DatasetProcessingDTOService.mapSyncFields(dto.datasetProcessing, new DatasetProcessing());
            process.id = dto.datasetProcessing.id;
			entity.datasetProcessing = process;
            entity.hasProcessing = !!entity.datasetProcessing;
		} else {
            entity.hasProcessing = dto.hasProcessing;
        }
        entity.tags = dto.tags ? dto.tags : [];
        return entity;
    }

    static mapSyncFieldsMr(dto: MrDatasetDTO, entity: MrDataset): MrDataset {
        entity.diffusionGradients = dto.diffusionGradients;
        entity.echoTime = dto.echoTime;
        entity.flipAngle = dto.flipAngle;
        entity.inversionTime = dto.inversionTime;
        entity.repetitionTime = dto.repetitionTime;
        entity.mrQualityProcedureType = dto.mrQualityProcedureType;
        entity.originMrMetadata = dto.originMrMetadata;
        entity.updatedMrMetadata = dto.updatedMrMetadata;
        entity.firstImageAcquisitionTime = dto.firstImageAcquisitionTime;
        entity.lastImageAcquisitionTime = dto.lastImageAcquisitionTime;
        return entity
    }

    static mapSyncFieldsEeg(dto: EegDatasetDTO, entity: EegDataset): EegDataset {
        entity.samplingFrequency = dto.samplingFrequency;
        entity.channelCount = dto.channelCount;
        entity.name = dto.name;
        entity.files = dto.files;
        entity.channels = dto.channels;
        entity.events = dto.events;
        entity.coordinatesSystem = dto.coordinatesSystem;
        return entity
    }
}

export class DatasetDTO {

    id: number;
	creationDate: Date;
    //groupOfSubjectsId: number;
    originMetadata: DatasetMetadata;
    studyId: number;
    studyName: string;
    subjectId: number;
    subjectName: string;
    updatedMetadata: DatasetMetadata;
	name: string;
    type: DatasetType;
    processings: DatasetProcessingDTO[];
	datasetProcessing: DatasetProcessingDTO;
    hasProcessing: boolean;
    datasetParent: number;
    datasetAcquisition: DatasetAcquisitionDTO;
    inPacs: boolean;
    tags: Tag[];
    copies: number[];
    source: number;

    constructor(dataset?: Dataset) {
        if (dataset) {
            this.id = dataset.id;
            this.creationDate = dataset.creationDate;
            this.originMetadata = dataset.originMetadata;
            this.studyId = dataset.study ? dataset.study.id : null;
            this.subjectId = dataset.subject ? dataset.subject.id : null;
            this.updatedMetadata = dataset.updatedMetadata;
            this.source = dataset.source;
            this.copies = dataset.copies;
            this.name = dataset.name;
            this.datasetProcessing = new DatasetProcessingDTO(dataset.datasetProcessing);
            this.type = dataset.type;
            this.processings = dataset.processings.map( (p: DatasetProcessing) => { return new DatasetProcessingDTO(p)} );
            if(dataset.datasetAcquisition) {
                this.datasetAcquisition = new DatasetAcquisitionDTO(dataset.datasetAcquisition);
            }
            this.tags = dataset.tags;
        }
    }
}

export class MrDatasetDTO extends DatasetDTO {
	diffusionGradients: DiffusionGradient[];
	echoTime: EchoTime[];
	flipAngle: FlipAngle[];
	inversionTime: InversionTime[];
	mrQualityProcedureType: MrQualityProcedureType;
	originMrMetadata: MrDatasetMetadataDTO;
	repetitionTime: RepetitionTime[];
	updatedMrMetadata: MrDatasetMetadataDTO;
	firstImageAcquisitionTime: string;
	lastImageAcquisitionTime: string;
}

export class EegDatasetDTO extends DatasetDTO {
    samplingFrequency: number;
    channelCount: number;
    name: string;
    files: string[];
    channels: Channel[];
    events: Event[];
    coordinatesSystem: string;
    selected: boolean;
}

export class MrDatasetMetadataDTO {
    mrDatasetNature: MrDatasetNature;
}
