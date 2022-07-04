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
import { NiftiConverter } from '../../niftiConverters/nifti.converter.model';
import { NiftiConverterService } from '../../niftiConverters/nifti.converter.service';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { DicomService } from './dicom.service';
import { DicomTag, Operation, StudyCard, StudyCardAssignment, StudyCardCondition, StudyCardRule } from './study-card.model';
import { Coil } from '../../coils/shared/coil.model';
import { CoilService } from '../../coils/shared/coil.service';
import { StudyCardDTO } from './study-card.dto.model';
import { StudyCardDTOServiceAbstract } from './study-card.dto.abstract';

@Injectable()
export class StudyCardDTOService extends StudyCardDTOServiceAbstract {

    constructor(
        private acqEqService: AcquisitionEquipmentService,
        private studyService: StudyService,
        private niftiService: NiftiConverterService,
        private dicomService: DicomService,
        private coilService: CoilService
    ) {
        super();
    }

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
            this.niftiService.get(dto.niftiConverterId).then(nifti => result.niftiConverter = nifti),
            this.dicomService.getDicomTags().then(tags => this.completeDicomTagNames(result, tags)),
            this.coilService.getAll().then(coils => this.completeCoils(result, coils))
        ]).then(([]) => {
            return result;
        });
    }

    private completeDicomTagNames(result: StudyCard, tags: DicomTag[]) {
        if (result.rules) {
            for (let rule of result.rules) {
                if (rule.conditions) {
                    for (let condition of rule.conditions) {
                        condition.dicomTag = tags.find(tag => !!condition.dicomTag && tag.code == condition.dicomTag.code);
                    }
                }
            }
        }
    }

    private completeCoils(result: StudyCard, coils: Coil[]) {
        if (result.rules) {
            for (let rule of result.rules) {
                if (rule.assignments) {
                    for (let assigment of rule.assignments) {
                        if (StudyCardDTOService.isCoil(assigment.field)) {
                            if (assigment.value instanceof Coil) assigment.value = coils.find(coil => coil.id == (assigment.value as Coil).id);
                        }
                    }
                }
            }
        }
    }

    /**
     * Convert from a DTO list to an Entity list
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntityList(dtos: StudyCardDTO[], result?: StudyCard[]): Promise<StudyCard[]>{
        if (!result) result = [];
        if (dtos) {
            for (let dto of dtos) {
                let entity = new StudyCard();
                StudyCardDTOService.mapSyncFields(dto, entity);
                result.push(entity);
            }
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
            }),
            // this.dicomService.getDicomTags().then(tags => {
            //     for (let entity of result) {
            //         this.completeDicomTagNames(entity, tags);
            //     }
            // })
        ]).then(() => {
            return result;
        })
    }

}