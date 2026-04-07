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

import { MetadataFieldScope, ConditionScope, Operation, StudyCard, StudyCardAssignment, StudyCardCondition, StudyCardRule } from './study-card.model';


export class StudyCardDTO {

    id: number;
    name: string;
    studyId: number;
    acquisitionEquipmentId: number;
    rules: StudyCardRuleDTO[];

    constructor(studyCard?: StudyCard) {
        if (studyCard) {
            this.id = studyCard.id;
            this.name = studyCard.name;
            this.studyId = studyCard.study ? studyCard.study.id : null;
            this.acquisitionEquipmentId = studyCard.acquisitionEquipment?.id;
            this.rules = studyCard.rules.map(rule => new StudyCardRuleDTO(rule));
        }
    }
}

export class StudyCardRuleDTO {
    constructor(rule: StudyCardRule) {
        this.scope = rule.scope;
        this.conditions = rule.conditions.map(cond => new StudyCardConditionDTO(cond));
        this.assignments = rule.assignments.map(ass => new StudyCardAssignmentDTO(ass));
    }
    scope: MetadataFieldScope;
    assignments: StudyCardAssignmentDTO[];
    conditions: StudyCardConditionDTO[];
}

export class StudyCardConditionDTO {
    scope: ConditionScope;
    shanoirField: string;
    dicomTag: number;
    operation: Operation;
    values: string[];
    cardinality: number;

    constructor(condition: StudyCardCondition) {
        this.scope = condition.scope;
        this.shanoirField = condition.shanoirField;
        this.dicomTag = condition.dicomTag?.code;
        this.operation = condition.operation;
        if (condition.values?.[0] instanceof Coil) {
            this.values = (condition.values as Coil[]).map(coil => coil.id.toString());
        } else {
            this.values = (condition.values as string[]);
        }
        this.cardinality = condition.cardinality;
    }
}
export class StudyCardAssignmentDTO {
    scope: MetadataFieldScope;
    field: string;
    value: string;

    constructor(assignment: StudyCardAssignment) {
        this.scope = assignment.scope;
        this.field = assignment.field;
        if (assignment.value instanceof Coil) {
            this.value = (assignment.value as Coil).id.toString();
        } else {
            this.value = assignment.value as string;
        }
    }
}
