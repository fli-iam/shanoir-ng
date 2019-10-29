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

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from '../../acquisition-equipments/shared/acquisition-equipment.service';
import { StudyService } from '../../studies/shared/study.service';
import { StudyCard } from './study-card.model';
import { NiftiConverterService } from '../../niftiConverters/nifti.converter.service';
import { Study } from '../../studies/shared/study.model';
import { NiftiConverter } from '../../niftiConverters/nifti.converter.model';

@Injectable()
export class StudyCardDTOService {

    constructor(
        private acqEqService: AcquisitionEquipmentService,
        private studyService: StudyService,
        private niftiService: NiftiConverterService) {}

    /**
     * Convert from DTO to Entity
     * Warning : DO NOT USE THIS IN A LOOP, use toEntityList instead
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntity(dto: StudyCardDTO, result?: StudyCard): Promise<StudyCard> {        
        if (!result) result = new StudyCard();
        StudyCardDTOService.mapSyncFields(dto, result);
        return Promise.all([
            this.studyService.get(dto.studyId).then(study => result.study = study),
            this.acqEqService.get(dto.acquisitionEquipmentId).then(acqEq => result.acquisitionEquipment = acqEq),
            this.niftiService.get(dto.niftiConverterId).then(nifti => result.niftiConverter = nifti)
        ]).then(([]) => {
            return result;
        });
    }

    /**
     * Convert from a DTO list to an Entity list
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntityList(dtos: StudyCardDTO[], result?: StudyCard[]): Promise<StudyCard[]>{
        if (!result) result = [];
        for (let dto of dtos) {
            let entity = new StudyCard();
            StudyCardDTOService.mapSyncFields(dto, entity);
            result.push(entity);
        }
        return Promise.all([
            this.studyService.getStudiesNames().then(studies => {
                for (let entity of result) {
                    if (entity.study) 
                        entity.study.name = studies.find(study => study.id == entity.study.id).name;
                }
            }),
            this.acqEqService.getAll().then(acqs => {
                for (let entity of result) {
                    if (entity.acquisitionEquipment) 
                        entity.acquisitionEquipment = acqs.find(acq => acq.id == entity.acquisitionEquipment.id);
                }
            }),
            this.niftiService.getAll().then(niftis => {
                for (let entity of result) {
                    if (entity.niftiConverter) 
                        entity.niftiConverter = niftis.find(nifti => nifti.id == entity.niftiConverter.id);
                }
            })
        ]).then(() => {
            return result;
        })
    }

    static mapSyncFields(dto: StudyCardDTO, entity: StudyCard): StudyCard {
        entity.id = dto.id;
        entity.name = dto.name;
        if (dto.studyId) {
            entity.study = new Study();
            entity.study.id = dto.studyId;
        }
        if (dto.acquisitionEquipmentId) {
            entity.acquisitionEquipment = new AcquisitionEquipment();
            entity.acquisitionEquipment.id = dto.acquisitionEquipmentId;
        }
        if (dto.niftiConverterId) {
            entity.niftiConverter = new NiftiConverter();
            entity.niftiConverter.id = dto.niftiConverterId;
        }
        return entity;
    }
}


export class StudyCardDTO {

    id: number;
    name: string;
    studyId: number;
    acquisitionEquipmentId: number;
    niftiConverterId: number;

    constructor(studyCard?: StudyCard) {
        if (studyCard) {
            this.id = studyCard.id;
            this.name = studyCard.name;
            this.studyId = studyCard.study.id;
            this.acquisitionEquipmentId = studyCard.acquisitionEquipment.id;
            this.niftiConverterId = studyCard.niftiConverter.id;
        }
    }
}