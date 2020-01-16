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

@Injectable()
export class DatasetDTOService {

    constructor(
        private studyService: StudyService,
        private subjectService: SubjectService,
    ) {}

    /**
     * Convert from DTO to Entity
     * Warning : DO NOT USE THIS IN A LOOP, use toEntityList instead
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntity(dto: DatasetDTO, result?: Dataset): Promise<Dataset> {      
        if (!result) result = Dataset.getDatasetInstance(dto.type);
        DatasetDTOService.mapSyncFields(dto, result);
        let promises: Promise<any>[] = [];
        if (dto.studyId) promises.push(this.studyService.get(dto.studyId).then(study => result.study = study));
        if (dto.subjectId) promises.push(this.subjectService.get(dto.subjectId).then(subject => result.subject = subject));
        return Promise.all(promises).then(([]) => {
            return result;
        });
    }

    /**
     * Convert from a DTO list to an Entity list
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntityList(dtos: DatasetDTO[], result?: Dataset[]): Promise<Dataset[]>{
        if (!result) result = [];
        for (let dto of dtos) {
            let entity = Dataset.getDatasetInstance(dto.type);
            DatasetDTOService.mapSyncFields(dto, entity);
            result.push(entity);
        }
        return Promise.all([
            this.studyService.getStudiesNames().then(studies => {
                for (let entity of result) {
                    if (entity.study) 
                        entity.study.name = studies.find(study => study.id == entity.study.id).name;
                }
            }),
            this.subjectService.getSubjectsNames().then(subjects => {
                for (let entity of result) {
                    if (entity.subject) 
                        entity.subject.name = subjects.find(subject => subject.id == entity.subject.id).name;
                }
            })
        ]).then(() => {
            return result;
        })
    }

    static mapSyncFields(dto: DatasetDTO, entity: Dataset): Dataset {
        entity.id = dto.id;
        entity.creationDate = dto.creationDate;
        entity.name = dto.name;
        entity.type = dto.type;
        entity.originMetadata = dto.originMetadata;
        entity.updatedMetadata = dto.updatedMetadata;
        if (dto.studyId) {
            entity.study = new Study();
            entity.study.id = dto.studyId;
        }
        if (dto.subjectId) {
            entity.subject = new Subject();
            entity.subject.id = dto.subjectId;
        }
        return entity;
    }
}


export class DatasetDTO {

    
    id: number;
	creationDate: Date;
    //groupOfSubjectsId: number;
    originMetadata: DatasetMetadata;
    studyId: number;
    subjectId: number;
    updatedMetadata: DatasetMetadata;
	name: string;
	type: DatasetType;

    constructor(dataset?: Dataset) {
        if (dataset) {
            this.id = dataset.id;
            this.creationDate = dataset.creationDate;
            //this.groupOfSubjectsId = dataset.groupOfSubjectsId;
            this.originMetadata = dataset.originMetadata;
            this.studyId = dataset.study ? dataset.study.id : null;
            this.subjectId = dataset.subject ? dataset.subject.id : null;
            this.updatedMetadata = dataset.updatedMetadata;
            this.name = dataset.name;
            this.type = dataset.type;
        }
    }
}