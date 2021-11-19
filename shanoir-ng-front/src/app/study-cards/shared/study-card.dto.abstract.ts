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
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { Coil } from '../../coils/shared/coil.model';
import { NiftiConverter } from '../../niftiConverters/nifti.converter.model';
import { Study } from '../../studies/shared/study.model';
import { StudyCardDTO } from './study-card.dto.model';
import { DicomTag, Operation, StudyCard, StudyCardAssignment, StudyCardCondition, StudyCardRule } from './study-card.model';


/** This was separated from the rest of the service to prevent a circular dependency warning from angular. */
export abstract class StudyCardDTOServiceAbstract {

    constructor() {}

    
    static isCoil(assigmentField: string): boolean {
        return assigmentField.toLowerCase().includes('coil');
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
        entity.rules = [];
        if (dto.rules) {
            for (let ruleDTO of dto.rules) {
                let rule: StudyCardRule = new StudyCardRule();
                if (ruleDTO.assignments) {
                    rule.assignments = [];
                    for (let assigmentDTO of ruleDTO.assignments) {
                        let assigment: StudyCardAssignment = new StudyCardAssignment();
                        assigment.field = assigmentDTO.field;
                        if (this.isCoil(assigment.field) && !Number.isNaN(Number(assigmentDTO.value))) {
                            assigment.value = new Coil();
                            assigment.value.id = +assigmentDTO.value;
                        } else {
                            assigment.value = assigmentDTO.value;
                        }
                        rule.assignments.push(assigment);
                    }
                }
                if (ruleDTO.conditions) {
                    rule.conditions = [];
                    for (let conditionDTO of ruleDTO.conditions) {
                        let condition: StudyCardCondition = new StudyCardCondition();
                        if (conditionDTO.dicomTag) condition.dicomTag = new DicomTag(+conditionDTO.dicomTag, null);
                        condition.dicomValue = conditionDTO.dicomValue;
                        condition.operation = conditionDTO.operation as Operation;
                        rule.conditions.push(condition);
                    }
                }
                entity.rules.push(rule);
            }
        }
        return entity;
    }
}