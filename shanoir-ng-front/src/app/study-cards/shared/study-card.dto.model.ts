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
import { Coil } from '../../coils/shared/coil.model';
import { Operation, StudyCard } from './study-card.model';


export class StudyCardDTO {

    id: number;
    name: string;
    studyId: number;
    acquisitionEquipmentId: number;
    niftiConverterId: number;
    rules: StudyCardRuleDTO[];

    constructor(studyCard?: StudyCard) {
        if (studyCard) {
            this.id = studyCard.id;
            this.name = studyCard.name;
            this.studyId = studyCard.study ? studyCard.study.id : null;
            this.acquisitionEquipmentId = studyCard.acquisitionEquipment.id;
            this.niftiConverterId = studyCard.niftiConverter.id;
            this.rules = studyCard.rules.map(rule => {
                let ruleDTO: StudyCardRuleDTO = new StudyCardRuleDTO();
                ruleDTO.conditions = rule.conditions.map(cond => {
                    let condDTO: StudyCardConditionDTO = new StudyCardConditionDTO();
                    condDTO.dicomTagOrField = cond.dicomTag ? cond.dicomTag.code : null;
                    let condValueDTO: StudyCardConditionValueDTO = new StudyCardConditionValueDTO();
                    condValueDTO.value = cond.dicomValue;
                    condDTO.values = [];
                    condDTO.values.push(condValueDTO);
                    condDTO.operation = cond.operation;
                    return condDTO;
                });
                ruleDTO.assignments = rule.assignments.map(ass => {
                    let assDTO: StudyCardAssignmentDTO = new StudyCardAssignmentDTO();
                    assDTO.field = ass.field;
                    if (ass.value instanceof Coil) {
                        assDTO.value = (ass.value as Coil).id.toString();
                    } else {
                        assDTO.value = ass.value as string;
                    }
                    return assDTO;
                });
                return ruleDTO;
            });
        }
    }
}

export class StudyCardRuleDTO {
    assignments: StudyCardAssignmentDTO[];
    conditions: StudyCardConditionDTO[];
}

export class StudyCardConditionDTO {
    dicomTagOrField: number;
    operation: Operation;
    values: StudyCardConditionValueDTO[];
}

export class StudyCardConditionValueDTO {
    value: string;
}

export class StudyCardAssignmentDTO {
    field: string;
    value: string;
}
