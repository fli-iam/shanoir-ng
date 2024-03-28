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
import { DicomService } from './dicom.service';
import { QualityCardDTO } from './quality-card.dto.model';
import { QualityCard, QualityCardRule } from './quality-card.model';
import { DicomTag, Operation, StudyCardCondition } from './study-card.model';
import { CoilService } from 'src/app/coils/shared/coil.service';
import { Coil } from 'src/app/coils/shared/coil.model';
import { StudyCardDTOService } from './study-card.dto';
import { StudyCardDTOServiceAbstract } from './study-card.dto.abstract';


@Injectable()
export class QualityCardDTOService {

    constructor(
        private studyService: StudyService,
        private dicomService: DicomService,
        private coilService: CoilService
    ) {}

    /**
     * Convert from DTO to Entity
     * Warning : DO NOT USE THIS IN A LOOP, use toEntityList instead
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntity(dto: QualityCardDTO, result?: QualityCard): Promise<QualityCard> {
        if (!result) result = new QualityCard();
        QualityCardDTOService.mapSyncFields(dto, result);
        return Promise.all([
            this.studyService.get(dto.studyId).then(study => result.study = study),
            this.dicomService.getDicomTags().then(tags => this.completeDicomTagNames(result, tags)),
            this.coilService.getAll().then(coils => this.completeCoils(result, coils))
        ]).then(([]) => {
            return result;
        });
    }

    private completeDicomTagNames(result: QualityCard, tags: DicomTag[]) {
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

    private completeCoils(result: QualityCard, coils: Coil[]) {
        if (result.rules) {
            for (let rule of result.rules) {
                rule.conditions?.forEach(cond => {
                    cond.values?.forEach((val, index) => {
                        if (StudyCardDTOService.isCoil(cond.shanoirField)) {
                            if (val instanceof Coil) cond.values[index] = coils.find(coil => coil.id == (val as Coil).id);
                        }
                    });
                });
            }
        }
    }

    /**
     * Convert from a DTO list to an Entity list
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntityList(dtos: QualityCardDTO[], result?: QualityCard[]): Promise<QualityCard[]>{
        if (!result) result = [];
        if (dtos) {
            for (let dto of dtos ? dtos : []) {
                let entity = new QualityCard();
                QualityCardDTOService.mapSyncFields(dto, entity);
                result.push(entity);
            }
        }
        return Promise.all([
            this.studyService.getStudiesNames().then(studies => {
                for (let entity of result) {
                    if (entity.study) 
                        entity.study.name = studies.find(study => study.id == entity.study.id)?.name;
                }
            }),
            this.dicomService.getDicomTags().then(tags => {
                for (let entity of result) {
                    this.completeDicomTagNames(entity, tags)
                }
            })   
        ]).then(() => {
            return result;
        })
    }

    static mapSyncFields(dto: QualityCardDTO, entity: QualityCard): QualityCard {
        entity.id = dto.id;
        entity.name = dto.name;
        if (dto.studyId) {
            entity.study = new Study();
            entity.study.id = dto.studyId;
        }
        entity.rules = [];
        if (dto.rules) {
            for (let ruleDTO of dto.rules) {
                let rule: QualityCardRule = new QualityCardRule();
                rule.tag = ruleDTO.qualityTag;
                rule.orConditions = ruleDTO.orConditions;
                if (ruleDTO.conditions) {
                    rule.conditions = [];
                    for (let conditionDTO of ruleDTO.conditions) {
                        let condition: StudyCardCondition = new StudyCardCondition(conditionDTO.scope);
                        condition.dicomTag = new DicomTag(+conditionDTO.dicomTag, null, null, null);
                        condition.shanoirField = conditionDTO.shanoirField;
                        if (StudyCardDTOServiceAbstract.isCoil(condition.shanoirField) && !Number.isNaN(Number(conditionDTO.values?.[0]))) {
                            condition.values = [];
                            conditionDTO.values?.forEach(dtoVal => {
                                let value = new Coil();
                                value.id = +dtoVal;
                                condition.values.push(value);
                            });
                        } else {
                            condition.values = conditionDTO.values;
                        }
                        condition.operation = conditionDTO.operation as Operation;
                        condition.cardinality = conditionDTO.cardinality;
                        rule.conditions.push(condition);
                    }
                }
                entity.rules.push(rule);
            }
        }
        entity.toCheckAtImport = dto.toCheckAtImport;
        return entity;
    }
}
